package org.b3.agents.openagent.dto;

import lombok.Data;
import java.util.List;

@Data
public class MetadataDTO {
    private String analysisTimestamp;
    private String complexity;
    private String maintainability;
    private List<String> keyFeatures;
}
