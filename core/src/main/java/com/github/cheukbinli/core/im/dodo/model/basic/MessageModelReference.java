package com.github.cheukbinli.core.im.dodo.model.basic;

import lombok.Data;

/***
 * 回复信息
 */
@Data
public class MessageModelReference {

    private String messageId;//	string	被回复消息ID
    private String dodoSourceId;//	string	被回复者DoDoID
    private String nickName;//	string	被回复者群昵称


}
