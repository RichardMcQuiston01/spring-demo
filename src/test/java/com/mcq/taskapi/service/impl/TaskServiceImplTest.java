package com.mcq.taskapi.service.impl;

import com.mcq.taskapi.dto.TaskRequest;
import com.mcq.taskapi.dto.TaskResponse;
import com.mcq.taskapi.dto.TaskStatusUpdateRequest;
import com.mcq.taskapi.entity.Task;
import com.mcq.taskapi.entity.TaskStatus;
import com.mcq.taskapi.exception.TaskNotFoundException;
import com.mcq.taskapi.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskServiceImpl taskService;

    @BeforeEach
    void setUp() {
        taskService = new TaskServiceImpl(taskRepository);
    }

    @Test
    void createTask_savesTaskBuiltFromRequest() {
        given(taskRepository.saveAndFlush(any(Task.class))).willAnswer(invocation -> invocation.getArgument(0));

        TaskResponse response = taskService.createTask(new TaskRequest("Write docs", "Cover setup", TaskStatus.IN_PROGRESS));

        ArgumentCaptor<Task> savedTask = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository).saveAndFlush(savedTask.capture());
        assertThat(savedTask.getValue().getTitle()).isEqualTo("Write docs");
        assertThat(savedTask.getValue().getDescription()).isEqualTo("Cover setup");
        assertThat(savedTask.getValue().getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        assertThat(response.title()).isEqualTo("Write docs");
        assertThat(response.status()).isEqualTo(TaskStatus.IN_PROGRESS);
    }

    @Test
    void listTasks_withoutStatus_returnsAllTasks() {
        given(taskRepository.findAll()).willReturn(List.of(taskWithStatus(TaskStatus.TODO), taskWithStatus(TaskStatus.DONE)));

        List<TaskResponse> responses = taskService.listTasks(null);

        assertThat(responses).extracting(TaskResponse::status).containsExactly(TaskStatus.TODO, TaskStatus.DONE);
        verify(taskRepository, never()).findByStatus(any());
    }

    @Test
    void listTasks_withStatus_filtersByThatStatus() {
        given(taskRepository.findByStatus(TaskStatus.DONE)).willReturn(List.of(taskWithStatus(TaskStatus.DONE)));

        List<TaskResponse> responses = taskService.listTasks(TaskStatus.DONE);

        assertThat(responses).extracting(TaskResponse::status).containsExactly(TaskStatus.DONE);
        verify(taskRepository, never()).findAll();
    }

    @Test
    void getTask_whenFound_returnsResponse() {
        Task task = taskWithStatus(TaskStatus.TODO);
        given(taskRepository.findById(task.getId())).willReturn(Optional.of(task));

        TaskResponse response = taskService.getTask(task.getId());

        assertThat(response.id()).isEqualTo(task.getId());
        assertThat(response.title()).isEqualTo(task.getTitle());
    }

    @Test
    void getTask_whenMissing_throwsNotFoundNamingTheId() {
        UUID missingId = UUID.randomUUID();
        given(taskRepository.findById(missingId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTask(missingId))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessage("Task with id " + missingId + " not found");
    }

    @Test
    void updateTask_replacesEveryField() {
        Task task = taskWithStatus(TaskStatus.TODO);
        given(taskRepository.findById(task.getId())).willReturn(Optional.of(task));
        given(taskRepository.saveAndFlush(task)).willReturn(task);

        TaskResponse response = taskService.updateTask(task.getId(), new TaskRequest("New title", null, TaskStatus.DONE));

        assertThat(response.title()).isEqualTo("New title");
        assertThat(response.description()).isNull();
        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void updateTask_whenMissing_throwsAndSavesNothing() {
        UUID missingId = UUID.randomUUID();
        given(taskRepository.findById(missingId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.updateTask(missingId, new TaskRequest("Title", null, null)))
                .isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateTaskStatus_changesOnlyTheStatus() {
        Task task = taskWithStatus(TaskStatus.TODO);
        given(taskRepository.findById(task.getId())).willReturn(Optional.of(task));
        given(taskRepository.saveAndFlush(task)).willReturn(task);

        TaskResponse response = taskService.updateTaskStatus(task.getId(), new TaskStatusUpdateRequest(TaskStatus.DONE));

        assertThat(response.status()).isEqualTo(TaskStatus.DONE);
        assertThat(response.title()).isEqualTo("Existing task");
    }

    @Test
    void deleteTask_deletesTheFoundTask() {
        Task task = taskWithStatus(TaskStatus.TODO);
        given(taskRepository.findById(task.getId())).willReturn(Optional.of(task));

        taskService.deleteTask(task.getId());

        verify(taskRepository).delete(task);
    }

    @Test
    void deleteTask_whenMissing_throwsAndDeletesNothing() {
        UUID missingId = UUID.randomUUID();
        given(taskRepository.findById(missingId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(missingId)).isInstanceOf(TaskNotFoundException.class);
        verify(taskRepository, never()).delete(any());
    }

    private Task taskWithStatus(TaskStatus status) {
        Task task = new Task();
        task.setId(UUID.randomUUID());
        task.setTitle("Existing task");
        task.setDescription("Existing description");
        task.setStatus(status);
        return task;
    }
}
