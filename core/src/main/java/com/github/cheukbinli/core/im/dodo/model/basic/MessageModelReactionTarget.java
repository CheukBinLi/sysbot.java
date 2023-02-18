package com.github.cheukbinli.core.im.dodo.model.basic;

import lombok.Data;

/***
 * 反应对象
 */
@Data
public class MessageModelReactionTarget {

    private String type;//	int	对象类型，0：消息
    private String id;//	string	对象ID，若对象类型为0，则代表消息ID


}
