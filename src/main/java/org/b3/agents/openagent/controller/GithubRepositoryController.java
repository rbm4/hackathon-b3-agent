package org.b3.agents.openagent.controller;

import org.b3.agents.openagent.dto.FileDocumentationDTO;
import org.b3.agents.openagent.dto.RepositoryFileDTO;
import org.b3.agents.openagent.model.GithubRepository;
import org.b3.agents.openagent.model.RepositoryFile;
import org.b3.agents.openagent.service.GithubRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/github-repositories")
public class GithubRepositoryController {
    @Autowired
    private GithubRepositoryService service;

    @GetMapping("/crawl")
    public ResponseEntity<String> crawlRepositories() {
        service.crawlRepositories();
        return ResponseEntity.ok("Crawling triggered");
    }

    @PostMapping
    public ResponseEntity<GithubRepository> createRepository(@RequestBody GithubRepository repo) {
        GithubRepository saved = service.save(repo);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<GithubRepository>> getAllRepositories() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/files")
    public ResponseEntity<List<RepositoryFileDTO>> getAllFiles() {
        return ResponseEntity.ok(service.findAllFiles());
    }

    @GetMapping("/genDoc")
    public ResponseEntity<List<FileDocumentationDTO>> generateDocumentation() {
        var documentation = service.generateDocumentation();
        return ResponseEntity.ok(documentation);
    }
}
