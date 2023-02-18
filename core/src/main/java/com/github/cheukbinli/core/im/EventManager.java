package com.github.cheukbinli.core.im;

import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.im.dodo.model.dto.RobotInfo;
import com.github.cheukbinli.core.model.NoticeFunctionModel;
import com.github.cheukbinli.core.model.TradeElementModel;
import com.github.cheukbinli.core.queue.ElementModel;
import com.github.cheukbinli.core.queue.QueueService;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public interface EventManager<CHANNEL> {

    Map<String, Map<String, EventHandler>> EVENT_HANDLERS_POOL = new ConcurrentHashMap<>();

    ImServer getImServer();

    QueueService getQueue();

    RobotInfo getRobotInfo();

    default String getImtype() {
        return this.getClass().getSimpleName();
    }

    default <T extends EventHandler> T getHandler(String imInstance, String eventType) {
        Map<String, EventHandler> instancesHamdler = EVENT_HANDLERS_POOL.get(imInstance);
        if (null == instancesHamdler || instancesHamdler.isEmpty()) {
            return null;
        }
        return (T) instancesHamdler.get(eventType);
    }

    default EventManager appendEventHandle(EventHandler handler) {
        String className = this.getClass().getSimpleName();
        Map<String, EventHandler> pool = EVENT_HANDLERS_POOL.get(className);
        if (null == pool) {
            pool = new ConcurrentHashMap<>();
            EVENT_HANDLERS_POOL.put(className, pool);
        }
        pool.put(handler.getHandlType(), handler);
        return this;
    }

    default <T> void notifyObservers(ElementModel elementModel) {
        getQueue().append(elementModel, new Function<NoticeFunctionModel<TradeElementModel, Long>, Long>() {
            @Override
            public Long apply(NoticeFunctionModel<TradeElementModel, Long> noticeFunctionModel) {
                if (0 > noticeFunctionModel.getCode()) {
                    try {
                        getImServer().writeLogAndWriteChannel(
                                noticeFunctionModel.getData().getIslandSource(),
                                noticeFunctionModel.getData().getChannel(),
                                noticeFunctionModel.getData().getIdentity(),
                                noticeFunctionModel.getMsg());
                    } catch (IOException e) {
                        GlobalLogger.append(e);
                        e.printStackTrace();
                    }
                }
                return null;
            }
        });
    }

    void excute(ImChannel<CHANNEL> channel, byte[] data) throws IOException;

}
