package com.mcq.taskapi.service;

import com.mcq.taskapi.dto.TaskRequest;
import com.mcq.taskapi.dto.TaskResponse;
import com.mcq.taskapi.dto.TaskStatusUpdateRequest;
import com.mcq.taskapi.entity.TaskStatus;

import java.util.List;
import java.util.UUID;

public interface TaskService {

    TaskResponse createTask(TaskRequest request);

    List<TaskResponse> listTasks(TaskStatus status);

    TaskResponse getTask(UUID id);

    TaskResponse updateTask(UUID id, TaskRequest request);

    TaskResponse updateTaskStatus(UUID id, TaskStatusUpdateRequest request);

    void deleteTask(UUID id);
}
