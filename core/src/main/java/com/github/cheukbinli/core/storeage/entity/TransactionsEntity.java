package com.github.cheukbinli.core.storeage.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/***
 * 流水表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class TransactionsEntity implements BaseEntity {

    private long id;
    private long userId;
    private long nid;
    private String platformUserId;
    private long tradeId;
    private String tradeName;
    private long time;
    private long activityId;

}
