package com.github.cheukbinli.core.util;

import java.util.Random;

public class NetUtil {

    public static Random random = new Random();

    public static String getRandomNo(String prefix, int digit) {
        StringBuilder result = new StringBuilder(null == prefix ? "" : prefix);
        for (int i = 0; i < digit; i++) {
            result.append(random.nextInt(10));
        }
        return result.toString();
    }

    public static byte[] intToByteArray(int a) {
        return new byte[]{(byte) ((a >> 24) & 0xFF), (byte) ((a >> 16) & 0xFF), (byte) ((a >> 8) & 0xFF), (byte) (a & 0xFF)};
    }

    public static byte[] littleEndianIntToByteArray(int a) {
        return new byte[]{(byte) (a & 0xFF), (byte) ((a >> 8) & 0xFF), (byte) ((a >> 16) & 0xFF), (byte) ((a >> 24) & 0xFF)};
    }

    public static int byteArrayToInt(byte[] b) {
        return b[3] & 0xFF | (b[2] & 0xFF) << 8 | (b[1] & 0xFF) << 16 | (b[0] & 0xFF) << 24;
    }

    public static byte[] LongToBytes(long values) {
        byte[] buffer = new byte[8];
        for (int i = 0; i < 8; i++) {
            int offset = 64 - (i + 1) * 8;
            buffer[i] = (byte) ((values >> offset) & 0xff);
        }
        return buffer;
    }

    public static long BytesToLong(byte[] buffer) {
        long values = 0;
        for (int i = 0; i < 8; i++) {
            values <<= 8;
            values |= (buffer[i] & 0xff);
        }
        return values;
    }
}
