package org.b3.agents.openagent.service;

import org.b3.agents.openagent.config.utils.FileWhitelistUtils;
import org.b3.agents.openagent.config.utils.GithubApiUtils;
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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
                    // Get tree
                    getRepositoryTree(apiUrl, defaultBranch,repo);
                } 
            } catch (Exception e) {
                e.printStackTrace();
            }
            repository.save(repo); 
        }
    }

    private GithubTreeResponseDTO getRepositoryTreeRecursive(String apiUrl, String sha) throws IOException {
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
                        entry.setChildren(getRepositoryTreeRecursive(apiUrl, entry.getSha()).getTree());
                    }
                }
            }
            return treeDTO;
        }
        return null;
    }

    private void getRepositoryTree(String apiUrl, String defaultBranch, GithubRepository repo)
            throws IOException {
        // Get the root tree SHA for the default branch
        String branchApiUrl = apiUrl;
        java.net.HttpURLConnection branchConn = GithubApiUtils.openAuthenticatedConnection(branchApiUrl);
        if (branchConn.getResponseCode() == 200) {
            java.io.InputStream is = branchConn.getInputStream();
            String json = new String(is.readAllBytes());
            is.close();
            String treeSha = json.split("\"sha\":\"")[1].split("\"")[0];
            GithubTreeResponseDTO fullTree = getRepositoryTreeRecursive(apiUrl, treeSha);
            repo.setRepositoryTree(new Gson().toJson(fullTree));
        }
    }

    private void getFilesContentJson(String apiUrl, String defaultBranch, String treeJson, GithubRepository repo)
            throws MalformedURLException, IOException, ProtocolException {
        // For each file in the tree, fetch its content
        // This is a simplified example, you may want to use a JSON library for parsing
        String[] paths = treeJson.split("\"path\":\"");
        for (int i = 1; i < paths.length; i++) {
            String path = paths[i].split("\"")[0];
            if (!path.endsWith("/")) { // Only files
                String contentApiUrl = apiUrl + "/contents/" + path + "?ref=" + defaultBranch;
                java.net.URL contentUrl = new java.net.URL(contentApiUrl);
                java.net.HttpURLConnection contentConn = (java.net.HttpURLConnection) contentUrl.openConnection();
                contentConn.setRequestMethod("GET");
                contentConn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                if (contentConn.getResponseCode() == 200) {
                    java.io.InputStream contentIs = contentConn.getInputStream();
                    var contentJson = new String(contentIs.readAllBytes());
                    var jsonDTO = new Gson().fromJson(contentJson, GithubFileResponseDTO.class);
                    var fileName = path.substring(path.lastIndexOf('/') + 1);
                    var filePath = path;
                    var fileEntity = fileRepository.findByRepositoryAndFilePath(repo, filePath).orElse(new RepositoryFile());
                    if (!FileWhitelistUtils.isCodeFile(fileName, fileEntity)){
                        continue;
                    }
                    fileEntity.setFileName(fileName);
                    fileEntity.setFilePath(filePath);
                    fileEntity.setRepository(repo);
                    byte[] decodedBytes = java.util.Base64.getDecoder().decode(jsonDTO.getContent());
                    fileEntity.setContent(new String(decodedBytes));
                    fileRepository.save(fileEntity);
                    contentIs.close();
                    // You can now process contentJson (contains base64-encoded file content)
                }
            }
        }
    }

    public List<RepositoryFileDTO> findAllFiles() {
        return fileRepository.findAll().stream().map(RepositoryFile::toDTO)
                .collect(Collectors.toList());
    }
}
