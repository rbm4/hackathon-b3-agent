package org.b3.agents.openagent.dto;

import lombok.Data;
import java.util.List;

@Data
public class FrontendDocumentationDTO {
    private ComponentInfoDTO componentInfo;
    private List<FrontendMethodDTO> methods;
    private List<String> state;
    private List<String> props;
    private List<String> hooks;
}

@Data
class ComponentInfoDTO {
    private String name;
    private String type;
    private String purpose;
}

@Data
class FrontendMethodDTO {
    private String name;
    private String purpose;
    private String type;
}
