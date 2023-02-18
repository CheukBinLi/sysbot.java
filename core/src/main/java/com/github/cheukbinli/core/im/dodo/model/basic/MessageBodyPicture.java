package com.github.cheukbinli.core.im.dodo.model.basic;

import lombok.Data;

/***
 * 图片消息
 */
@Data
public class MessageBodyPicture extends MessageBodyBase {

    private static final long serialVersionUID = -3901284514786974214L;
    private String url;//	string	是	图片链接，必须是官方的链接，通过上传资源图片接口可获得图片链接
    private int width;//	int	是	图片宽度
    private int height;//	int	是	图片高度
    private int isOriginal;//	int	否	是否原图，0：压缩图，1：原图


}
