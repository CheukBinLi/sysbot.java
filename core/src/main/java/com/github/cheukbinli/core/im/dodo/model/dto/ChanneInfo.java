package com.github.cheukbinli.core.im.dodo.model.dto;

import lombok.Data;

/***
 * 频道列表信息
 */
@Data
public class ChanneInfo {

    private String channelId;//	string	频道ID
    private String channelName;//	string	频道名称
    private int channelType;//	int	频道类型，1：文字频道，2：语音频道，4：帖子频道，5：链接频道，6：资料频道
    private int defaultFlag;//	int	默认访问频道标识，0：否，1：是
    private String groupId;//	string	分组ID
    private String groupName;//	string	分组名称
    private boolean vip;
}
