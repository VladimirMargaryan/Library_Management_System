package com.app.library.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class NotVerifiedUserException extends RuntimeException {

    public NotVerifiedUserException() {
        super();
    }

    public NotVerifiedUserException(String message) {
        super(message);
    }
}
