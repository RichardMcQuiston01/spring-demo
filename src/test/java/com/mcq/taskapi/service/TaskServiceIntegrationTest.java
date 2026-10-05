package com.mcq.taskapi.service;

import com.mcq.taskapi.dto.TaskRequest;
import com.mcq.taskapi.dto.TaskResponse;
import com.mcq.taskapi.dto.TaskStatusUpdateRequest;
import com.mcq.taskapi.entity.TaskStatus;
import com.mcq.taskapi.exception.TaskNotFoundException;
import com.mcq.taskapi.support.MutableClock;
import com.mcq.taskapi.support.PostgresIntegrationTest;
import com.mcq.taskapi.support.TestClockConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Import(TestClockConfig.class)
@Transactional
class TaskServiceIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private MutableClock clock;

    @Test
    void createTask_withoutStatus_defaultsToTodo() {
        TaskResponse created = taskService.createTask(new TaskRequest("New task", null, null));

        assertThat(created.id()).isNotNull();
        assertThat(created.status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void updateTask_returnsRefreshedUpdatedAtAndKeepsCreatedAt() {
        TaskResponse created = taskService.createTask(new TaskRequest("Original", "before", TaskStatus.TODO));

        clock.advance(Duration.ofMinutes(10));
        TaskResponse updated = taskService.updateTask(
                created.id(), new TaskRequest("Renamed", "after", TaskStatus.DONE));

        assertThat(updated.title()).isEqualTo("Renamed");
        assertThat(updated.description()).isEqualTo("after");
        assertThat(updated.status()).isEqualTo(TaskStatus.DONE);
        assertThat(updated.createdAt()).isEqualTo(created.createdAt());
        assertThat(updated.updatedAt()).isEqualTo(created.createdAt().plusMinutes(10));
    }

    @Test
    void updateTask_withoutStatus_resetsStatusToTodo() {
        TaskResponse created = taskService.createTask(new TaskRequest("Task", null, TaskStatus.DONE));

        TaskResponse updated = taskService.updateTask(created.id(), new TaskRequest("Task", null, null));

        assertThat(updated.status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void updateTaskStatus_returnsRefreshedUpdatedAt() {
        TaskResponse created = taskService.createTask(new TaskRequest("Task", null, TaskStatus.TODO));

        clock.advance(Duration.ofMinutes(3));
        TaskResponse updated = taskService.updateTaskStatus(
                created.id(), new TaskStatusUpdateRequest(TaskStatus.IN_PROGRESS));

        assertThat(updated.status()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(updated.updatedAt()).isEqualTo(created.createdAt().plusMinutes(3));
    }

    @Test
    void deleteTask_removesTask() {
        TaskResponse created = taskService.createTask(new TaskRequest("Doomed", null, null));

        taskService.deleteTask(created.id());

        assertThatThrownBy(() -> taskService.getTask(created.id()))
                .isInstanceOf(TaskNotFoundException.class);
    }
}
