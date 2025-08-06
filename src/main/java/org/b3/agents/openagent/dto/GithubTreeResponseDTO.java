package org.b3.agents.openagent.dto;
import org.b3.agents.openagent.model.RepositoryFile;
import lombok.Data;
import java.util.List;

@Data
public class GithubTreeResponseDTO {
    private String sha;
    private String url;
    private List<TreeEntry> tree;
    private boolean truncated;
    
    @Data
    public static class TreeEntry {
        private RepositoryFile file;
        private String path;
        private String mode;
        private String type; // "tree" or "blob"
        private String sha;
        private Long size;
        private String url;
        private List<TreeEntry> children; // Only for type="tree"
    }
}
