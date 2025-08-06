package org.b3.agents.openagent.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class GithubBlobResponseDTO {
    private String sha;
    @JsonProperty("node_id")
    private String nodeId;
    private Long size;
    private String url;
    private String content;
    private String encoding;
}
