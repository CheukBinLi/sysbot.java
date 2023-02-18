package com.github.cheukbinli.core.queue;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ElementModel<DATA> {

    /***
     * 用户编码
     */
    private String identity;

    /***
     * 用户名称
     */
    private String userName;

    /***
     * 是否收费用户
     */
    private boolean vip;
    /***
     * 是否有效
     */
    private boolean invalid;

    /***
     * 数据
     */
    private DATA data;

    /***
     * im渠道
     */
    private String channel;

    /***
     * 区域来源
     */
    private String islandSource;

    private String queueChannel;
    /***
     * 当前位置
     */
    private long currentLocation;

    /***
     * 实时操作消息
     */
    private String operationMessage;
    /***
     * 流程操作状态
     */
    private int operationStatus;

//    public ElementModel<DATA> append(DATA data) {
////        Object o = null == this.data ? this.data = Arrays.asList(data) : this.data.add(data);
//        if (null == this.data) {
//            this.data = Arrays.asList(data);
//        } else {
//            this.data.add(data);
//        }
//        return this;
//    }

}
