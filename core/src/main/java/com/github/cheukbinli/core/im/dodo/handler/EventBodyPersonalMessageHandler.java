package com.github.cheukbinli.core.im.dodo.handler;

import com.github.cheukbinli.core.im.EventHandler;
import com.github.cheukbinli.core.im.EventManager;
import com.github.cheukbinli.core.im.ImChannel;
import com.github.cheukbinli.core.im.dodo.model.event.EventBodyPersonalMessage;
import com.github.cheukbinli.core.im.dodo.model.event.EventSubjectModel;
import com.github.cheukbinli.core.queue.ElementModel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import okhttp3.WebSocket;

@Data
@RequiredArgsConstructor
public class EventBodyPersonalMessageHandler implements EventHandler<WebSocket, EventBodyPersonalMessage.EventBodyPersonalMessageBody> {

    private final EventManager<WebSocket> eventManager;

    @Override
    public String getHandlType() {
        return "1001";
    }

    @Override
    public Class<EventBodyPersonalMessage.EventBodyPersonalMessageBody> getBodyType() {
        return EventBodyPersonalMessage.EventBodyPersonalMessageBody.class;
    }

    @Override
    public boolean doExcute(ImChannel<WebSocket> webSocket, EventSubjectModel eventSubjectModel, EventBodyPersonalMessage.EventBodyPersonalMessageBody content) {
        return true;
    }

    @Override
    public ElementModel noticeObserver(EventSubjectModel eventSubjectModel, EventBodyPersonalMessage.EventBodyPersonalMessageBody mode) {
        return null;
    }
}
