package com.mcq.taskapi.repository;

import com.mcq.taskapi.entity.Task;
import com.mcq.taskapi.entity.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises a full create -> read -> update -> delete round trip against the repository.
 * Runs against an H2 in-memory database (see application-test.yml) as a documented
 * fallback: this environment does not have a Docker daemon available for Testcontainers.
 */
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void createReadUpdateDeleteRoundTrip() {
        Task task = new Task();
        task.setTitle("Write portfolio README");
        task.setDescription("Document the Task API for recruiters");
        task.setStatus(TaskStatus.TODO);

        Task created = taskRepository.save(task);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getUpdatedAt()).isNotNull();

        UUID id = created.getId();
        Optional<Task> found = taskRepository.findById(id);
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Write portfolio README");
        assertThat(found.get().getStatus()).isEqualTo(TaskStatus.TODO);

        Task toUpdate = found.get();
        toUpdate.setStatus(TaskStatus.IN_PROGRESS);
        Task updated = taskRepository.save(toUpdate);
        assertThat(updated.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(updated.getUpdatedAt()).isAfterOrEqualTo(updated.getCreatedAt());

        taskRepository.deleteById(id);
        assertThat(taskRepository.findById(id)).isEmpty();
    }
}
