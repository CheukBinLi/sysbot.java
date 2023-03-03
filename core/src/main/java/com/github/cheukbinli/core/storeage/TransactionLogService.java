package com.github.cheukbinli.core.storeage;

import com.github.cheukbinli.core.storeage.entity.TransactionLogEntity;

import java.sql.SQLException;
import java.util.List;

public class TransactionLogService {

    SqliteHelper sqliteHelper = SqliteHelper.getInstance();

    public int add(TransactionLogEntity transactionLogEntity) throws SQLException, ClassNotFoundException {
        return sqliteHelper.executeInsert(transactionLogEntity);
    }

    public String getCondiction(TransactionLogEntity entity) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(entity.getTableName()).append(" WHERE 1=1 ");
        if (entity.getId() != 0) {
            sql.append("AND id=").append(entity.getId());
        }
        if (entity.getUser() != 0) {
            sql.append("AND user=").append(entity.getUser());
        }
        if (entity.getNid() != 0) {
            sql.append("AND nid=").append(entity.getNid());
        }
//        if (entity.getPlatformUserId() != 0) {
//            sql.append("AND platformUserId=").append(entity.getPlatformUserId());
//        }
        return sql.toString();
    }

    public TransactionLogEntity find(TransactionLogEntity transactionLogEntity) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return sqliteHelper.executeQuery(getCondiction(transactionLogEntity), TransactionLogEntity.class);
    }

    public List<TransactionLogEntity> findList(TransactionLogEntity transactionLogEntity) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return sqliteHelper.executeQueryList(getCondiction(transactionLogEntity), TransactionLogEntity.class);
    }

}
