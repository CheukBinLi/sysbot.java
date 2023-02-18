package com.github.cheukbinli.core.im.dodo.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


/***
 * 鉴权方式
 * 在HTTP请求头上添加Authorization，值为Bot clientId.token，示例Bot 88888888.NjM2OTM0NDg.au-_ve-_vSs.Scs7Y_y8Aw3shbzQ7aUcbz0kAnSsE7pXWHM6Ww6VvNY
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Authorization {

    private String islandSourceId;
    private String clientId;//	机器人唯一标识	88888888
    private String token;//	机器人鉴权Token	NjM2OTM0NDg.au-_ve-_vSs.Scs7Y_y8Aw3shbzQ7aUcbz0kAnSsE7pXWHM6Ww6VvNY


}
