package com.app.library.exception;

public class UsernameOrPasswordIncorrectException extends Exception{

    public UsernameOrPasswordIncorrectException() {
        super();
    }

    public UsernameOrPasswordIncorrectException(String message) {
        super(message);
    }
}
