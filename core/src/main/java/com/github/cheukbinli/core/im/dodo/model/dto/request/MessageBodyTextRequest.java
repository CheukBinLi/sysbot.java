package com.github.cheukbinli.core.im.dodo.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MessageBodyTextRequest implements Serializable {

    private String content;//	string	是	文字内容，限制10000个字符，频道文字消息支持多种 消息语法
}
