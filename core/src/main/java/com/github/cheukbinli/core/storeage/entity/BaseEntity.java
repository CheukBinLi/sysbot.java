package com.github.cheukbinli.core.storeage.entity;

public interface BaseEntity {

    long getId();

    default String getTableName() {
        return (getClass().getSimpleName().charAt(0) + "").toLowerCase() + getClass().getSimpleName().substring(1).replace("Entity", "");
    }

}
