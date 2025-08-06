package org.b3.agents.openagent.service;

import org.b3.agents.openagent.config.utils.FileWhitelistUtils;
import org.b3.agents.openagent.config.utils.GithubApiUtils;
import org.b3.agents.openagent.dto.GithubBlobResponseDTO;
import org.b3.agents.openagent.dto.GithubBranchResponseDTO;
import org.b3.agents.openagent.dto.GithubFileResponseDTO;
import org.b3.agents.openagent.dto.GithubTreeResponseDTO;
import org.b3.agents.openagent.dto.RepositoryFileDTO;
import org.b3.agents.openagent.model.GithubRepository;
import org.b3.agents.openagent.model.RepositoryFile;
import org.b3.agents.openagent.repository.GithubRepositoryRepository;
import org.b3.agents.openagent.repository.RepositoryFileRepository;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GithubRepositoryService {

    @Autowired
    private GithubRepositoryRepository repository;

    @Autowired
    private RepositoryFileRepository fileRepository;

    public GithubRepository save(GithubRepository repo) {
        return repository.save(repo);
    }

    public List<GithubRepository> findAll() {
        return repository.findAll();
    }

    public void crawlRepositories() {
        List<GithubRepository> repos = findAll();
        for (GithubRepository repo : repos) {
            fileRepository.deleteAll(fileRepository.findByRepository(repo));
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
                        //getFilesContentJson(entry.getUrl(), repo.getDefaultBranch(), entry.getPath(), repo);
                        var file = getBlobContent(entry.getUrl(), repo, entry.getPath());
                        entry.setFile(file);
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
            for (GithubTreeResponseDTO.TreeEntry entry : fullTree.getTree()) {
                if (entry.getFile() != null) {
                    fileRepository.save(entry.getFile());
                }
            }
        }
    }

    private RepositoryFile getBlobContent(String url, GithubRepository repo, String fileName) throws IOException {
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
                return null; 
            }
            fileEntity.setContent(fileContent);
            fileEntity.setFileName(fileName);
            fileEntity.setRepository(repo);
            fileEntity.setSize(blob.getSize());
            return fileEntity;
        }
        return null;
    }

    private void getFilesContentJson(String apiUrl, String defaultBranch, String path, GithubRepository repo)
            throws MalformedURLException, IOException, ProtocolException {

        String contentApiUrl = apiUrl + "/contents/" + path + "?ref=" + defaultBranch;
        java.net.HttpURLConnection contentConn = GithubApiUtils.openAuthenticatedConnection(contentApiUrl);
        contentConn.setRequestMethod("GET");
        contentConn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        if (contentConn.getResponseCode() == 200) {
            java.io.InputStream contentIs = contentConn.getInputStream();
            var contentJson = new String(contentIs.readAllBytes());
            var jsonDTO = new Gson().fromJson(contentJson, GithubFileResponseDTO.class);
            var fileName = path.substring(path.lastIndexOf('/') + 1);
            var filePath = path;
            var fileEntity = fileRepository.findByRepositoryAndFilePath(repo, filePath).orElse(new RepositoryFile());
            if (!FileWhitelistUtils.isCodeFile(fileName, fileEntity)) {
                return; 
            }
            fileEntity.setFileName(fileName);
            fileEntity.setFilePath(filePath);
            fileEntity.setRepository(repo);
            byte[] decodedBytes = java.util.Base64.getDecoder().decode(jsonDTO.getContent());
            fileEntity.setContent(new String(decodedBytes));
            fileRepository.save(fileEntity);
            contentIs.close();
        }
    }

    public List<RepositoryFileDTO> findAllFiles() {
        return fileRepository.findAll().stream().map(RepositoryFile::toDTO)
                .collect(Collectors.toList());
    }
}
