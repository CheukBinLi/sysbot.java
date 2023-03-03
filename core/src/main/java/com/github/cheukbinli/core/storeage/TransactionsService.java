package com.github.cheukbinli.core.storeage;

import com.github.cheukbinli.core.storeage.entity.TransactionsEntity;

import java.sql.SQLException;
import java.util.List;

public class TransactionsService {

    SqliteHelper sqliteHelper = SqliteHelper.getInstance();

    public int add(TransactionsEntity userEntity) throws SQLException, ClassNotFoundException {
        return sqliteHelper.executeInsert(userEntity);
    }

    public String getCondiction(TransactionsEntity entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(entity.getTableName()).append(" WHERE 1=1 ");
        if (entity.getId() != 0) {
            sql.append("AND id=").append(entity.getId());
        }
        if (entity.getUserId() != 0) {
            sql.append("AND userId=").append(entity.getNid());
        }
        if (entity.getNid() != 0) {
            sql.append("AND nid=").append(entity.getNid());
        }
//        if (entity.getPlatformUserId() != 0) {
//            sql.append("AND platformUserId=").append(entity.getPlatformUserId());
//        }
        if (entity.getActivityId() != 0) {
            sql.append("AND activityId=").append(entity.getPlatformUserId());
        }
        return sql.toString();
    }

    public TransactionsEntity find(TransactionsEntity userEntity) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return sqliteHelper.executeQuery(getCondiction(userEntity), TransactionsEntity.class);
    }

    public List<TransactionsEntity> findList(TransactionsEntity userEntity) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return sqliteHelper.executeQueryList(getCondiction(userEntity), TransactionsEntity.class);
    }

}
