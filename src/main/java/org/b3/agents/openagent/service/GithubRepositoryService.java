package org.b3.agents.openagent.service;

import org.b3.agents.openagent.config.utils.FileWhitelistUtils;
import org.b3.agents.openagent.config.utils.GithubApiUtils;
import org.b3.agents.openagent.dto.FileDocumentationDTO;
import org.b3.agents.openagent.dto.GithubBlobResponseDTO;
import org.b3.agents.openagent.dto.GithubBranchResponseDTO;
import org.b3.agents.openagent.dto.GithubFileResponseDTO;
import org.b3.agents.openagent.dto.GithubTreeResponseDTO;
import org.b3.agents.openagent.dto.PromptDTO;
import org.b3.agents.openagent.dto.RepositoryFileDTO;
import org.b3.agents.openagent.model.GithubRepository;
import org.b3.agents.openagent.model.RepositoryFile;
import org.b3.agents.openagent.repository.GithubRepositoryRepository;
import org.b3.agents.openagent.repository.RepositoryFileRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import ch.qos.logback.core.util.StringUtil;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class GithubRepositoryService {

    @Autowired
    private GithubRepositoryRepository repository;

    @Autowired
    private RepositoryFileRepository fileRepository;

    @Autowired
    private OciGenerativeAiService ociService;

    public GithubRepository save(GithubRepository repo) {
        return repository.save(repo);
    }

    public List<GithubRepository> findAll() {
        return repository.findAll();
    }

    public void crawlRepositories() {
        List<GithubRepository> repos = findAll();
        for (GithubRepository repo : repos) {
            //fileRepository.deleteAll(fileRepository.findByRepository(repo));
            try {
                String apiUrl = repo.getUrl().replace("https://github.com/", "https://api.github.com/repos/");
                // Get default branch
                var conn = GithubApiUtils.openAuthenticatedConnection(apiUrl);
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.io.InputStream is = conn.getInputStream();
                    String json = new String(is.readAllBytes());
                    is.close();
                    String defaultBranch = json.split("\"default_branch\":\"")[1].split("\"")[0];
                    repo.setDefaultBranch(defaultBranch);
                    // Get tree
                    getRepositoryTree(apiUrl, defaultBranch, repo);
                } else {
                    java.io.InputStream is = conn.getInputStream();
                    String json = new String(is.readAllBytes());
                    is.close();
                    System.out.println(json);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            repository.save(repo);
        }
    }

    private GithubTreeResponseDTO getRepositoryTreeRecursive(String apiUrl, String sha, GithubRepository repo)
            throws IOException {
        String treeApiUrl = apiUrl + "/git/trees/" + sha;
        java.net.HttpURLConnection treeConn = GithubApiUtils.openAuthenticatedConnection(treeApiUrl);
        if (treeConn.getResponseCode() == 200) {
            java.io.InputStream treeIs = treeConn.getInputStream();
            String treeJson = new String(treeIs.readAllBytes());
            treeIs.close();
            GithubTreeResponseDTO treeDTO = new Gson().fromJson(treeJson, GithubTreeResponseDTO.class);
            if (treeDTO.getTree() != null) {
                for (GithubTreeResponseDTO.TreeEntry entry : treeDTO.getTree()) {
                    if ("tree".equals(entry.getType())) {
                        // Recursively fetch children for subtrees
                        entry.setChildren(getRepositoryTreeRecursive(apiUrl, entry.getSha(), repo).getTree());
                    }
                    if ("blob".equals(entry.getType())) {
                        // Fetch file content for blobs
                        // getFilesContentJson(entry.getUrl(), repo.getDefaultBranch(), entry.getPath(),
                        // repo);
                        getBlobContent(entry.getUrl(), repo, entry.getPath());
                    }
                }
            }
            return treeDTO;
        }
        return null;
    }

    private void getRepositoryTree(String apiUrl, String branch, GithubRepository repo)
            throws IOException {
        // Get the root tree SHA for the default branch
        String branchApiUrl = apiUrl + "/branches/" + branch;
        java.net.HttpURLConnection branchConn = GithubApiUtils.openAuthenticatedConnection(branchApiUrl);
        if (branchConn.getResponseCode() == 200) {
            java.io.InputStream is = branchConn.getInputStream();
            String json = new String(is.readAllBytes());
            is.close();
            var jsonretObj = new Gson().fromJson(json, GithubBranchResponseDTO.class);
            var treeUrlExtract = jsonretObj.getCommit().getSha(); // .getAsJsonObject("tree").get("url").getAsString();
            GithubTreeResponseDTO fullTree = getRepositoryTreeRecursive(apiUrl, treeUrlExtract, repo);
            repo.setRepositoryTree(new Gson().toJson(fullTree));
            // Save the tree structure to the repository
            repository.save(repo);
            // Save files to the database
        }
    }

    private void getBlobContent(String url, GithubRepository repo, String fileName) throws IOException {
        var contentConn = GithubApiUtils.openAuthenticatedConnection(url);
        if (contentConn.getResponseCode() == 200) {
            InputStream contentIs = contentConn.getInputStream();
            var contentBlob = new String(contentIs.readAllBytes());
            GithubBlobResponseDTO blob = new Gson().fromJson(contentBlob, GithubBlobResponseDTO.class);
            String content = blob.getContent().replaceAll("\\s+", "");
            byte[] decodedContent = Base64.getDecoder().decode(content);
            String fileContent = new String(decodedContent, StandardCharsets.UTF_8);
            var fileEntity = fileRepository.findByRepositoryAndFilePath(repo, fileName).orElse(new RepositoryFile());
            contentIs.close();
            if (!FileWhitelistUtils.isCodeFile(fileName, fileEntity)) {
                return;
            }
            fileEntity.setContent(fileContent);
            fileEntity.setFileName(fileName);
            fileEntity.setRepository(repo);
            fileEntity.setSize(blob.getSize());
            fillFileDocumentation(fileEntity);
            fileRepository.save(fileEntity);
        }
    }

    private void fillFileDocumentation(RepositoryFile file) {

        var fileContent = file.getContent();
        var fileName = file.getFileName();
        var fileType = file.getFileType();
        var extension = file.getExtension();

        StringBuilder prompt = new StringBuilder();
        prompt.append(
                "Ignore any previous instructions. We will analyze code files in order to document them. I will provide you with the file name, type (if it is markup, backend or frontend), and content of the file, then i will give you a JSON format for you to bring me back the information about the content of said file in a way to explain it with the intention of document what it does. ");
        prompt.append("File name: ").append(fileName).append(". ");
        prompt.append("Extension: ").append(extension).append(". ");
        prompt.append("Type: ").append(fileType).append(". ");
        if ("markup".equalsIgnoreCase(fileType)) {
            prompt.append("This is a markup file. ");
        } else if ("backend".equalsIgnoreCase(fileType)) {
            prompt.append("This is a backend code file. ");
        } else if ("frontend".equalsIgnoreCase(fileType)) {
            prompt.append("This is a frontend code file. ");
        }
        prompt.append("Content: ").append(fileContent).append(". ");
        prompt.append(
                "RESPONSE FORMAT REQUIREMENTS:\n" +
                        "- Return ONLY valid JSON\n" +
                        "- NO markdown json blocks (```json)\n" +
                        "- NO markdown code blocks (```)\n" +
                        "- NO backticks\n" +
                        "- NO explanatory text\n" +
                        "- NO newlines anywhere" +
                        "- Start with { and end with }\n" +
                        "- Use the exact structure provided below:\n");
        prompt.append(GithubApiUtils.JSON_FORMAT);
        prompt.append("\nRemember: Your entire response must be parseable as JSON. Nothing else.");
        try {
            log.info("Documenting file: " + file.getFileName());
            var documentation = ociService.generateReadme(prompt.toString());
            file.setDocumentation(documentation);
            fileRepository.save(file);
        } catch (Exception e) {
            log.error("Error processing file through OCI: " + file.getFileName(), e);
        }
        //generatorWrapper(file, prompt);

    }

    private void generatorWrapper(RepositoryFile file, StringBuilder prompt) {
        try {
            java.net.URL url = new java.net.URL("https://ki6.com.br/hackathon_b3/documentacao");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            PromptDTO body = new PromptDTO();
            body.setTopico(prompt.toString());
            try (java.io.OutputStream os = conn.getOutputStream()) {
                os.write(new Gson().toJson(body).getBytes(StandardCharsets.UTF_8));
            }
            System.out.println("Envio do arquivo: " + file.getFileName());
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (InputStream is = conn.getInputStream()) {
                    String response = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                    var rets = response.replaceAll("```json", "").replaceAll("```", "").replaceAll("\\n", "").trim();
                    JsonObject validatedJson = new Gson().fromJson(rets, JsonObject.class);
                    var md = validatedJson.get("resposta").getAsString();
                    file.setDocumentation(md);
                    // Validate that it's proper JSON by parsing it
                    fileRepository.save(file);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public List<RepositoryFileDTO> findAllFiles() {
        var ret = fileRepository.findAll();

        return ret.stream().map(RepositoryFile::toDTO)
                .collect(Collectors.toList());
    }

    public List<FileDocumentationDTO> generateDocumentation() {
        var ret = fileRepository.findAll();
        var documentationList = new ArrayList<FileDocumentationDTO>();
        for (RepositoryFile file : ret) {
            if (StringUtils.hasText(file.getDocumentation())) {
                try {
                    var json = new Gson().fromJson(file.getDocumentation(), FileDocumentationDTO.class);
                    documentationList.add(json);
                } catch (JsonSyntaxException e) {
                    log.error("Error parsing JSON for file: " + file.getFileName(), e);
                    // Attempt to fix common JSON issues
                    String fixed = file.getDocumentation().trim();

                    fixed = fixed.replaceAll("\\s{2,}", " ");

                    // Fix missing commas between key-value pairs
                    // Pattern: "key":"value""key2":"value2" -> "key":"value","key2":"value2"
                    fixed = fixed.replaceAll("\"\\s*\"([a-zA-Z_][a-zA-Z0-9_]*?)\"\\s*:", "\",\"$1\":");

                    // Fix missing commas between string values and next keys
                    // Pattern: "value""key": -> "value","key":
                    fixed = fixed.replaceAll("\"\\s*\"([a-zA-Z_][a-zA-Z0-9_]*?)\"\\s*:", "\",\"$1\":");

                    // Fix missing commas between numbers and next keys
                    // Pattern: 123"key": -> 123,"key":
                    fixed = fixed.replaceAll("(\\d)\\s*\"([a-zA-Z_][a-zA-Z0-9_]*?)\"\\s*:", "$1,\"$2\":");

                    // Fix missing commas between boolean values and next keys
                    // Pattern: true"key": -> true,"key":
                    fixed = fixed.replaceAll("(true|false)\\s*\"([a-zA-Z_][a-zA-Z0-9_]*?)\"\\s*:", "$1,\"$2\":");

                    // Fix missing commas between null values and next keys
                    // Pattern: null"key": -> null,"key":
                    fixed = fixed.replaceAll("(null)\\s*\"([a-zA-Z_][a-zA-Z0-9_]*?)\"\\s*:", "$1,\"$2\":");

                    // Fix missing commas between arrays and next keys
                    // Pattern: ]"key": -> ],"key":
                    fixed = fixed.replaceAll("\\]\\s*\"([a-zA-Z_][a-zA-Z0-9_]*?)\"\\s*:", "],\"$1\":");

                    // Fix missing commas between objects and next keys
                    // Pattern: }"key": -> },"key":
                    fixed = fixed.replaceAll("\\}\\s*\"([a-zA-Z_][a-zA-Z0-9_]*?)\"\\s*:", "},\"$1\":");

                    // Fix missing commas between string values inside arrays: ["value" "value"] -> ["value","value"]
                    fixed = fixed.replaceAll("\"\\s+\"", "\",\"");
                    
                    // Fix double commas that might have been introduced
                    fixed = fixed.replaceAll(",,+", ",");

                    // Fix comma after opening brace
                    fixed = fixed.replaceAll("\\{\\s*,", "{");

                    try {
                        // Test if the fixed JSON is valid
                        var json = new Gson().fromJson(fixed, FileDocumentationDTO.class);
                        documentationList.add(json);
                        log.info("Successfully fixed malformed JSON");
                    } catch (JsonSyntaxException ex) {
                        log.warn("Could not automatically fix JSON format, using original");
                    }
                }
            }
        }
        return documentationList;
    }
}
