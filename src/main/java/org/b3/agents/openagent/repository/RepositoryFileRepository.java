package org.b3.agents.openagent.repository;

import java.util.List;
import java.util.Optional;

import org.b3.agents.openagent.model.GithubRepository;
import org.b3.agents.openagent.model.RepositoryFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RepositoryFileRepository extends JpaRepository<RepositoryFile, Long> {
    List<RepositoryFile> findByRepository(GithubRepository repository);
    Optional<RepositoryFile> findByRepositoryAndFilePath(GithubRepository repository, String filePath);
}
