package org.b3.agents.openagent.config.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class GithubApiUtils {

    private static String token = System.getenv("GITHUB_KEY");
    
    /**
     * Opens an authenticated connection to the given GitHub API endpoint.
     * @param endpointUrl The full API endpoint URL.
     * @param token The GitHub personal access token.
     * @return An open HttpURLConnection with authentication headers set.
     * @throws IOException If an error occurs opening the connection.
     */
    public static HttpURLConnection openAuthenticatedConnection(String endpointUrl) throws IOException {
        URL url = new URL(endpointUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("X-GitHub-Api-Version", "2022-11-28");
        return conn;
    }
}
