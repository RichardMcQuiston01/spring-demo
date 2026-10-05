package com.mcq.taskapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcq.taskapi.dto.TaskRequest;
import com.mcq.taskapi.dto.TaskResponse;
import com.mcq.taskapi.dto.TaskStatusUpdateRequest;
import com.mcq.taskapi.entity.TaskStatus;
import com.mcq.taskapi.exception.TaskNotFoundException;
import com.mcq.taskapi.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    private static final String TASKS_PATH = "/api/v1/tasks";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;

    @Test
    void createTask_returns201AndCreatedResource() throws Exception {
        TaskResponse response = taskResponse(UUID.randomUUID(), "Write tests", TaskStatus.TODO);
        given(taskService.createTask(any(TaskRequest.class))).willReturn(response);

        mockMvc.perform(post(TASKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TaskRequest("Write tests", "Cover the happy path", null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.title").value("Write tests"))
                .andExpect(jsonPath("$.status").value("TODO"));
    }

    @Test
    void createTask_withBlankTitle_returns400WithFieldErrors() throws Exception {
        mockMvc.perform(post(TASKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed for one or more fields"))
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Title is required"));
    }

    @Test
    void listTasks_withoutFilter_returnsAllTasks() throws Exception {
        given(taskService.listTasks(null)).willReturn(List.of(
                taskResponse(UUID.randomUUID(), "First", TaskStatus.TODO),
                taskResponse(UUID.randomUUID(), "Second", TaskStatus.DONE)));

        mockMvc.perform(get(TASKS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[1].title").value("Second"));
    }

    @Test
    void listTasks_withStatusFilter_passesStatusToService() throws Exception {
        given(taskService.listTasks(TaskStatus.DONE)).willReturn(List.of());

        mockMvc.perform(get(TASKS_PATH).param("status", "DONE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(taskService).listTasks(TaskStatus.DONE);
    }

    @Test
    void getTask_returnsTask() throws Exception {
        TaskResponse response = taskResponse(UUID.randomUUID(), "Find me", TaskStatus.IN_PROGRESS);
        given(taskService.getTask(response.id())).willReturn(response);

        mockMvc.perform(get(TASKS_PATH + "/{id}", response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Find me"))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void getTask_whenMissing_returns404WithStructuredError() throws Exception {
        UUID id = UUID.randomUUID();
        given(taskService.getTask(id)).willThrow(new TaskNotFoundException(id));

        mockMvc.perform(get(TASKS_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Task with id " + id + " not found"))
                .andExpect(jsonPath("$.path").value(TASKS_PATH + "/" + id));
    }

    @Test
    void updateTask_returnsUpdatedTask() throws Exception {
        TaskResponse response = taskResponse(UUID.randomUUID(), "Renamed", TaskStatus.DONE);
        given(taskService.updateTask(any(UUID.class), any(TaskRequest.class))).willReturn(response);

        mockMvc.perform(put(TASKS_PATH + "/{id}", response.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TaskRequest("Renamed", null, TaskStatus.DONE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Renamed"))
                .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void updateTask_whenMissing_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        given(taskService.updateTask(any(UUID.class), any(TaskRequest.class))).willThrow(new TaskNotFoundException(id));

        mockMvc.perform(put(TASKS_PATH + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(new TaskRequest("Title", null, null))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task with id " + id + " not found"));
    }

    @Test
    void updateTask_withBlankTitle_returns400() throws Exception {
        mockMvc.perform(put(TASKS_PATH + "/{id}", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \" \"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("title"));
    }

    @Test
    void updateTaskStatus_returnsUpdatedTask() throws Exception {
        TaskResponse response = taskResponse(UUID.randomUUID(), "Task", TaskStatus.IN_PROGRESS);
        given(taskService.updateTaskStatus(any(UUID.class), any(TaskStatusUpdateRequest.class))).willReturn(response);

        mockMvc.perform(patch(TASKS_PATH + "/{id}/status", response.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": \"IN_PROGRESS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void updateTaskStatus_withoutStatus_returns400WithFieldError() throws Exception {
        mockMvc.perform(patch(TASKS_PATH + "/{id}/status", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("status"))
                .andExpect(jsonPath("$.fieldErrors[0].message").value("Status is required"));
    }

    @Test
    void deleteTask_returns204() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete(TASKS_PATH + "/{id}", id))
                .andExpect(status().isNoContent());

        verify(taskService).deleteTask(id);
    }

    @Test
    void deleteTask_whenMissing_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new TaskNotFoundException(id)).when(taskService).deleteTask(id);

        mockMvc.perform(delete(TASKS_PATH + "/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Task with id " + id + " not found"));
    }

    private TaskResponse taskResponse(UUID id, String title, TaskStatus taskStatus) {
        OffsetDateTime now = OffsetDateTime.now();
        return new TaskResponse(id, title, null, taskStatus, now, now);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
