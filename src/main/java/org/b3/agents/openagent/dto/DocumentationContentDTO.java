package org.b3.agents.openagent.dto;

import lombok.Data;

@Data
public class DocumentationContentDTO {
    private BackendDocumentationDTO backend;
    private FrontendDocumentationDTO frontend;
    private MarkdownDocumentationDTO markdown;
    private ConfigDocumentationDTO config;
}
