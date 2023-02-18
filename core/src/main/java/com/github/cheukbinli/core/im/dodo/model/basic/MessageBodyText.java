package com.github.cheukbinli.core.im.dodo.model.basic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MessageBodyText extends MessageBodyBase {

    private static final long serialVersionUID = -8128990867229390844L;
    private String content;//	string	是	文字内容，限制10000个字符，频道文字消息支持多种 消息语法

}
