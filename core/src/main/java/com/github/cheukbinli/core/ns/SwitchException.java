package com.github.cheukbinli.core.ns;

public class SwitchException extends RuntimeException {
    public SwitchException() {
        super();
    }

    public SwitchException(String message) {
        super(message);
    }

    public SwitchException(String message, Throwable cause) {
        super(message, cause);
    }

    public SwitchException(Throwable cause) {
        super(cause);
    }

    protected SwitchException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
