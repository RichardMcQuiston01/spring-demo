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
        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setStatus(request.status() != null ? request.status() : TaskStatus.TODO);

        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
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
        Task task = findTaskOrThrow(id);
        return TaskResponse.fromEntity(task);
    }

    @Override
    public TaskResponse updateTask(UUID id, TaskRequest request) {
        Task task = findTaskOrThrow(id);
        task.setTitle(request.title());
        task.setDescription(request.description());
        if (request.status() != null) {
            task.setStatus(request.status());
        }

        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
    }

    @Override
    public TaskResponse updateTaskStatus(UUID id, TaskStatusUpdateRequest request) {
        Task task = findTaskOrThrow(id);
        task.setStatus(request.status());

        Task savedTask = taskRepository.save(task);
        return TaskResponse.fromEntity(savedTask);
    }

    @Override
    public void deleteTask(UUID id) {
        Task task = findTaskOrThrow(id);
        taskRepository.delete(task);
    }

    private Task findTaskOrThrow(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }
}
