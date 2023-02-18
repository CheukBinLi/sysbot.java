package com.github.cheukbinli.core.im.dodo.model;

import com.github.cheukbinli.core.im.dodo.model.basic.*;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class DodoMessageInfoModel {

    private String islandSourceId;//	string	来源群ID
    private String channelId;//	string	来源频道ID
    private String dodoSourceId;//	string	来源DoDoID
    private String[] at = new String[0];//@
    private MessageModelPersonal personal;//	object	个人信息
    private MessageModelMember member;//	object	成员信息
    private MessageModelReference reference;//	object	回复信息
    private int messageType;//	int	消息类型，1：文字消息，2：图片消息，3：视频消息，4：分享消息，5：文件消息，6：卡片消息，7：红包消息
    private MessageBodyText messageBody;//	object	消息内容
    private MessageBodyPicture messageBodyPicture;//	object	消息内容
    private MessageBodyFile messageBodyFile;//	object	消息内容

    public DodoMessageInfoModel setAt(String... at) {
        this.at = at;
        return this;
    }

}
