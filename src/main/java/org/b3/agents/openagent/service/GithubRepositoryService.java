package org.b3.agents.openagent.service;

import org.b3.agents.openagent.model.GithubRepository;
import org.b3.agents.openagent.repository.GithubRepositoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.util.List;

@Service
public class GithubRepositoryService {
    @Autowired
    private GithubRepositoryRepository repository;

    public GithubRepository save(GithubRepository repo) {
        return repository.save(repo);
    }

    public List<GithubRepository> findAll() {
        return repository.findAll();
    }

    // Placeholder for crawling logic
    public void crawlRepositories() {
        List<GithubRepository> repos = findAll();
        for (GithubRepository repo : repos) {
            try {
                String apiUrl = repo.getUrl().replace("https://github.com/", "https://api.github.com/repos/");
                // Get default branch
                java.net.URL repoApiUrl = new java.net.URL(apiUrl);
                java.net.HttpURLConnection conn = (java.net.HttpURLConnection) repoApiUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    java.io.InputStream is = conn.getInputStream();
                    String json = new String(is.readAllBytes());
                    is.close();
                    String defaultBranch = json.split("\"default_branch\":\"")[1].split("\"")[0];
                    // Get tree
                    getRepositoryTree(apiUrl, defaultBranch);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getRepositoryTree(String apiUrl, String defaultBranch)
            throws MalformedURLException, IOException, ProtocolException {
        String treeApiUrl = apiUrl + "/git/trees/" + defaultBranch + "?recursive=1";
        java.net.URL treeUrl = new java.net.URL(treeApiUrl);
        java.net.HttpURLConnection treeConn = (java.net.HttpURLConnection) treeUrl.openConnection();
        treeConn.setRequestMethod("GET");
        treeConn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        if (treeConn.getResponseCode() == 200) {
            java.io.InputStream treeIs = treeConn.getInputStream();
            String treeJson = new String(treeIs.readAllBytes());
            treeIs.close();
            getFilesContentJson(apiUrl, defaultBranch, treeJson);
        }
    }

    private void getFilesContentJson(String apiUrl, String defaultBranch, String treeJson)
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
                    String contentJson = new String(contentIs.readAllBytes());
                    contentIs.close();
                    // You can now process contentJson (contains base64-encoded file content)
                }
            }
        }
    }
}
