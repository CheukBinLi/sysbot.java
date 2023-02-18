package com.github.cheukbinli.core.ns;

import com.github.cheukbinli.core.GlobalLogger;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

public class BufferPoolUtil {

    static int maxPoolSize = 15;
    static int bufferSize = 5120;

    static BlockingDeque<ByteBuffer> byteBufferPool = new LinkedBlockingDeque<>(maxPoolSize * 2);

    static {
        for (int i = 0; i < maxPoolSize; i++) {
            try {
                byteBufferPool.put(ByteBuffer.allocate(bufferSize));
            } catch (InterruptedException e) {
                GlobalLogger.append(e);
                throw new RuntimeException(e);
            }
        }
    }

    public ByteBuffer borrowResource() throws InterruptedException {
        return byteBufferPool.take();
    }

    public void fillBack(ByteBuffer byteBuffer) throws InterruptedException {
        if (null == byteBuffer) {
            return;
        }
        byteBuffer.rewind();
        byteBufferPool.putLast(byteBuffer);
    }

}
