package org.b3.agents.openagent.repository;

import org.b3.agents.openagent.model.GithubRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GithubRepositoryRepository extends JpaRepository<GithubRepository, Long> {
}
