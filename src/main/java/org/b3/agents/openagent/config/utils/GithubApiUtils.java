package org.b3.agents.openagent.config.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class GithubApiUtils {

    private static String token = System.getenv("GITHUB_KEY");

    /**
     * Opens an authenticated connection to the given GitHub API endpoint.
     * 
     * @param endpointUrl The full API endpoint URL.
     * @param token       The GitHub personal access token.
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

    public static final String JSON_FORMAT = """
                        {
              "fileType": "backend|frontend|markdown|config|other",
              "summary": "Brief description of the file's purpose",
              "documentation": {
                "backend": {
                  "classInfo": {
                    "name": "ClassName",
                    "purpose": "What this class or component does",
                    "responsibilities": ["responsibility1", "responsibility2"]
                  },
                  "methods": [
                    {
                      "name": "methodName",
                      "purpose": "What this method does",
                      "parameters": ["param1: type - description"],
                      "returnType": "return type and description",
                      "complexity": "low|medium|high"
                    }
                  ],
                  "dependencies": ["dependency1", "dependency2"],
                  "patterns": ["design patterns used"]
                },
                "frontend": {
                  "componentInfo": {
                    "name": "ComponentName",
                    "type": "component|page|utility|hook",
                    "purpose": "What this component/file does"
                  },
                  "methods": [
                    {
                      "name": "functionName",
                      "purpose": "General description of what it does",
                      "type": "handler|utility|render|lifecycle"
                    }
                  ],
                  "state": ["state variables managed"],
                  "props": ["expected props"],
                  "hooks": ["hooks used"]
                },
                "markdown": {
                  "title": "Document title",
                  "sections": [
                    {
                      "heading": "Section name",
                      "purpose": "What this section covers"
                    }
                  ],
                  "type": "documentation|readme|guide|specification"
                },
                "config": {
                  "purpose": "Configuration file purpose",
                  "keySettings": [
                    {
                      "key": "setting name",
                      "purpose": "what this setting does"
                    }
                  ]
                }
              },
              "metadata": {
                "analysisTimestamp": "2025-08-06T10:30:00Z",
                "complexity": "low|medium|high",
                "maintainability": "good|fair|poor",
                "keyFeatures": ["feature1", "feature2"]
              }
            }
            """;
}
