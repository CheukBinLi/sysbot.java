package com.github.cheukbinli.core.storeage.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/***
 * 用户表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class UserEntity implements BaseEntity {

    private long id;
    private long nid;
    private String userName;
    private String platformUserId;
    private String platformUserName;
    private int level;
    private int isEnable;

}
