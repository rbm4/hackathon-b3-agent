package org.b3.agents.openagent.config;

import org.b3.agents.openagent.model.GithubRepository;
import org.b3.agents.openagent.service.GithubRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private GithubRepositoryService service;

    @Override
    public void run(String... args) {
        service.save(new GithubRepository("rbm4/hackathon-b3-agent", "https://github.com/cpbet/cpbet-back"));
    }
}
