package com.github.cheukbinli.core.im.dodo;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.im.EventHandler;
import com.github.cheukbinli.core.im.EventManager;
import com.github.cheukbinli.core.im.ImChannel;
import com.github.cheukbinli.core.im.ImServer;
import com.github.cheukbinli.core.im.dodo.model.dto.RobotInfo;
import com.github.cheukbinli.core.im.dodo.model.event.EventSubjectModel;
import com.github.cheukbinli.core.queue.QueueService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import okhttp3.WebSocket;

import java.io.IOException;

@Data
@RequiredArgsConstructor
public class DodoEventManager implements EventManager<WebSocket> {

    private final ImServer imServer;
    private final QueueService queue;
    private final RobotInfo robotInfo;

    @Override
    public QueueService getQueue() {
        return this.queue;
    }

    @Override
    public RobotInfo getRobotInfo() {
        return this.robotInfo;
    }

    @Override
    public void excute(ImChannel<WebSocket> channel, byte[] data) throws IOException {
        TypeReference<EventSubjectModel<String>> eventSubjectModelTypeReference = new TypeReference<EventSubjectModel<String>>(EventSubjectModel.class, String.class) {
        };

        EventSubjectModel<String> eventSubjectModel = JSON.parseObject(new String(data), eventSubjectModelTypeReference);
        if (eventSubjectModel.getType() != 0) {
            //心跳
            return;
        }
        System.out.println("事件类型:" + eventSubjectModel.getData().getEventType());
        GlobalLogger.appendln("事件类型:" + eventSubjectModel.getData().getEventType());
        EventHandler<WebSocket, Object> handler = getHandler(getImtype(), eventSubjectModel.getData().getEventType());
        if (null == handler) {
            return;
        }
        handler.excute(channel, eventSubjectModel);
    }
}
