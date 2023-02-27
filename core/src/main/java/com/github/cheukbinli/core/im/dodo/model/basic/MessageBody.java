package com.github.cheukbinli.core.im.dodo.model.basic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MessageBody implements Serializable {

    private String content;//	string	是	文字内容，限制10000个字符，频道文字消息支持多种 消息语法
    private ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
    //	string	是	文件链接
    //	string	是	图片链接，必须是官方的链接，通过上传资源图片接口可获得图片链接
    private String url;
    /***
     * file
     */
    private static final long serialVersionUID = 4321870832796372972L;
    private String name;//	string	是	文件名称
    private int size;//	long	是	文件大小

    /***
     * picture
     */
    private int width;//	int	是	图片宽度
    private int height;//	int	是	图片高度
    private int isOriginal;//	int	否	是否原图，0：压缩图，1：原图
}
