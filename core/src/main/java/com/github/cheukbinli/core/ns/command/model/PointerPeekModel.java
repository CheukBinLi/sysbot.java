package com.github.cheukbinli.core.ns.command.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class PointerPeekModel {

    /***
     * 偏移
     */
    private long[] jumps;
    /***
     * 大小
     */
    private int size;
    /***
     * 数据
     */
    private byte[] data;

}
