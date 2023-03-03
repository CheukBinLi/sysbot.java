package com.github.cheukbinli.core.ns.command.impl;

import com.github.cheukbinli.core.ns.command.CommectionException;
import com.github.cheukbinli.core.ns.command.ConnectionApi;
import lombok.Data;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Data
public class DefaultCommandApi implements CommandApi {

    final ConnectionApi connectionApi;

    public DefaultCommandApi(ConnectionApi connectionApi) {
        this.connectionApi = connectionApi;
    }

    @Override
    public byte[] read(int size) {
        byte[] result = new byte[size];
        connectionApi.read(result);
        return result;
    }

    @Override
    public byte[] read(char breakCode) {
        int code;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while ((code = connectionApi.read()) != (int) breakCode) {
            out.write(code);
        }
        return out.toByteArray();
    }

    @Override
    public void read(char breakCode, int sectionSize, Function<byte[], byte[]> section) {
//        if (sectionSize % 2 != 0) {
//            throw new CommectionException("sectionSize：必须能被2整除。");
//        }
        sectionSize = sectionSize % 2 == 0 ? sectionSize : (sectionSize + 1);
        final List<byte[]> result = new ArrayList<>();
        connectionApi.read(breakCode, sectionSize, bytes -> {
            section.apply(bytes);
            return null;
        });
    }

    @Override
    public List<byte[]> read(char breakCode, int sectionSize) {
//        if (sectionSize % 2 != 0) {
//            throw new CommectionException("sectionSize：必须能被2整除。");
//        }
        sectionSize = sectionSize % 2 == 0 ? sectionSize : (sectionSize + 1);
        final List<byte[]> result = new ArrayList<>();
        connectionApi.read(breakCode, sectionSize, bytes -> {
            result.add(hexByteToByte(bytes));
            return null;
        });
        return result;
    }

    @Override
    public void write(byte[] data) {
        connectionApi.write(data);
    }

    @Override
    public void write(String command) {
        connectionApi.write(encode(command));
    }

    @Override
    public byte[] hexByteToByte(byte[] data) {
        try {
            return Hex.decodeHex(new String(data));
        } catch (Throwable e) {
            throw new CommectionException(e);
        }
    }

    @Override
    public long hexByteToLong(byte[] data) {
        try {
            return new BigInteger(new String(data), 16).longValue();
        } catch (Throwable e) {
            throw new CommectionException(e);
        }
    }

    @Override
    public String byteToHexString(byte[] data) {
        try {
            return Hex.encodeHexString(data);
        } catch (Throwable e) {
            throw new CommectionException(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        int i = 10;
        int x = 51;
        int p = 2;
        for (int n = 1; n < x; n++) {
            System.out.print(n + " ");
            if (p++ > i) {
                p = 2;
                Thread.sleep(50);
                System.out.println();
            }
        }
    }

}
