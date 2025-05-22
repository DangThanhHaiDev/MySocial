package com.mysocial.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String message;
    private List<FieldError> errors = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }

}
