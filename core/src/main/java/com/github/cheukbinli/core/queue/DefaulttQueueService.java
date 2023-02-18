package com.github.cheukbinli.core.queue;

import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.model.NoticeFunctionModel;
import com.github.cheukbinli.core.model.TradeElementModel;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class DefaulttQueueService<T extends ElementModel> implements QueueService<T> {

    @Getter
    protected Map<String, ElementModel> freeQueueMap = new ConcurrentHashMap<>(256);
    @Getter
    protected Map<String, ElementModel> vipQueueMap = new ConcurrentHashMap<>(256);
    @Getter
    protected LinkedList<ElementModel> freeQueue = new LinkedList<>();
    @Getter
    protected LinkedList<ElementModel> vipQueue = new LinkedList<>();

    private CounterUtil counterUtil = new CounterUtil();

    private volatile ElementModel swapCurrentElement = new TradeElementModel().setIdentity("");
    @Getter
    private volatile ElementModel currentElement = swapCurrentElement;

    Function<NoticeFunctionModel<T, Long>, Long> DEFAULT_NOTICE_FUNCTION = new Function<NoticeFunctionModel<T, Long>, Long>() {
        @Override
        public Long apply(NoticeFunctionModel aLong) {
            return null;
        }
    };

    private String vipChannel = "vip";
    private String freeChannel = "free";

    public class CounterUtil {
        Map<String, AtomicLong> counterPool = new ConcurrentHashMap<>();

        public long addAndGetOrSet(String channel, boolean set, boolean invalidQueue, int i) {
            channel = invalidQueue ? channel + "_invalid" : channel;
            AtomicLong counter = counterPool.get(channel);
            if (null == counter) {
                counter = new AtomicLong(1);
                counterPool.put(channel, counter);
                return 1;
            }
            return i != 0 ? counter.addAndGet(i) : counter.get();
        }

    }

    /***
     * 比例
     */
    @Getter
    @Setter
    private int proportion = 8;

    /***
     * 计数器
     */
    @Getter
    private AtomicInteger counter = new AtomicInteger(0);
    /***
     * 队列元素数量限制
     */
    @Setter
    @Getter
    private volatile int maxElement = 3;

    @Override
    public void resetCurrentElement() {
        this.currentElement = swapCurrentElement;
    }

    @Override
    public QueueService<T> append(T t) {
        return append(t, null);
    }

    @Override
    public QueueService<T> append(T t, Function<NoticeFunctionModel<T, Long>, Long> noticeFunction) {
        noticeFunction = null == noticeFunction ? DEFAULT_NOTICE_FUNCTION : noticeFunction;
        if (null == t) {
            return this;
        } else if (t.isVip()) {
            ElementModel queue = vipQueueMap.get(t.getIdentity());
            if (null == queue && !getCurrentElement().getIdentity().equals(t.getIdentity())) {
                queue = t;
                vipQueueMap.put(t.getIdentity(), t);
            } else {
//            } else if (queue.getData().size() > maxElement) {
                noticeFunction.apply(new NoticeFunctionModel().setData(t).setCode(-1).setMsg("你已在队列。"));
                return this;
            }
//            queue.getData().add(queue);
            long size = counterUtil.addAndGetOrSet(vipChannel, false, false, 1);
            vipQueue.add(queue.setQueueChannel(vipChannel).setCurrentLocation(size));
            t = (T) queue;
        } else {
            if (freeQueueMap.containsKey(t.getIdentity())) {
                noticeFunction.apply(new NoticeFunctionModel().setData(t).setCode(-1).setMsg("你已在队列。"));
                return this;
            }
            freeQueueMap.put(t.getIdentity(), t);
            long size = counterUtil.addAndGetOrSet(freeChannel, false, false, 1);
            freeQueue.add(t.setQueueChannel(freeChannel).setCurrentLocation(size));
        }
        if (null != noticeFunction) {
            noticeFunction.apply(new NoticeFunctionModel().setData(t).setAdditional(t.getCurrentLocation() - counterUtil.addAndGetOrSet(t.getQueueChannel(), false, true, 0)));
        }
        return this;
    }

    @Override
    public T dequeue() {
        T t = null;
        do {
            if (vipQueue.isEmpty() && !freeQueue.isEmpty()) {
                t = (T) freeQueue.remove();
                counter.set(0);
                counterUtil.addAndGetOrSet(freeChannel, false, true, 1);
            }
            if (!vipQueue.isEmpty() && counter.get() <= proportion) {
                counter.addAndGet(1);
                t = (T) vipQueue.remove();
                counterUtil.addAndGetOrSet(vipChannel, false, true, 1);
            } else if (!freeQueue.isEmpty()) {
                counter.set(0);
                t = (T) freeQueue.remove();
                counterUtil.addAndGetOrSet(freeChannel, false, true, 1);
            }
            if (null == t || !t.isInvalid()) {
                synchronized (this) {
                    try {
                        wait(500);
                    } catch (InterruptedException e) {
                        GlobalLogger.append(e);
                        throw new RuntimeException(e);
                    }
                }
            }
        } while (null == t);
        currentElement = t;
        vipQueueMap.remove(t.getIdentity());
        freeQueueMap.remove(t.getIdentity());
        return t;
    }

    @Override
    public boolean contains(String key) {
        return false;
    }

    @Override
    public long size() {
        return vipQueue.size() + freeQueue.size();
    }

    @Override
    public long size(String channel) {
        return vipChannel.equals(channel) ? vipQueue.size() : freeQueue.size();
    }

    @Override
    public T remove(String key) {
        ElementModel elementModel = freeQueueMap.remove(key);
        if (null == elementModel) {
            elementModel = vipQueueMap.remove(key);
        }
        if (null == elementModel) {
            return null;
        }
        elementModel.setInvalid(true);
        return (T) elementModel;
    }

    @Override
    public T get(String key) {
        ElementModel list = freeQueueMap.get(key);
        if (null == list) {
            list = vipQueueMap.get(key);
        }
        return null == list ? null : (T) list;
    }

    public long position(String key) {
        if (key.equals(getCurrentElement().getIdentity())) {
            return 0;
        }
        T t = get(key);
        if (null == t) {
            return -100;
        }
        return t.getCurrentLocation() - counterUtil.addAndGetOrSet(t.getQueueChannel(), false, true, 0);
    }

    public long statistics(String channel) {
        return counterUtil.addAndGetOrSet(channel, false, false, 0);
    }
}
