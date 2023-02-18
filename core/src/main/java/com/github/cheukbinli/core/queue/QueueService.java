package com.github.cheukbinli.core.queue;

import com.github.cheukbinli.core.model.NoticeFunctionModel;

import java.util.function.Function;

public interface QueueService<T extends ElementModel> {

    ElementModel getCurrentElement();

    void resetCurrentElement();

    /***
     * 追加元素
     * @param t
     * @return
     */
    QueueService<T> append(T t);

    /***
     *
     * @param t
     * @param positionFunction 返回当前位置
     * @return
     */
    QueueService<T> append(T t, Function<NoticeFunctionModel<T, Long>, Long> positionFunction);

    /***
     * 出列
     * @return
     */
    T dequeue();

    /***
     * 是否存在
     * @param key
     * @return
     */
    boolean contains(String key);

    /***
     * 所有队列总和
     * @return
     */
    long size();

    /***
     * 按渠道查询
     * @param channel
     * @return
     */
    long size(String channel);

    /***
     * 删除
     * @param key
     * @return
     */
    T remove(String key);

    T get(String key);

    long position(String key);

    long statistics(String channel);

}
