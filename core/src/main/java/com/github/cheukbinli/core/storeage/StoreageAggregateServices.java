package com.github.cheukbinli.core.storeage;

import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.storeage.entity.TransactionsEntity;
import com.github.cheukbinli.core.storeage.entity.UserEntity;
import lombok.Data;

import java.sql.SQLException;
import java.util.List;

@Data
public class StoreageAggregateServices {

    TransactionsService transactionsService = new TransactionsService();
    UserService userService = new UserService();
    TransactionLogService transactionLogService = new TransactionLogService();

    public UserEntity addUser(UserEntity userEntity) {
        try {
            userEntity.setTransactionTime(System.currentTimeMillis());
            do {
                UserEntity user = userService.find(new UserEntity().setNid(userEntity.getNid()));
                if (null != user) {
                    userService.update(userEntity.setId(user.getId()));
                    return user;
                }
                userService.add(userEntity);
                GlobalLogger.append(String.format("添加用户：%s 平台I：%s NID：%s", userEntity.getUserName(), userEntity.getPlatformUserId(), userEntity.getNid()));
            }
            while (true);
        } catch (Exception e) {
            e.printStackTrace();
            GlobalLogger.append(e);
        }
        return null;
    }

    public List<UserEntity> findUser(UserEntity userEntity) {
        try {
            return userService.findList(userEntity);
        } catch (Exception e) {
            e.printStackTrace();
            GlobalLogger.append(e);
        }
        return null;
    }


    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        StoreageAggregateServices s = new StoreageAggregateServices();
        UserEntity userEntity = s.addUser(new UserEntity().setUserName("A").setNid(-11).setPlatformUserName("mma").setPlatformUserId("cc11"));


        s.getTransactionsService().add(new TransactionsEntity()
                .setUserId(userEntity.getId())
                .setNid(userEntity.getNid())
                .setPlatformUserId(userEntity.getPlatformUserId())
                .setActivityId(99941)
                .setTime(System.currentTimeMillis())
        );

    }
}
