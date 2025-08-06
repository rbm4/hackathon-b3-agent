package org.b3.agents.openagent.service;

import org.b3.agents.openagent.model.GithubRepository;
import org.b3.agents.openagent.repository.GithubRepositoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GithubRepositoryService {
    @Autowired
    private GithubRepositoryRepository repository;

    public GithubRepository save(GithubRepository repo) {
        return repository.save(repo);
    }

    public List<GithubRepository> findAll() {
        return repository.findAll();
    }

    // Placeholder for crawling logic
    public void crawlRepositories() {
        // TODO: Implement GitHub API crawling logic here
    }
}
