package org.b3.agents.openagent.dto;

import lombok.Data;
import java.util.List;

@Data
public class BackendDocumentationDTO {
    private ClassInfoDTO classInfo;
    private List<MethodDocumentationDTO> methods;
    private List<String> dependencies;
    private List<String> patterns;
}

@Data
class ClassInfoDTO {
    private String name;
    private String purpose;
    private List<String> responsibilities;
}

@Data
class MethodDocumentationDTO {
    private String name;
    private String purpose;
    private List<String> parameters;
    private String returnType;
    private String complexity;
}
