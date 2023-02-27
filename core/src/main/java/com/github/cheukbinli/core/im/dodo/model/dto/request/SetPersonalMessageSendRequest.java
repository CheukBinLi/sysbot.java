package com.github.cheukbinli.core.im.dodo.model.dto.request;

import lombok.Data;
import lombok.experimental.Accessors;

/***
 * 私信API
 * 发送私信
 */
@Data
@Accessors(chain = true)
public class SetPersonalMessageSendRequest {

    private String islandSourceId;//	string	是	来源群ID，可自行指定，亦可从私信事件中获取
    private String dodoSourceId;//	string	是	DoDoID
    private int messageType = 1;//	int	是	消息类型，1：文字消息，2：图片消息，3：视频消息
    private MessageBodyTextRequest messageBody;//   	object	是	消息内容


}
