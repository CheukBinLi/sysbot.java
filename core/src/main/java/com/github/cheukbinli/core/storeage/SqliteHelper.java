package com.github.cheukbinli.core.storeage;

import com.github.cheukbinli.core.storeage.entity.BaseEntity;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class SqliteHelper {

    final static Logger logger = LoggerFactory.getLogger(SqliteHelper.class);

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private String dbFilePath;
    static SqliteHelper instance;
    public static String DB_NAME = "data";
    private static final Map<String, Map<String, Field>> FIELD_CACHE = new ConcurrentHashMap<>();

    public static SqliteHelper getInstance() {
        if (null == instance) {
            synchronized (SqliteHelper.class) {
                if (null == instance) {
                    try {
                        System.out.println(InitTable.getDbPatch(DB_NAME));
                        File db = new File(InitTable.getDbPatch(DB_NAME));
                        if (!db.exists()) {
//                            InputStream in = SqliteHelper.class.getResourceAsStream("/data");
                            InputStream in = SqliteHelper.class.getClassLoader().getResourceAsStream(DB_NAME);
                            FileOutputStream out = new FileOutputStream(db);
//                            db.createNewFile();
                            int code;
                            while ((code = in.read()) != -1) {
                                out.write(code);
                            }
                            out.close();
                            in.close();
                        }
                        instance = new SqliteHelper(InitTable.getDbPatch(DB_NAME));
                        InitTable.init(instance);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return instance;
    }

    /**
     * ????????????
     *
     * @param dbFilePath sqlite db ????????????
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private SqliteHelper(String dbFilePath) throws ClassNotFoundException, SQLException {
        this.dbFilePath = dbFilePath;
        connection = getConnection(dbFilePath);
    }

    /**
     * ?????????????????????
     *
     * @param dbFilePath db????????????
     * @return ???????????????
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Connection getConnection(String dbFilePath) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
//        conn = DriverManager.getConnection("jdbc:sqlite::resource:" + dbFilePath);
        return conn;
    }
//
//    /**
//     * ??????sql??????
//     *
//     * @param sql sql select ??????
//     * @param rse ????????????????????????
//     * @return ????????????
//     * @throws SQLException
//     * @throws ClassNotFoundException
//     */
//    public <T> T executeQuery(String sql, ResultSetExtractor<T> rse) throws SQLException, ClassNotFoundException {
//        try {
//            resultSet = getStatement().executeQuery(sql);
//            T rs = rse.extractData(resultSet);
//            return rs;
//        } finally {
//            destroyed();
//        }
//    }
//
//    /**
//     * ??????select???????????????????????????
//     *
//     * @param sql sql select ??????
//     * @param rm  ????????????????????????????????????
//     * @return
//     * @throws SQLException
//     * @throws ClassNotFoundException
//     */
//    public <T> List<T> executeQuery(String sql, RowMapper<T> rm) throws SQLException, ClassNotFoundException {
//        List<T> rsList = new ArrayList<T>();
//        try {
//            resultSet = getStatement().executeQuery(sql);
//            while (resultSet.next()) {
//                rsList.add(rm.mapRow(resultSet, resultSet.getRow()));
//            }
//        } finally {
//            destroyed();
//        }
//        return rsList;
//    }

    /**
     * ?????????????????????sql??????
     *
     * @param sql
     * @return ????????????
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int executeUpdate(String sql) throws SQLException, ClassNotFoundException {
        try {
            int c = getStatement().executeUpdate(sql);
            return c;
        } finally {
            destroyed();
        }

    }

    /**
     * ????????????sql????????????
     *
     * @param sqls
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void executeUpdate(String... sqls) throws SQLException, ClassNotFoundException {
        try {
            for (String sql : sqls) {
                getStatement().executeUpdate(sql);
            }
        } finally {
            destroyed();
        }
    }

    /**
     * ????????????????????? sql List
     *
     * @param sqls sql??????
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void executeUpdate(List<String> sqls) throws SQLException, ClassNotFoundException {
        try {
            for (String sql : sqls) {
                getStatement().executeUpdate(sql);
            }
        } finally {
            destroyed();
        }
    }

    private Connection getConnection() throws ClassNotFoundException, SQLException {
        if (null == connection) connection = getConnection(dbFilePath);
        return connection;
    }

    private Statement getStatement() throws SQLException, ClassNotFoundException {
        if (null == statement) statement = getConnection().createStatement();
        return statement;
    }

    /**
     * ??????????????????????????????
     */
    public void destroyed() {
        try {
            if (null != statement) {
                statement.close();
                statement = null;
            }

            if (null != connection) {
                connection.close();
                connection = null;
            }

            if (null != resultSet) {
                resultSet.close();
                resultSet = null;
            }
        } catch (SQLException e) {
            logger.error("Sqlite????????????????????????", e);
        }
    }

    /**
     * ??????select???????????????????????????
     *
     * @param sql   sql select ??????
     * @param clazz ????????????
     * @return ????????????
     * @throws SQLException           ????????????
     * @throws ClassNotFoundException ????????????
     */
    public <T> List<T> executeQueryList(String sql, Class<T> clazz) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        List<T> rsList = new ArrayList<T>();
        try {
            resultSet = getStatement().executeQuery(sql);
            while (resultSet.next()) {
                rsList.add(fillValue(resultSet, clazz));
            }
        } finally {
            destroyed();
        }
        return rsList;
    }

    /**
     * ??????sql??????,?????????????????????
     *
     * @param sql   sql select ??????
     * @param clazz ????????????????????????
     * @return ????????????
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public <T> T executeQuery(String sql, Class<T> clazz) throws SQLException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        try {
            resultSet = getStatement().executeQuery(sql);
            resultSet.next();
            return fillValue(resultSet, clazz);
        } finally {
            destroyed();
        }
    }

    Map<String, Field> getFields(Class c, boolean cache) {
        return findField(c, null, cache);
    }

    /***
     *
     * @param clazz
     * @param function  ??????????????????
     * @param cache
     * @return
     */
    Map<String, Field> findField(Class clazz, Function<Field, String> function, boolean cache) {
        Map<String, Field> result = null;
        if (cache) {
            result = FIELD_CACHE.get(clazz.getName());
            if (null != result) {
                return result;
            }
            result = new HashMap<>();
        }
        if (null == function) {
            function = new Function<Field, String>() {
                @Override
                public String apply(Field field) {
                    return field.getName();
                }
            };
        }
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            result.put(function.apply(field), field);
        }
        if (cache) {
            FIELD_CACHE.put(clazz.getName(), result);
        }
        return result;
    }

    <T> T fillValue(ResultSet resultSet, Class<T> clazz) throws InstantiationException, IllegalAccessException, SQLException {
        if (resultSet.getRow() < 1) {
            return null;
        }
        Map<String, Field> fields = FIELD_CACHE.get(clazz.getName());
        T result = clazz.newInstance();
        if (null == fields) {
            fields = new HashMap<>();
            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                fields.put(field.getName(), field);
                Object value = resultSet.getObject(field.getName());
                if (null == value) {
                    continue;
                }
                field.set(result, value);
            }
        } else {
            fields.forEach((k, v) -> {
                try {
                    Object value = resultSet.getObject(k);
                    if (null == value) {
                        return;
                    }
                    v.set(result, value);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return result;

    }


    /**
     * ?????????????????????sql??????
     * <p>
     * //     * @param tableName ??????
     * //     * @param param     key-value?????????,key:???????????????,value:???
     *
     * @return ????????????
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @SneakyThrows
    public int executeInsert(BaseEntity baseEntity) throws SQLException, ClassNotFoundException {

        Map<String, Field> param = getFields(baseEntity.getClass(), true);

        try {
            StringBuffer sql = new StringBuffer();
            sql.append("INSERT INTO ");
            sql.append(baseEntity.getTableName());
            sql.append(" ( ");
            for (String key : param.keySet()) {
                sql.append(key);
                sql.append(",");
            }
            sql.delete(sql.length() - 1, sql.length());
            sql.append(")  VALUES ( ");
            for (String key : param.keySet()) {
                if ("id".equals(key)) {
                    sql.append("null,");
                } else {
                    sql.append("'");
                    sql.append(param.get(key).get(baseEntity));
                    sql.append("',");
                }
            }
            sql.delete(sql.length() - 1, sql.length());
            sql.append(");");
            int c = getStatement().executeUpdate(sql.toString());
            return c;
        } finally {
            destroyed();
        }
    }

    @SneakyThrows
    public int executeUpdate(BaseEntity baseEntity) throws SQLException, ClassNotFoundException {

        Map<String, Field> param = getFields(baseEntity.getClass(), true);

        try {
            StringBuffer sql = new StringBuffer();
            param.forEach((k, v) -> {
                if ("id".equals(k)) {
                    return;
                }
                try {
                    Object value = v.get(baseEntity);
//                    boolean isString = v.getType().isAssignableFrom(String.class);
                    String str = String.class == v.getType() ? "'" : "";
                    if (null != value) {
                        sql.append(",").append(k).append("=").append(str).append(v.get(baseEntity)).append(str);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            });
            sql.append(" WHERE id=").append(baseEntity.getId());

            int c = getStatement().executeUpdate(
                    "UPDATE " +
                            baseEntity.getTableName() +
                            " SET " +
                            sql.substring(1)
            );
            return c;
        } finally {
            destroyed();
        }
    }
}
