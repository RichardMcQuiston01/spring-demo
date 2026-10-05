package com.mcq.taskapi.dto;

import com.mcq.taskapi.entity.TaskStatus;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TaskRequestTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void status_whenOmitted_defaultsToTodo() {
        assertThat(new TaskRequest("Title", null, null).status()).isEqualTo(TaskStatus.TODO);
    }

    @Test
    void status_whenProvided_isKept() {
        assertThat(new TaskRequest("Title", null, TaskStatus.DONE).status()).isEqualTo(TaskStatus.DONE);
    }

    @Test
    void validate_withValidTitle_hasNoViolations() {
        assertThat(validator.validate(new TaskRequest("A".repeat(200), null, null))).isEmpty();
    }

    @Test
    void validate_withBlankTitle_reportsTitleRequired() {
        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(new TaskRequest("   ", null, null));

        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Title is required");
    }

    @Test
    void validate_withNullTitle_reportsTitleRequired() {
        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(new TaskRequest(null, null, null));

        assertThat(violations).extracting(ConstraintViolation::getMessage).containsExactly("Title is required");
    }

    @Test
    void validate_withTitleOver200Characters_reportsLengthLimit() {
        Set<ConstraintViolation<TaskRequest>> violations = validator.validate(new TaskRequest("A".repeat(201), null, null));

        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .containsExactly("Title must not exceed 200 characters");
    }
}
