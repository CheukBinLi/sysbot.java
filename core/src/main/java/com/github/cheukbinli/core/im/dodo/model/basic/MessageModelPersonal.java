package com.github.cheukbinli.core.im.dodo.model.basic;

import lombok.Data;

/***
 * 个人信息
 */
@Data
public class MessageModelPersonal {

    private String nickName;//	string	DoDo昵称
    private String avatarUrl;//	string	头像
    private int sex;//	int	性别，-1：保密，0：女，1：男


}
