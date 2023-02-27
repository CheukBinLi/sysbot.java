package com.github.cheukbinli.core.im.dodo.model.event;

import com.github.cheukbinli.core.im.dodo.model.basic.MessageBody;
import com.github.cheukbinli.core.im.dodo.model.basic.MessageModelMember;
import com.github.cheukbinli.core.im.dodo.model.basic.MessageModelPersonal;
import com.github.cheukbinli.core.im.dodo.model.basic.MessageModelReference;
import lombok.Data;

@Data
public class EventBodyChannelMessage extends EventSubjectModel<EventBodyChannelMessage.EventBodyChannelMessageBody> {

    @Data
    public static class EventBodyChannelMessageBody {
        private String islandSourceId;//	string	来源群ID
        private String channelId;//	string	来源频道ID
        private String dodoSourceId;//	string	来源DoDoID
        private String messageId;//	string	消息ID
        private MessageModelPersonal personal;//	object	个人信息
        private MessageModelMember member;//	object	成员信息
        private MessageModelReference reference;//	object	回复信息
        private int messageType;//	int	消息类型，1：文字消息，2：图片消息，3：视频消息，4：分享消息，5：文件消息，6：卡片消息，7：红包消息
        private MessageBody messageBody;//	object	消息内容

        public boolean isFile() {
            return messageType == 5;
        }

        public boolean isText() {
            return messageType == 1;
        }
    }


}
