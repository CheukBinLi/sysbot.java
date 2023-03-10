package com.github.cheukbinli.core.ns;

import com.github.cheukbinli.core.ns.constant.KeyboardKey;
import com.github.cheukbinli.core.ns.constant.ScreenState;
import com.github.cheukbinli.core.ns.constant.SwitchButton;
import com.github.cheukbinli.core.ns.constant.SwitchStick;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

@RequiredArgsConstructor
public class SwitchCommandApi extends BufferPoolUtil {

    static String WRITE_END_CODE = "\r\n";
    static char READ_END_CODE = '\n';
    static byte[] BLANK_BYTE = new byte[0];
    static final int DEFAULT_SIZE = 17;

    SocketChannel socketChannel;
    Socket socket;
    private final String ip;
    private final int port;

    public void write(SocketChannel channel, String command) throws InterruptedException, IOException {
        byte[] data = encode(command);
        ByteBuffer byteBuffer = null;
        try {
            channel.write(byteBuffer = borrowResource().put(data).slice(0, data.length));
        } finally {
            fillBack(byteBuffer);
        }
    }

    public SocketChannel getSocketChannel() throws IOException {
        if (null == socketChannel) {
            synchronized (this) {
                if (null == socketChannel) {
                    this.socketChannel = connection();
                }
            }
        }
        return socketChannel;
    }

    public SocketChannel connection() throws IOException {
        return connection(ip, port);
    }

    public SocketChannel connection(String ip, int port) throws IOException {
        SocketChannel socketChannel = SocketChannel.open(new InetSocketAddress(ip, port));
        socketChannel.configureBlocking(true);
        return socketChannel;
    }

    public byte[] read(Socket socket, int size) throws IOException {
        InputStream in = socket.getInputStream();
        int code;
        byte[] data = new byte[size];
        in.read(data, 0, size);
        return data;
    }

    public byte[] read(Socket socket, char endCode) throws IOException {
        InputStream in = socket.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int code;
        while ((code = in.read()) != endCode) {
            out.write((char) code);
        }
        return out.toByteArray();
    }

    public String longArrayToHexString(long... pointer) {
        if (pointer.length == 1) {
            return "0x" + Long.toHexString(pointer[0]);
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pointer.length; i++) {
            sb.append(" ").append("0x").append(Long.toHexString(pointer[i]));
        }
        return sb.substring(1);
    }

    public String longArrayToString(long... pointer) {
        if (pointer.length == 1) {
            return pointer[0] + "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pointer.length; i++) {
            sb.append(" ").append(pointer[i]);
        }
        return sb.substring(1);
    }

    public byte[] encode(String content) {
        if (null == content || content.length() < 1) {
            return BLANK_BYTE;
        }
        return content.endsWith(WRITE_END_CODE) ? content.getBytes() : (content + WRITE_END_CODE).getBytes();
    }

    public String checkPrefix(String data, String prefix) {
        return data.startsWith(prefix) ? data : prefix + data;
    }

    public String bytesToHex(byte[] bytes) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(bytes[i] & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }

    /***
     * ??????????????????
     * @param pointer
     * @return
     * @throws IOException
     */
    public byte[] pointerAll(long... pointer) throws IOException {
        String command = "pointerAll " + longArrayToHexString(pointer);
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
//        ByteBuffer byteBuffer = ByteBuffer.allocate(DEFAULT_SIZE);
//        getSocketChannel().read(byteBuffer);
//        if (null == byteBuffer) {
//            return BLANK_BYTE;
//        }
//        byte[] result = new byte[DEFAULT_SIZE - 1];
//        byteBuffer.get(0, result);
//        return result;
        return read(getSocketChannel().socket(), READ_END_CODE);
    }

    /***
     * ????????????
     * @param pointer
     * @param size
     * @return
     * @throws IOException
     */
    public byte[] peek(int size, long... pointer) throws IOException {
        String command = "peek " + longArrayToHexString(pointer) + " " + size;
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
//        size = size * 2 + 1;
//        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
//        getSocketChannel().read(byteBuffer);
//        if (null == byteBuffer) {
//            return null;
//        }
//        byte[] result = new byte[size - 1];
//        byteBuffer.get(0, result);
//        return result;
        return read(getSocketChannel().socket(), READ_END_CODE);
    }

    /***
     * ????????????:????????????
     * @param pointer
     * @param size
     * @return
     * @throws IOException
     */
    public byte[] peekAbsolute(int size, long... pointer) throws IOException {
        String command = "peekAbsolute " + longArrayToHexString(pointer) + " " + size;
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
        size = size * 2 + 1;
//        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
//        getSocketChannel().read(byteBuffer);
//        if (null == byteBuffer) {
//            return null;
//        }
//        byte[] result = new byte[size - 1];
//        byteBuffer.get(0, result);
//        return result;
//        return read(getSocketChannel().socket(), READ_END_CODE);
        return Arrays.copyOf(read(getSocketChannel().socket(), size), size - 1);
    }

    /***
     * ????????????:????????????
     * @param pointer
     * @param size
     * @return
     * @throws IOException
     */
    public byte[] pointerPeek(int size, long... pointer) throws IOException {
        String command = "pointerPeek " + size + " " + longArrayToString(pointer);
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
//        size = size * 2 + 1;
//        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
//        getSocketChannel().read(byteBuffer);
//        if (null == byteBuffer) {
//            return null;
//        }
//        byte[] result = new byte[size - 1];
//        byteBuffer.get(0, result);
//        return result;
        return read(getSocketChannel().socket(), READ_END_CODE);
    }

    /***
     * ????????????:????????????
     * @return
     * @throws IOException
     */
    public byte[] getTitleID() throws IOException {
        String command = "getTitleID ";
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
//        ByteBuffer byteBuffer = ByteBuffer.allocate(DEFAULT_SIZE);
//        getSocketChannel().read(byteBuffer);
//        if (null == byteBuffer) {
//            return null;
//        }
//        byte[] result = new byte[DEFAULT_SIZE - 1];
//        byteBuffer.get(0, result);
//        return result;
        return read(getSocketChannel().socket(), READ_END_CODE);
    }


    /***
     * ??????????????????
     * @param pointer
     * @param data
     * @throws IOException
     */
    public void pokeAbsolute(byte[] data, long... pointer) throws IOException {
        String command = "pokeAbsolute " + longArrayToHexString(pointer) + " " + checkPrefix(bytesToHex(data), "0x");
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
    }

    /***
     * ??????????????????
     * @param pointer
     * @param data
     * @throws IOException
     */
    public void pointerPoke(byte[] data, long... pointer) throws IOException {
        String command = "pointerPoke " + checkPrefix(bytesToHex(data), "0x") + " " + longArrayToString(pointer);
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
    }

    /***
     * ?????????????????????
     * Removes the virtual controller from the bot. Allows physical controllers to control manually.
     * @throws IOException
     */
    public void detachController() throws IOException {
        String command = "detachController";
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
    }

    /***
     * ??????
     * Removes the virtual controller from the bot. Allows physical controllers to control manually.
     * @throws IOException
     */
    public void clickButton(SwitchButton.Button button) throws IOException {
        String command = "click " + button;
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
    }

    public void clickButton(SwitchButton.Button button, long waitTime) throws IOException, InterruptedException {
        String command = "click " + button;
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
        Thread.sleep(waitTime);
    }

    /***
     * ????????????
     * Removes the virtual controller from the bot. Allows physical controllers to control manually.
     * @throws IOException
     */
    public void pressButton(SwitchButton button) throws IOException {
        String command = "press " + button;
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
    }

    /***
     * ????????????
     * Removes the virtual controller from the bot. Allows physical controllers to control manually.
     * @throws IOException
     */
    public void releaseButton(SwitchButton button) throws IOException {
        String command = "release " + button;
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
    }

    /***
     * ???????????????????????????
     * Removes the virtual controller from the bot. Allows physical controllers to control manually.
     * @throws IOException
     */
    public void setStick(SwitchStick stick, float x, float y) throws IOException {
        String command = String.format("setStick %f %f", stick.name(), x, y);
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
    }

    /***
     * ???????????????????????????
     * Removes the virtual controller from the bot. Allows physical controllers to control manually.
     * @throws IOException
     */
    public void resetStick(SwitchStick stick) throws IOException {
        setStick(stick, 0f, 0f);
    }

    /***
     * ????????????
     *
     * @throws IOException
     */
    public void keyboardKey(KeyboardKey... keys) throws IOException {
        StringBuilder command = new StringBuilder("key");
        for (KeyboardKey key : keys) {
            command.append(" ").append(key.getV());
        }
        getSocketChannel().write(ByteBuffer.wrap(encode(command.toString())));
    }

    /***
     * ????????????????????????/?????????????????????????????????????????????
     * Toggles the screen display On/Off, useful for saving power if not needed
     * @throws IOException
     */
    public void SetScreen(ScreenState state) throws IOException {
        String command = "screen" + state.name();
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
    }

    /***
     * ??????
     * @throws IOException
     */
    public byte[] pixelPeek() throws IOException, InterruptedException, DecoderException {
        String command = "pixelPeek";
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
//        InputStream in = getSocketChannel().socket().getInputStream();
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        int code;
//        while ((code = in.read()) != '\n') {
//            out.write((char) code);
//        }
//        return out.toByteArray();
        return read(getSocketChannel().socket(), READ_END_CODE);
    }

    /***
     *
     *
     * @throws IOException
     */
    public boolean isProgramRunning(long pid) throws IOException {
        String command = "isProgramRunning " + longArrayToHexString(pid);
        getSocketChannel().write(ByteBuffer.wrap(encode(command)));
        ByteBuffer byteBuffer = ByteBuffer.allocate(DEFAULT_SIZE);
        getSocketChannel().read(byteBuffer);
        if (null == byteBuffer) {
            return false;
        }
        byte[] result = new byte[DEFAULT_SIZE - 1];
        byteBuffer.get(0, result);
        return true;
    }

    public void pushPKM(byte[] data, long pointer) throws IOException, InterruptedException {
        String command = "pokeAbsolute " + longArrayToHexString(pointer) + " 0x" + Hex.encodeHexString(data);
//        aaaa.bytesToHexString(data);
        write(getSocketChannel(), command);
    }

    byte[] randomRead(byte[] data, int index, int end) {
        if (null == data || data.length < end) {
            return data;
        }
        byte[] result = new byte[end - index];
        for (int i = index, j = 0; i < end; i++) {
            result[j++] = data[i];
        }
        return result;
    }

    public static void main(String[] args) throws IOException, InterruptedException, DecoderException {
//        System.out.println(new SwitchCommandApi("", 0).longArrayToHexString(PokeDataOffsetsSV.LibAppletWeID));
//        SwitchCommandApi api = new SwitchCommandApi("192.168.1.125", 6000);
        SwitchCommandApi api = new SwitchCommandApi("192.168.50.220", 6000);
        System.out.println(Hex.encodeHexString(api.pixelPeek()));
//        api.clickButton(SwitchButton.Button.HOME, 1000);
//        api.clickButton(SwitchButton.Button.X, 1000);
//        api.clickButton(SwitchButton.Button.A, 8000);
//        api.clickButton(SwitchButton.Button.B, 4000);
//        api.clickButton(SwitchButton.Button.A, 4000);
//        api.clickButton(SwitchButton.Button.A, 30000);
//        api.clickButton(SwitchButton.Button.A, 4000);
        try {
            byte[] imageByte = api.pixelPeek();
//            byte[] imageByte = api.pixelPeekBio();
//            System.out.println((int) imageByte[imageByte.length - 1]);
//            System.out.println((int) '\n');
            byte[] imageByte1 = Hex.decodeHex(new String(imageByte));
//            System.out.println(new String(imageByte1));

            FileOutputStream out = new FileOutputStream(new File("/Users/cheukbinli/Downloads/11111.jpg"));
            out.write(imageByte1);
            out.close();

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        } catch (DecoderException ex) {
            throw new RuntimeException(ex);
        }

    }

}
