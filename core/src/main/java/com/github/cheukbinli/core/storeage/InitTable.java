package com.github.cheukbinli.core.storeage;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public interface InitTable {

    String SQLITE_MASTER = "create table main.sqlite_master\n" +
            "(\n" +
            "    type     TEXT,\n" +
            "    name     TEXT,\n" +
            "    tbl_name TEXT,\n" +
            "    rootpage INT,\n" +
            "    sql      TEXT\n" +
            ");\n" +
            "\n";
    String SQLITE_SEQUENCE = "create table main.sqlite_sequence\n" +
            "(\n" +
            "    name,\n" +
            "    seq\n" +
            ");\n" +
            "\n";
    String TRANSACTIONS = "create table main.transactions\n" +
            "(\n" +
            "    userId         integer,\n" +
            "    nid            integer,\n" +
            "    platformUserId TEXT,\n" +
            "    tradeId        integer,\n" +
            "    tradeName      TEXT,\n" +
            "    time           REAL INTEGER,\n" +
            "    activityId     integer,\n" +
            "    id             integer default 1\n" +
            "        constraint transactions_pk\n" +
            "            primary key autoincrement\n" +
            ");\n" +
            "\n";
    String USER = "create table main.user\n" +
            "(\n" +
            "    id               integer default 1\n" +
            "        constraint user_pk\n" +
            "            primary key autoincrement,\n" +
            "    nid              integer,\n" +
            "    userName         TEXT,\n" +
            "    platformUserId   TEXT,\n" +
            "    platformUserName TEXT,\n" +
            "    level            integer,\n" +
            "    isEnable         INT\n" +
            ");\n" +
            "\n";

    static String getDbPatch(String dbName) {
        return System.getProperty("user.dir") + File.separator + dbName;
    }

    static void init(SqliteHelper sqliteHelper) throws IOException, SQLException, ClassNotFoundException {
//        sqliteHelper.executeUpdate(SQLITE_MASTER, SQLITE_SEQUENCE, TRANSACTIONS, USER);
    }

    public static void main(String[] args) {
        System.out.println(System.getProperty("user.dir"));
    }
}
