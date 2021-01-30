package com.carledwinti.library.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiErrors {

    private List<String> errors;

    public ApiErrors(BindingResult bindingResult) {
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> {
            this.errors.add(error.getDefaultMessage());
        });
    }

    public ApiErrors(BusinessException businessException) {
        this.errors = Arrays.asList(businessException.getLocalizedMessage());
    }

    public ApiErrors(ResponseStatusException responseStatusException) {
        this.errors = Arrays.asList(responseStatusException.getReason());
    }

    public List<String> getErrors() {
        return errors;
    }
}
