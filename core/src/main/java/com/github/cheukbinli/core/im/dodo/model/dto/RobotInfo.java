package com.github.cheukbinli.core.im.dodo.model.dto;

@lombok.Data
public class RobotInfo {
    private String clientId;//	string	机器人唯一标识
    private String dodoSourceId;//	string	机器人DoDoID
    private String nickName;//	string	机器人昵称
    private String avatarUrl;//	string	机器人头像
}
