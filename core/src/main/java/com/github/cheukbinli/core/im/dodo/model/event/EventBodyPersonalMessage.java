package com.github.cheukbinli.core.im.dodo.model.event;

import com.github.cheukbinli.core.im.dodo.model.basic.MessageBodyText;
import com.github.cheukbinli.core.im.dodo.model.basic.MessageModelPersonal;
import lombok.Data;

/***
 * 私信事件
 */
@Data
public class EventBodyPersonalMessage extends EventSubjectModel<EventBodyPersonalMessage.EventBodyPersonalMessageBody> {

    @Data
    public static class EventBodyPersonalMessageBody {
        private String islandSourceId;//	string	来源群ID
        private String dodoSourceId;//	string	来源DoDoID
        private MessageModelPersonal personal;//	object	个人信息
        private String messageId;//	string	消息ID
        private int messageType;//	int	消息类型，1：文字消息，2：图片消息，3：视频消息
        private MessageBodyText messageBody;//	object	消息内容
    }


}
