package com.github.cheukbinli.core.im.dodo.model.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

/***
 * 私信API
 * 发送私信
 */
@Data
@Accessors(chain = true)
public class SetChannelMessageSendRequest {


    private String channelId;//	string	是	频道ID
    private int messageType = 1;//	int	是	消息类型，1：文字消息，2：图片消息，3：视频消息，6：卡片消息
    private MessageBodyTextRequest messageBody;//	object	是	消息内容
    private String referencedMessageId;//	string	否	回复消息ID
    private String dodoSourceId;//	string	否	DoDoID，非必传，如果传了，则给该成员发送频道私信


}
