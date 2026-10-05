package com.mcq.taskapi.repository;

import com.mcq.taskapi.config.JpaAuditingConfig;
import com.mcq.taskapi.entity.Task;
import com.mcq.taskapi.entity.TaskStatus;
import com.mcq.taskapi.support.MutableClock;
import com.mcq.taskapi.support.PostgresIntegrationTest;
import com.mcq.taskapi.support.TestClockConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({JpaAuditingConfig.class, TestClockConfig.class})
class TaskRepositoryTest extends PostgresIntegrationTest {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private MutableClock clock;

    @Test
    void createReadUpdateDeleteRoundTrip() {
        Task created = taskRepository.saveAndFlush(newTask("Write portfolio README", TaskStatus.TODO));
        OffsetDateTime createdAt = OffsetDateTime.now(clock);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreatedAt()).isEqualTo(createdAt);
        assertThat(created.getUpdatedAt()).isEqualTo(createdAt);

        clock.advance(Duration.ofMinutes(5));
        Task toUpdate = taskRepository.findById(created.getId()).orElseThrow();
        toUpdate.setStatus(TaskStatus.IN_PROGRESS);
        Task updated = taskRepository.saveAndFlush(toUpdate);

        assertThat(updated.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(updated.getCreatedAt()).isEqualTo(createdAt);
        assertThat(updated.getUpdatedAt()).isEqualTo(createdAt.plusMinutes(5));

        taskRepository.deleteById(created.getId());
        assertThat(taskRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void findByStatus_returnsOnlyTasksWithThatStatus() {
        Task todoTask = taskRepository.save(newTask("Todo task", TaskStatus.TODO));
        taskRepository.save(newTask("Done task", TaskStatus.DONE));

        List<Task> todoTasks = taskRepository.findByStatus(TaskStatus.TODO);

        assertThat(todoTasks).extracting(Task::getId).contains(todoTask.getId());
        assertThat(todoTasks).extracting(Task::getStatus).containsOnly(TaskStatus.TODO);
    }

    private Task newTask(String title, TaskStatus status) {
        Task task = new Task();
        task.setTitle(title);
        task.setStatus(status);
        return task;
    }
}
