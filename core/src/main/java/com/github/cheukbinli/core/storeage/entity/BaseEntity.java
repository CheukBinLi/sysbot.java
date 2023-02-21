package com.github.cheukbinli.core.storeage.entity;

public interface BaseEntity {

    default String getTableName() {
        return (getClass().getSimpleName().charAt(0) + "").toLowerCase() + getClass().getSimpleName().substring(1).replace("Entity", "");
    }

}
