package com.app.library.exception;

public class EmailAlreadyExistException extends Exception{

    public EmailAlreadyExistException() {
        super();
    }

    public EmailAlreadyExistException(String message) {
        super(message);
    }
}
