package org.b3.agents.openagent.dto;

import lombok.Data;
import java.util.List;

@Data
public class MarkdownDocumentationDTO {
    private String title;
    private List<MarkdownSectionDTO> sections;
    private String type;
}

@Data
class MarkdownSectionDTO {
    private String heading;
    private String purpose;
}
