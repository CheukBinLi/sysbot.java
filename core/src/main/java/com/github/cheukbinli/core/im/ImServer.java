package com.github.cheukbinli.core.im;

import com.github.cheukbinli.core.im.dodo.model.dto.ChanneInfo;

import java.io.IOException;

public interface ImServer {
    void start();

    void stop();

    void personalMessageSend(String toId, String islandSourceId, String message) throws IOException;

    void channelMessageSend(String channel, String atId, boolean atAll, String message) throws IOException;

    ChanneInfo getChanneInfo(String channelID) throws IOException;

    void writeLogAndWriteChannel(String islandSourceId, String channel, String atId, String message) throws IOException;
}
