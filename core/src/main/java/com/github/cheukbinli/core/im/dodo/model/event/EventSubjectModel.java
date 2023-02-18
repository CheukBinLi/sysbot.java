package com.github.cheukbinli.core.im.dodo.model.event;

import lombok.Data;

/***
 * 事件主体
 */
@Data
public class EventSubjectModel<T> {

    private int type;//	int	数据类型，0：业务数据
    private EventSubjectDataBusiness<T> data;//	object	业务数据
    private String version;//	string	业务版本

    @Data
    public static class EventSubjectDataBusiness<T> {
        private T eventBody;//	object	事件内容
        private String eventId;//	string	事件ID
        private String eventType;//	string	事件类型
        private long timestamp;//	long	接收时间戳

    }

}
