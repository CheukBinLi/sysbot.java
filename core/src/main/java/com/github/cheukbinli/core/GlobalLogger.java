package com.github.cheukbinli.core;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Function;

public class GlobalLogger {

    static BlockingDeque<Throwable> error = new LinkedBlockingDeque<>(1024);
    static BlockingDeque<String> msg = new LinkedBlockingDeque<>(1024);

    public static void append(String msg) {
        GlobalLogger.msg.add(msg);
    }

    public static void appendln(String msg) {
        GlobalLogger.msg.add(msg + "\n");
    }

    public static void append(Throwable msg) {
        error.add(msg);
    }

    public static <T> T pullExceptionLog(Function<Throwable, T> throwableFunction) throws InterruptedException {
        return throwableFunction.apply(error.takeFirst());
    }

    public static String pullMsgLog() throws InterruptedException {
        return msg.take();
    }

}
