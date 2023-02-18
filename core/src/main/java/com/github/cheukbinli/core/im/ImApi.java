package com.github.cheukbinli.core.im;

import com.github.cheukbinli.core.im.dodo.model.Authorization;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public interface ImApi {

    default WebSocket connection(String urlStr, EventManager eventManager, Authorization authorization, final CountDownLatch block) throws URISyntaxException {
        String auth = String.format("Bot %s.%s", authorization.getClientId(), authorization.getToken());
        OkHttpClient client = new OkHttpClient.Builder()
                .pingInterval(25, TimeUnit.SECONDS)
                .build();
        Request request = new Request
                .Builder()
                .url(urlStr)
                .header("Authorization", auth)
                .header("Content-type", "application/json")
                .build();
        return client.newWebSocket(request, new WebSocketListener() {
            @Override
            public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
                block.countDown();
                super.onClosed(webSocket, code, reason);
            }

            @Override
            public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
                block.countDown();
                super.onFailure(webSocket, t, response);
            }

            @Override
            public void onMessage(@NotNull WebSocket webSocket, @NotNull ByteString bytes) {
                super.onMessage(webSocket, bytes);
                System.out.println(bytes.toByteArray().toString());
                System.out.println(new String(bytes.toByteArray()));
            }

            @Override
            public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
                super.onOpen(webSocket, response);
                System.out.println("连接成功");
            }
        });

    }

    public static void main(String[] args) throws URISyntaxException, InterruptedException {
        String url = "wss://bot-gateway.imdodo.com/dodo?token=k5btqXNoynVvZZp7izYQbTVATS_46n8H1DlMqtSVxEupBWlEU7pUjGGjeEKilCHpDOSxuhK1c430_A7WZLMugCIskjqsJ57HHklDRRFOWKvaISXhekY9I-PmaShKNRnbr8VqDCacjEE_RirMkXjgJd-6Lnz3PZQqgwETYmjqDQ4=&ts=1676293017";
        ImApi a = new ImApi() {
        };

        CountDownLatch countDownLatch = new CountDownLatch(1);
        a.connection(url, null, new Authorization("192169", "27574261", "Mjc1NzQyNjE.JMqT77-9.ddCaLpqQq9hkKv_FXf74--Qgck0uzpIVXbI3PDgnwhs"), countDownLatch);
        System.out.println("等待结束");
        countDownLatch.await();

    }

}
