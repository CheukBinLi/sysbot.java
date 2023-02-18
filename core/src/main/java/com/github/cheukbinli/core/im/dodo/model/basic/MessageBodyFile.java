package com.github.cheukbinli.core.im.dodo.model.basic;

import lombok.Data;

@Data
public class MessageBodyFile extends MessageBodyBase {

    private static final long serialVersionUID = -9136524606121764441L;

    private String url;//	string	是	文件链接
    private String name;//	string	是	文件名称
    private String size;//	long	是	文件大小


}
