package com.github.cheukbinli.core.ns.command;

import java.util.List;
import java.util.function.Function;

public interface BaseCommand {

    String WRITE_END_CODE = "\r\n";
    char READ_END_CODE = '\n';
    byte[] BLANK_BYTE = new byte[0];
    int POINTER_PLACEHOLDER_SIZE = 16;

    default String getWriteEndCode() {
        return WRITE_END_CODE;
    }

    default char getReadEndCode() {
        return READ_END_CODE;
    }

    default int getPointerPlaceholderSize() {
        return POINTER_PLACEHOLDER_SIZE;
    }

    default byte[] getBlankByte() {
        return BLANK_BYTE;
    }

    byte[] read(int size);

    byte[] read(char breakCode);

    default int converReadSize(int size) {
        return size * 2 + 1;
    }

    /***
     *
     * @param breakCode 结束符
     * @param sectionSize 分割数量 必须能被2整除
     * @param section  input:hexbyte数据，out:正常byte数据
     */
    void read(char breakCode, int sectionSize, Function<byte[], byte[]> section);

    /***
     *
     * @param breakCode 结束符
     * @param sectionSize  分段大小
     */
    List<byte[]> read(char breakCode, int sectionSize);

    void write(byte[] data);

    void write(String command);

    byte[] hexByteToByte(byte[] data);

    long hexByteToLong(byte[] data);

    String byteToHexString(byte[] data);

    default byte[] encode(String content) {
        if (null == content || content.length() < 1) {
            return BLANK_BYTE;
        }
        return content.endsWith(getWriteEndCode()) ? content.getBytes() : (content + getWriteEndCode()).getBytes();
    }

    default String longArrayToHexString(long... pointer) {
        if (pointer.length == 1) {
            return "0x" + Long.toHexString(pointer[0]);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pointer.length; i++) {
            sb.append(" ").append("0x").append(Long.toHexString(pointer[i]));
        }
        return sb.substring(1);
    }

    enum ConfigureEnum {
        mainLoopSleepTime,
        buttonClickSleepTime,
        echoCommands,
        printDebugResultCodes,
        keySleepTime,
        fingerDiameter,
        parseStringToInt,
        freezeRate,
        controllerType;
    }

    /***
     *     设置参数
     *      configure <mainLoopSleepTime or buttonClickSleepTime> <time in ms>
     */
    default void configure(ConfigureEnum configureEnum, String value) throws CommectionException {
        write("detachController");
    }

    /***
     *      获取标题ID
     *		Commands::MetaData meta = Commands::getMetaData();
     * 		printf("%016lX\n", meta.titleID);
     * @return
     */
    default byte[] getTitleID() throws CommectionException {
        write("getTitleID");
        return hexByteToByte(read(getReadEndCode()));
    }

    /***
     *      获取语言
     *		setInitialize();
     * 		u64 languageCode = 0;
     * 		SetLanguage language = SetLanguage_ENUS;
     * 		setGetSystemLanguage(&languageCode);
     * 		setMakeLanguage(languageCode, &language);
     * 		printf("%d\n", language);
     * @return
     */
    default byte[] getSystemLanguage() throws CommectionException {
        write("getSystemLanguage");
        return hexByteToByte(read(getReadEndCode()));
    }

    /***
     *      屏幕截图
     *      u64 bSize = 0x7D000;
     * @return
     */
    default byte[] pixelPeek() throws CommectionException {
        write("pixelPeek");
        return hexByteToByte(read(getReadEndCode()));
    }

    /***
     *      获取插件版本
     * @return
     */
    default byte[] getVersion() throws CommectionException {
        write("getVersion");
        return hexByteToByte(read(getReadEndCode()));
    }

    /***
     *
     * @return
     */
    default void charge() throws CommectionException {
        write("charge");
    }

}
