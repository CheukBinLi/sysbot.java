package com.github.cheukbinli.core.im.dodo;

import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.im.EventHandler;
import com.github.cheukbinli.core.im.EventManager;
import com.github.cheukbinli.core.im.ImChannel;
import com.github.cheukbinli.core.im.MessageReceiver;
import com.github.cheukbinli.core.im.dodo.model.Authorization;
import lombok.Data;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Data
public class DodoReceiver implements MessageReceiver {

    private Thread receiver;
    private final DodoApi dodoApi;
    private CountDownLatch blockLock;
    private ImChannel<WebSocket> channel;
    private final Authorization authorization;
    private final EventManager<WebSocket> eventManager;

    @Override
    public void start() {
        if (null != blockLock && blockLock.getCount() > 0) {
            return;
        }
        blockLock = new CountDownLatch(1);
        receiver = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("Dodo消息服务初始化");
                    GlobalLogger.appendln("Dodo消息服务初始化");
                    receive(dodoApi.getWebSocketConnection(), eventManager, authorization, blockLock);
                } catch (Exception e) {
                    e.printStackTrace();
                    GlobalLogger.appendln("Dodo消息服务停止");
                    GlobalLogger.append(e);
                }
            }
        });
        receiver.start();
    }

    @Override
    public void stop() {
        blockLock.countDown();
    }

    @Override
    public MessageReceiver appendHandler(EventHandler handler) {
        eventManager.appendEventHandle(handler);
        return this;
    }

    @Override
    public void removeHandler(String handlerType) {

    }

    void receive(String urlStr, EventManager eventManager, Authorization authorization, final CountDownLatch block) throws URISyntaxException, InterruptedException {
        String auth = String.format("Bot %s.%s", authorization.getClientId(), authorization.getToken());
        OkHttpClient client = new OkHttpClient.Builder()
                .pingInterval(30, TimeUnit.SECONDS)
                .build();
        Request request = new Request
                .Builder()
                .url(urlStr)
                .header("Authorization", auth)
                .header("Content-type", "application/json")
                .build();
        client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                block.countDown();
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                block.countDown();
                t.printStackTrace();
                super.onFailure(webSocket, t, response);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                try {
                    eventManager.excute(DodoReceiver.this.channel, bytes.toByteArray());
                } catch (Exception e) {
                    GlobalLogger.append(e);
                    e.printStackTrace();
                }
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                DodoReceiver.this.channel = new ImChannel<WebSocket>() {
                    @Override
                    public WebSocket getChannel() {
                        return webSocket;
                    }
                };
                System.out.println("Dodo消息服务启动成功");
                GlobalLogger.appendln("Dodo消息服务启动成功");
            }
        });
        blockLock.await();
        System.out.println(1);
    }

    public static void main(String[] args) {
//        MessageReceiver receive = new DodoReceiver(new Authorization("27574261", "Mjc1NzQyNjE.JMqT77-9.ddCaLpqQq9hkKv_FXf74--Qgck0uzpIVXbI3PDgnwhs"), "https://botopen.imdodo.com");
//        receive.init();
//        receive.start();
    }

}
