package com.github.cheukbinli.core.ns.command;

public class CommectionException extends RuntimeException{
    public CommectionException() {
    }

    public CommectionException(String message) {
        super(message);
    }

    public CommectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommectionException(Throwable cause) {
        super(cause);
    }

    public CommectionException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
