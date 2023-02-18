package com.github.cheukbinli.core.im;

public interface MessageReceiver {

    default void init() {
    }

    void start();

    void stop();

    MessageReceiver appendHandler(EventHandler handler);

    void removeHandler(String handlerType);

    <T> EventManager<T> getEventManager();

}
