package com.github.cheukbinli.core.storeage.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/***
 * 交换日志
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TransactionLogEntity implements BaseEntity {

    private long id;
    private long nid;
    private long user;
    private String userName;
    private String pkm;
    private String pkmName;
    private long createTime;

}
