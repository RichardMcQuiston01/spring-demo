package com.mcq.taskapi.repository;

import com.mcq.taskapi.entity.Task;
import com.mcq.taskapi.entity.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {

    List<Task> findByStatus(TaskStatus status);
}
