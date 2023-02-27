package com.github.cheukbinli.core.im;

import com.alibaba.fastjson2.JSON;
import com.github.cheukbinli.core.im.dodo.model.event.EventSubjectModel;
import com.github.cheukbinli.core.im.model.EventMessage;
import com.github.cheukbinli.core.queue.ElementModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public interface EventHandler<Channel, Model> {

    Map<String, Map<String, Function<EventMessage, EventMessage>>> observer = new HashMap<>();

    String getHandlType();

    Class<Model> getBodyType();

    EventManager<Channel> getEventManager();

    default void excute(ImChannel<Channel> channel, EventSubjectModel<String> eventSubjectModel) throws IOException {
        Model model = JSON.parseObject(eventSubjectModel.getData().getEventBody(), getBodyType());
        if (!doExcute(channel, eventSubjectModel, model)) {
            return;//无效数据
        }
        getEventManager().notifyObservers(noticeObserver(eventSubjectModel, model));
//        observer(new EventMessage<>(getEventManager().getImtype(), eventSubjectModel.getData().getEventType(), model));
    }

    boolean doExcute(ImChannel<Channel> channel, EventSubjectModel eventSubjectModel, Model content) throws IOException;

    ElementModel noticeObserver(EventSubjectModel eventSubjectModel, Model mode) throws IOException;

}
