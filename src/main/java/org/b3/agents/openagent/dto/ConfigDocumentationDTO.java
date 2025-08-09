package org.b3.agents.openagent.dto;

import lombok.Data;
import java.util.List;

@Data
public class ConfigDocumentationDTO {
    private String purpose;
    private List<ConfigSettingDTO> keySettings;
}

@Data
class ConfigSettingDTO {
    private String key;
    private String purpose;
}
