package com.mcq.taskapi.service.impl;

import com.mcq.taskapi.dto.TaskRequest;
import com.mcq.taskapi.dto.TaskResponse;
import com.mcq.taskapi.dto.TaskStatusUpdateRequest;
import com.mcq.taskapi.entity.Task;
import com.mcq.taskapi.entity.TaskStatus;
import com.mcq.taskapi.exception.TaskNotFoundException;
import com.mcq.taskapi.repository.TaskRepository;
import com.mcq.taskapi.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;

    public TaskServiceImpl(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Override
    public TaskResponse createTask(TaskRequest request) {
        Task task = new Task();
        applyRequest(task, request);
        return saveAndMap(task);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(TaskStatus status) {
        List<Task> tasks = status != null
                ? taskRepository.findByStatus(status)
                : taskRepository.findAll();

        return tasks.stream()
                .map(TaskResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TaskResponse getTask(UUID id) {
        return TaskResponse.fromEntity(findTaskOrThrow(id));
    }

    @Override
    public TaskResponse updateTask(UUID id, TaskRequest request) {
        Task task = findTaskOrThrow(id);
        applyRequest(task, request);
        return saveAndMap(task);
    }

    @Override
    public TaskResponse updateTaskStatus(UUID id, TaskStatusUpdateRequest request) {
        Task task = findTaskOrThrow(id);
        task.setStatus(request.status());
        return saveAndMap(task);
    }

    @Override
    public void deleteTask(UUID id) {
        taskRepository.delete(findTaskOrThrow(id));
    }

    private Task findTaskOrThrow(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    private void applyRequest(Task task, TaskRequest request) {
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status());
    }

    // Flushing applies the audited timestamps to the entity before it is mapped to the response.
    private TaskResponse saveAndMap(Task task) {
        return TaskResponse.fromEntity(taskRepository.saveAndFlush(task));
    }
}
