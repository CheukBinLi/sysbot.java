package com.github.cheukbinli.core.im.dodo.model;

import com.github.cheukbinli.core.im.dodo.model.basic.MessageModelEmoji;
import com.github.cheukbinli.core.im.dodo.model.basic.MessageModelMember;
import com.github.cheukbinli.core.im.dodo.model.basic.MessageModelPersonal;
import com.github.cheukbinli.core.im.dodo.model.basic.MessageModelReactionTarget;
import lombok.Data;

@Data
public class MessageReactionEvent {

    private String islandSourceId;//	string	来源群ID
    private String channelId;//	string	来源频道ID
    private String dodoSourceId;//	string	来源DoDoID
    private String messageId;//	string	来源消息ID
    private MessageModelPersonal personal;//	object	个人信息
    private MessageModelMember member;//	object	成员信息
    private MessageModelReactionTarget reactionTarget;//	object	反应对象
    private MessageModelEmoji reactionEmoji;//	object	反应表情
    private int reactionType;//	int	反应类型，0：删除，1：新增


}
