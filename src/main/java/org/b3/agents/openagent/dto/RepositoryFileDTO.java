package org.b3.agents.openagent.dto;

import lombok.Data;

@Data
public class RepositoryFileDTO {
    private Long id;
    private String fileName;
    private String filePath;
    private String content;
    private String language;
    private String fileType;
    private String extension;
    private Long size;
}
