package com.github.cheukbinli.core.im;

public class ImServiceException extends RuntimeException {

    public ImServiceException() {
    }

    public ImServiceException(String message) {
        super(message);
    }

    public ImServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ImServiceException(Throwable cause) {
        super(cause);
    }

    public ImServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
