package com.github.cheukbinli.core.ns.command.impl;

import com.github.cheukbinli.core.ns.command.CommectionException;
import com.github.cheukbinli.core.ns.command.ConnectionApi;
import lombok.Data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.function.Function;

@Data
public class WirelessConnectionApi implements ConnectionApi {

    private SocketChannel socketChannel;
    private volatile InputStream in;
    private volatile OutputStream out;
    private volatile boolean canInitSocketChannel = true;
    private final String ipaddress;
    private final int port;

    public WirelessConnectionApi(String ipaddress, int port) throws IOException {
        this.ipaddress = ipaddress;
        this.port = port;
        start();
    }

    public void start() throws IOException {
        if (canInitSocketChannel || null == socketChannel) {
            synchronized (this) {
                if (canInitSocketChannel) {
                    canInitSocketChannel = false;
                    socketChannel = SocketChannel.open(new InetSocketAddress(ipaddress, port));
                    socketChannel.configureBlocking(true);
                    in = socketChannel.socket().getInputStream();
                    out = socketChannel.socket().getOutputStream();
                }
            }
        }
    }

    @Override
    public void read(final byte[] buffer) {
        try {
            in.read(buffer);
        } catch (Throwable e) {
            throw new CommectionException(e);
        }
    }

    @Override
    public int read() {
        try {
            return in.read();
        } catch (Throwable e) {
            throw new CommectionException(e);
        }
    }

    @Override
    public void read(char breakCode, int sectionSize, Function<byte[], byte[]> section) {
        if (sectionSize % 2 != 0) {
            throw new CommectionException("sectionSize：必须能被2整除。");
        }
        int code;
        int counter = 2;
        ByteArrayOutputStream sectionStream = new ByteArrayOutputStream();
        try {
            while ((code = in.read()) != (int) breakCode) {
                sectionStream.write(code);
                if (counter++ > sectionSize) {
                    counter = 2;
                    section.apply(sectionStream.toByteArray());
                    sectionStream.reset();
                }
            }
            if (sectionStream.size() > 0) {
                section.apply(sectionStream.toByteArray());
            }
        } catch (Throwable e) {
            throw new CommectionException(e);
        }
    }

    @Override
    public void write(byte[] buffer, int offset, int len) {
        try {
            out.write(buffer, offset, len);
        } catch (Throwable e) {
            throw new CommectionException(e);
        }
    }

    @Override
    public void write(byte[] buffer) {
        try {
            out.write(buffer);
        } catch (Throwable e) {
            throw new CommectionException(e);
        }
    }

    @Override
    public void write(String content) {
        try {
            out.write(content.getBytes());
        } catch (Throwable e) {
            throw new CommectionException(e);
        }
    }

    @Override
    public void write(int b) {
        try {
            out.write((byte) b);
        } catch (Throwable e) {
            throw new CommectionException(e);
        }
    }
}
