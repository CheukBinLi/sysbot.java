package com.github.cheukbinli.core.ns.command;

import java.util.function.Function;

public interface ConnectionApi {

    void read(final byte[] buffer) throws CommectionException;

    int read() throws CommectionException;

    void read(char breakCode, int sectionSize, Function<byte[], byte[]> section) throws CommectionException;

    void write(byte[] buffer, int offset, int len) throws CommectionException;

    void write(byte[] buffer) throws CommectionException;

    void write(String content) throws CommectionException;

    void write(int b) throws CommectionException;

}
