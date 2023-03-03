package com.github.cheukbinli.core.ns.command;

import com.github.cheukbinli.core.ns.command.model.PointerModel;
import com.github.cheukbinli.core.ns.command.model.PointerPeekModel;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.util.List;
import java.util.function.Function;

public interface MemoryCommand extends BaseCommand {

    /***
     * 	获取主内存信息
     * @return
     */
    default byte[] getMainNsoBase() throws CommectionException {
        write("getMainNsoBase");
        return hexByteToByte(read(getReadEndCode()));
    }

    default byte[] getBuildID() throws CommectionException {
        write("getBuildID");
        return hexByteToByte(read(getReadEndCode()));
    }

    /***
     * 获取堆信息
     * @return
     * @throws CommectionException
     */
    default byte[] getHeapBase() throws CommectionException {
        write("getHeapBase");
        return hexByteToByte(read(getReadEndCode()));
    }

    /***
     * 		Commands::MetaData meta = Commands::getMetaData();
     * 		u64 offset = Util::parseStringToInt(argv[1]);
     * 		u64 size = Util::parseStringToInt(argv[2]);
     * 		Commands::peek(meta.heap_base + offset, size);
     * @return
     */
    default byte[] peek(long offset, int size) throws CommectionException {
        write("peek 0x" + Long.toHexString(offset) + " " + converReadSize(size));
        return hexByteToByte(read(getReadEndCode()));
    }

    default void peek(long offset, int size, int sectionSize, Function<byte[], Boolean> sectionFunction) throws CommectionException {
        write("peek 0x" + Long.toHexString(offset) + " " + converReadSize(size));
        read(READ_END_CODE, sectionSize, new Function<byte[], byte[]>() {
            @Override
            public byte[] apply(byte[] bytes) {
                try {
                    sectionFunction.apply(Hex.decodeHex(new String(bytes)));
                } catch (DecoderException e) {
                    throw new CommectionException(e);
                }
                return bytes;
            }
        });
    }

    /***
     *      读取指针
     * 		u64 offset = Util::parseStringToInt(argv[1]);
     * 		u64 size = Util::parseStringToInt(argv[2]);
     *
     * 		Commands::peek(offset, size);
     * @return
     */
    default byte[] peekAbsolute(long pointer, int size) throws CommectionException {
        write("peekAbsolute 0x" + Long.toHexString(pointer) + " " + converReadSize(size));
        return hexByteToByte(read(getReadEndCode()));
    }

    default void peekAbsolute(long pointer, int size, int sectionSize, Function<byte[], Boolean> sectionFunction) throws CommectionException {
        write("peekAbsolute 0x" + Long.toHexString(pointer) + " " + converReadSize(size));
        read(READ_END_CODE, sectionSize,
                bytes -> {
                    try {
                        sectionFunction.apply(Hex.decodeHex(new String(bytes)));
                    } catch (DecoderException e) {
                        throw new CommectionException(e);
                    }
                    return bytes;
                });
    }

    /***
     *      主内存偏移量
     * 		Commands::MetaData meta = Commands::getMetaData();
     *
     * 		u64 offset = Util::parseStringToInt(argv[1]);
     * 		u64 size = Util::parseStringToInt(argv[2]);
     *
     * 		Commands::peek(meta.main_nso_base + offset, size);
     * @return
     */
    default byte[] peekMain(long offset, int size) throws CommectionException {
        write("peekMain 0x" + Long.toHexString(offset) + " " + converReadSize(size));
        return hexByteToByte(read(getReadEndCode()));
    }

    /***
     * @todo I DOWN KNOW DATA DECODE FORMAT;
     * 		Commands::MetaData meta = Commands::getMetaData();
     * 		u64 offset = Util::parseStringToInt(argv[1]);
     * 		u64 size = Util::parseStringToInt(argv[2]);
     * 		Commands::peek(meta.heap_base + offset, size);
     * @return
     */
    default List<PointerModel> peekMulti(List<PointerModel> pointerModels) throws CommectionException {
        StringBuilder command = new StringBuilder("peekMulti");
        pointerModels.forEach(item -> {
            command.append(" ").append("0x").append(Long.toHexString(item.getPointer())).append(" ").append(converReadSize(item.getSize()));
        });
        write(command.toString());
        return null;
    }

    /***
     * @todo I DOWN KNOW DATA DECODE FORMAT;
     * 		for (u64 i = 0; i < itemCount; ++i)
     *                {
     * 			offsets[i] = Util::parseStringToInt(argv[(i * 2) + 1]);
     * 			sizes[i] = Util::parseStringToInt(argv[(i * 2) + 2]);
     *        }
     * 		Commands::peekMulti(offsets, sizes, itemCount);
     * @return
     */
    default List<PointerModel> peekAbsoluteMulti(List<PointerModel> pointerModels) throws CommectionException {
        StringBuilder command = new StringBuilder("peekAbsoluteMulti");
        pointerModels.forEach(item -> {
            command.append(" ").append("0x").append(Long.toHexString(item.getPointer())).append(" ").append(converReadSize(item.getSize()));
        });
        write(command.toString());
        return null;
    }


    /***
     * @todo I DOWN KNOW DATA DECODE FORMAT;
     * 		u64 itemCount = (argc - 1) / 2;
     * 		u64 offsets[itemCount];
     * 		u64 sizes[itemCount];
     *
     * 		for (u64 i = 0; i < itemCount; ++i)
     *                {
     * 			offsets[i] = meta.main_nso_base + Util::parseStringToInt(argv[(i * 2) + 1]);
     * 			sizes[i] = Util::parseStringToInt(argv[(i * 2) + 2]);
     *        }
     * 		Commands::peekMulti(offsets, sizes, itemCount);
     * @return
     */
    default List<PointerModel> peekMainMulti(List<PointerModel> pointerModels) throws CommectionException {
        StringBuilder command = new StringBuilder("peekMainMulti");
        pointerModels.forEach(item -> {
            command.append(" ").append("0x").append(Long.toHexString(item.getPointer())).append(" ").append(converReadSize(item.getSize()));
        });
        write(command.toString());
        return null;
    }


    /***
     *     写入堆内存偏移位
     *      poke <address in hex or dec> <amount of bytes in hex or dec> <data in hex or dec>
     *
     * 		u64 offset = Util::parseStringToInt(argv[1]);
     * 		u64 size = 0;
     * 		u8 *data = Util::parseStringToByteBuffer(argv[2], &size);
     * 		Commands::poke(meta.heap_base + offset, size, data);
     * @return
     */
    default void poke(long offset, byte[] data) throws CommectionException {
        write("poke 0x" + Long.toHexString(offset) + " 0x" + byteToHexString(data));
    }

    /***
     *     写入堆内存
     *      u64 offset = Util::parseStringToInt(argv[1]);
     * 		u64 size = 0;
     * 		u8 *data = Util::parseStringToByteBuffer(argv[2], &size);
     * 		Commands::poke(offset, size, data);
     * 		free(data);
     * @return
     */
    default void pokeAbsolute(long pointer, byte[] data) throws CommectionException {
        write("pokeAbsolute 0x" + Long.toHexString(pointer) + " 0x" + byteToHexString(data));
    }

    /***
     *     写入主内存偏移位
     *      Commands::MetaData meta = Commands::getMetaData();
     *
     * 		u64 offset = Util::parseStringToInt(argv[1]);
     * 		u64 size = 0;
     * 		u8 *data = Util::parseStringToByteBuffer(argv[2], &size);
     * 		Commands::poke(meta.main_nso_base + offset, size, data);
     * 		free(data);
     * @return
     */
    default void pokeMain(long offset, byte[] data) throws CommectionException {
        write("pokeMain 0x" + Long.toHexString(offset) + " 0x" + byteToHexString(data));
    }

    /***
     *      获取指针地址
     * 		for (int i = 1; i < argc; i++)
     * 			jumps[i - 1] = Util::parseStringToSignedLong(argv[i]);
     * 		u64 solved = Commands::followMainPointer(jumps, argc - 1);
     * 		printf("%016lX\n", solved);
     * @return
     */
    default long pointer(long... jumps) throws CommectionException {
        write("pointer " + longArrayToHexString(jumps));
        return hexByteToLong(read(getReadEndCode()));
    }

    /***
     *      获取指针后+偏移位+结束位(peekMain 0x110 11 12)
     *      if (argc < 3) //传入两个以上指针。。
     * 			return 0;
     * 		s64 finalJump = Util::parseStringToSignedLong(argv[argc - 1]); //finalJump=12;
     * 		u64 count = argc - 2;//4-2=2
     * 		s64 jumps[count];
     * 		for (int i = 1; i < argc - 1; i++)
     * 			jumps[i - 1] = Util::parseStringToSignedLong(argv[i]); jumps[0]=110,jumps[1]=11
     * 		u64 solved = Commands::followMainPointer(jumps, count); followMainPointer(110, 2);
     * 		if (solved != 0)
     * 			solved += finalJump;
     * 		printf("%016lX\n", solved);
     * @return
     */
    default long pointerAll(long... jumps) throws CommectionException {
        write("pointerAll " + longArrayToHexString(jumps));
        return hexByteToLong(read(getReadEndCode()));
    }

    /***
     *      通过指针获取堆偏移后的指针
     *      // pointerRelative <first (main) jump> <additional jumps> <final jump in pointerexpr>
     * 	    // returns offset relative to heap
     * 		if (argc < 3)
     * 			return 0;
     * 		s64 finalJump = Util::parseStringToSignedLong(argv[argc - 1]);
     * 		u64 count = argc - 2;
     * 		s64 jumps[count];
     * 		for (int i = 1; i < argc - 1; i++)
     * 			jumps[i - 1] = Util::parseStringToSignedLong(argv[i]);
     * 		u64 solved = Commands::followMainPointer(jumps, count);
     * 		if (solved != 0)
     *                {
     * 			solved += finalJump;
     * 			Commands::MetaData meta = Commands::getMetaData();
     * 			solved -= meta.heap_base;
     *        }
     * 		printf("%016lX\n", solved);
     * @param jumps
     * @return
     * @throws CommectionException
     */
    default long pointerRelative(long... jumps) throws CommectionException {
        write("pointerRelative " + longArrayToHexString(jumps));
        return hexByteToLong(read(getReadEndCode()));
    }

    /***
     *      读取主内存指针对应的值。读取.指针（客栈）==>堆数据
     *
     * 		if (argc < 4)
     * 			return 0;
     *
     * 		s64 finalJump = Util::parseStringToSignedLong(argv[argc - 1]);
     * 		u64 size = Util::parseStringToInt(argv[1]);
     * 		u64 count = argc - 3;
     * 		s64 jumps[count];
     * 		for (int i = 2; i < argc - 1; i++)
     * 			jumps[i - 2] = Util::parseStringToSignedLong(argv[i]);
     * 		u64 solved = Commands::followMainPointer(jumps, count);
     * 		solved += finalJump;
     * 		Commands::peek(solved, size);
     * @param size
     * @param jumps
     * @return
     * @throws CommectionException
     */
    default byte[] pointerPeek(int size, long... jumps) throws CommectionException {
        write("pointerPeek " + size + " " + longArrayToHexString(jumps));
        return hexByteToByte(read(getReadEndCode()));
    }

    /***
     *	// pointerPeekMulti <amount of bytes in hex or dec> <first (main) jump> <additional jumps> <final jump in pointerexpr> split by asterisks (*)
     * 	// warning: no validation
     * 	星号分割
     * @param jumps
     * @return
     * @throws CommectionException
     */
    default List<PointerPeekModel> pointerPeekMulti(List<PointerPeekModel> jumps) throws CommectionException {
        StringBuilder command = new StringBuilder();
        jumps.forEach(item -> {
            command.append("* ").append(item.getSize()).append(" ").append(longArrayToHexString(item.getJumps())).append(" ");
        });
        write("pointerPeekMulti" + command.substring(1));
        hexByteToByte(read(getReadEndCode()));
        return null;
    }

    /***
     * 写入堆
     * 		if (argc < 4)
     * 			return 0;
     *
     * 		s64 finalJump = Util::parseStringToSignedLong(argv[argc - 1]);
     * 		u64 count = argc - 3;
     * 		s64 jumps[count];
     * 		for (int i = 2; i < argc - 1; i++)
     * 			jumps[i - 2] = Util::parseStringToSignedLong(argv[i]);
     * 		u64 solved = Commands::followMainPointer(jumps, count);
     * 		solved += finalJump;
     *
     * 		u64 size;
     * 		u8 *data = Util::parseStringToByteBuffer(argv[1], &size);
     * 		Commands::poke(solved, size, data);
     * 		free(data);
     * @param jump
     * @throws CommectionException
     */
    default void pointerPoke(PointerPeekModel jump) throws CommectionException {
        write("pointerPoke 0x" + byteToHexString(jump.getData()) + " " + longArrayToHexString(jump.getJumps()));
    }

    /***
     * 冻结内存
     * 		if (argc != 3)
     * 			return 0;
     *
     * 		Commands::MetaData meta = Commands::getMetaData();
     *
     * 		u64 offset = Util::parseStringToInt(argv[1]);
     * 		u64 size = 0;
     * 		u8 *data = Util::parseStringToByteBuffer(argv[2], &size);
     * 		Freeze::addToFreezeMap(offset, data, size, meta.titleID);
     * @param offset
     * @param data
     * @throws CommectionException
     */
    default void freeze(long offset, byte[] data) throws CommectionException {
        write("freeze 0x" + Long.toHexString(offset) + " 0x" + byteToHexString(data));
    }

    /***
     * 解冻
     * @param offset
     * @throws CommectionException
     */
    default void unFreeze(long offset) throws CommectionException {
        write("unFreeze 0x" + Long.toHexString(offset));
    }

    /***
     * 冻结数
     * @throws CommectionException
     */
    default long freezeCount() throws CommectionException {
        write("freezeCount");
        return hexByteToLong(read(getReadEndCode()));
    }

    /***
     * 冻结重置
     * @throws CommectionException
     */
    default void freezeClear() throws CommectionException {
        write("freezeClear");
    }

    /***
     * 暂停
     * @throws CommectionException
     */
    default void freezePause() throws CommectionException {
        write("freezePause");
    }

    /***
     * 恢复
     * @throws CommectionException
     */
    default void freezeUnpause() throws CommectionException {
        write("freezePause");
    }

}
