package org.b3.agents.openagent.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class FileDocumentationDTO {
    private String fileName;
    private String fileType;
    private String summary;
    private DocumentationContentDTO documentation;
    private MetadataDTO metadata;
}
