package com.github.cheukbinli.core.storeage;

import com.github.cheukbinli.core.storeage.entity.UserEntity;

import java.sql.SQLException;
import java.util.List;

public class UserService {

    SqliteHelper sqliteHelper = SqliteHelper.getInstance();

    public int add(UserEntity userEntity) throws SQLException, ClassNotFoundException {
        return sqliteHelper.executeInsert(userEntity);
    }

    public int update(UserEntity userEntity) throws SQLException, ClassNotFoundException {
        return sqliteHelper.executeUpdate(userEntity);
    }

    public String getCondiction(UserEntity userEntity) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM ").append(userEntity.getTableName()).append(" WHERE 1=1 ");
        if (userEntity.getId() != 0) {
            sql.append("AND id=").append(userEntity.getId());
        }
        if (userEntity.getNid() != 0) {
            sql.append("AND nid=").append(userEntity.getNid());
        }
//        if (userEntity.getPlatformUserId() != 0) {
//            sql.append("AND platformUserId=").append(userEntity.getPlatformUserId());
//        }
        return sql.toString();
    }

    public UserEntity find(UserEntity userEntity) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return sqliteHelper.executeQuery(getCondiction(userEntity), UserEntity.class);
    }

    public List<UserEntity> findList(UserEntity userEntity) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        return sqliteHelper.executeQueryList(getCondiction(userEntity), UserEntity.class);
    }

}
