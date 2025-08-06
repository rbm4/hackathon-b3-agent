package org.b3.agents.openagent.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import lombok.Data;

@Entity
@Data
public class GithubRepository {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String url;
    @Lob
    private String repositoryTree;
    // Reference to RepositoryFile class
    // Assuming a repository can have multiple files
    // and RepositoryFile is another @Entity class

    public GithubRepository() {}

    public GithubRepository(String name, String url) {
        this.name = name;
        this.url = url;
    }

}
