package org.b3.agents.openagent.model;

import org.b3.agents.openagent.dto.RepositoryFileDTO;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class RepositoryFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long size;

    private String fileName;
    private String filePath;
    @Lob
    private String content;

    
    private String language;
    private String fileType; // e.g. frontend, backend, config, etc.
    private String extension;
    

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id")
    private GithubRepository repository;

    public RepositoryFileDTO toDTO() {
        RepositoryFileDTO dto = new RepositoryFileDTO();
        dto.setId(this.id);
        dto.setFileName(this.fileName);
        dto.setFilePath(this.filePath);
        dto.setContent(this.content);
        dto.setLanguage(this.language);
        dto.setFileType(this.fileType);
        dto.setExtension(this.extension);
        dto.setSize(this.size);
        return dto;
    }


}
