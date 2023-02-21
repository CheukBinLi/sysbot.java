package com.github.cheukbinli.core.storeage;

import com.github.cheukbinli.core.storeage.entity.TransactionsEntity;
import com.github.cheukbinli.core.storeage.entity.UserEntity;

import java.sql.SQLException;
import java.util.List;

public class TransactionsService {

    SqliteHelper sqliteHelper = SqliteHelper.getInstance();

    public int add(TransactionsEntity userEntity) throws SQLException, ClassNotFoundException {
        return sqliteHelper.executeInsert(userEntity);
    }

    public String getCondiction(TransactionsEntity userEntity) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(userEntity.getTableName()).append(" WHERE 1=1 ");
        if (userEntity.getId() != 0) {
            sql.append("AND id=").append(userEntity.getId());
        }
        if (userEntity.getUserId() != 0) {
            sql.append("AND userId=").append(userEntity.getNid());
        }
        if (userEntity.getNid() != 0) {
            sql.append("AND nid=").append(userEntity.getNid());
        }
//        if (userEntity.getPlatformUserId() != 0) {
//            sql.append("AND platformUserId=").append(userEntity.getPlatformUserId());
//        }
        if (userEntity.getActivityId() != 0) {
            sql.append("AND activityId=").append(userEntity.getPlatformUserId());
        }
        return sql.toString();
    }

    public UserEntity find(TransactionsEntity userEntity) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return sqliteHelper.executeQuery(getCondiction(userEntity), UserEntity.class);
    }

    public List<UserEntity> findList(TransactionsEntity userEntity) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return sqliteHelper.executeQueryList(getCondiction(userEntity), UserEntity.class);
    }

}
