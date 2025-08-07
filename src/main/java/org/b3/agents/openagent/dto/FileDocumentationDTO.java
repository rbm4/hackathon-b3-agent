package org.b3.agents.openagent.dto;

import lombok.Data;

@Data
public class FileDocumentationDTO {
    private String fileType;
    private String summary;
    private DocumentationContentDTO documentation;
    private MetadataDTO metadata;
}
