package com.github.cheukbinli.core.im.dodo.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/***
 * 事件API
 * 获取WebSocket连接
 * GetWebSocketConnection
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetWebSocketConnection {

    private int status;//	int	返回码
    private String message;//	string	返回信息
    private Data data;//	object	返回数据

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String endpoint;//	string	连接节点
    }


}
