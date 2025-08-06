package org.b3.agents.openagent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubFileResponseDTO {
    private String name;
    private String path;
    private String sha;
    private Long size;
    private String url;
    @JsonProperty("html_url")
    private String htmlUrl;
    @JsonProperty("git_url")
    private String gitUrl;
    @JsonProperty("download_url")
    private String downloadUrl;
    private String type;
    private String content;
    private String encoding;
    private Links links;

    @Data
    public static class Links {
        private String self;
        private String git;
        private String html;
    }
}
