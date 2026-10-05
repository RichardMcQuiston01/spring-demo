package com.mcq.taskapi.exception;

import com.mcq.taskapi.controller.TaskController;
import com.mcq.taskapi.service.TaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
class GlobalExceptionHandlerTest {

    private static final String TASKS_PATH = "/api/v1/tasks";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskService taskService;

    @Test
    void invalidUuidInPath_returns400NamingTheParameter() throws Exception {
        mockMvc.perform(get(TASKS_PATH + "/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Parameter 'id' must be a valid UUID, but received 'not-a-uuid'"))
                .andExpect(jsonPath("$.path").value(TASKS_PATH + "/not-a-uuid"));
    }

    @Test
    void invalidStatusQueryParameter_returns400ListingAllowedValues() throws Exception {
        mockMvc.perform(get(TASKS_PATH).param("status", "BOGUS"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Parameter 'status' must be one of [TODO, IN_PROGRESS, DONE], but received 'BOGUS'"));
    }

    @Test
    void invalidEnumInBody_returns400NamingFieldAndAllowedValues() throws Exception {
        mockMvc.perform(post(TASKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"x\", \"status\": \"BOGUS\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Invalid value 'BOGUS' for field 'status': must be one of [TODO, IN_PROGRESS, DONE]"));
    }

    @Test
    void malformedJsonBody_returns400() throws Exception {
        mockMvc.perform(post(TASKS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{oops"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or is not valid JSON"));
    }

    @Test
    void missingBody_returns400() throws Exception {
        mockMvc.perform(post(TASKS_PATH).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or is not valid JSON"));
    }

    @Test
    void unknownRoute_returns404() throws Exception {
        mockMvc.perform(get("/api/v1/nope"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("No endpoint found for GET /api/v1/nope"))
                .andExpect(jsonPath("$.path").value("/api/v1/nope"));
    }

    @Test
    void unsupportedMethod_returns405WithAllowHeader() throws Exception {
        mockMvc.perform(put(TASKS_PATH))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(header().exists("Allow"))
                .andExpect(jsonPath("$.status").value(405));
    }

    @Test
    void unsupportedContentType_returns415() throws Exception {
        mockMvc.perform(post(TASKS_PATH).contentType(MediaType.TEXT_PLAIN).content("hi"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(jsonPath("$.status").value(415));
    }

    @Test
    void unexpectedException_returns500WithoutLeakingDetails() throws Exception {
        UUID id = UUID.randomUUID();
        given(taskService.getTask(id)).willThrow(new IllegalStateException("db password is hunter2"));

        mockMvc.perform(get(TASKS_PATH + "/{id}", id))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred while processing the request"))
                .andExpect(jsonPath("$.message", not(containsString("hunter2"))))
                .andExpect(jsonPath("$.trace").doesNotExist());
    }
}
