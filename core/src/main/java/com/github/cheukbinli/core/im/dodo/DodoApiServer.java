package com.github.cheukbinli.core.im.dodo;

import com.github.cheukbinli.core.GlobalLogger;
import com.github.cheukbinli.core.im.EventManager;
import com.github.cheukbinli.core.im.ImServer;
import com.github.cheukbinli.core.im.dodo.handler.EventBodyChannelMessageHandler;
import com.github.cheukbinli.core.im.dodo.handler.EventBodyPersonalMessageHandler;
import com.github.cheukbinli.core.im.dodo.model.Authorization;
import com.github.cheukbinli.core.im.dodo.model.DodoMessageInfoModel;
import com.github.cheukbinli.core.im.dodo.model.basic.MessageBody;
import com.github.cheukbinli.core.im.dodo.model.dto.ChanneInfo;
import com.github.cheukbinli.core.im.dodo.model.dto.RobotInfo;
import com.github.cheukbinli.core.im.dodo.model.dto.request.MessageBodyTextRequest;
import com.github.cheukbinli.core.im.dodo.model.dto.request.SetChannelMessageSendRequest;
import com.github.cheukbinli.core.im.dodo.model.dto.request.SetPersonalMessageSendRequest;
import com.github.cheukbinli.core.im.dodo.model.dto.resopnse.GetChannelListResponse;
import com.github.cheukbinli.core.queue.QueueService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import okhttp3.WebSocket;
import org.apache.commons.collections.MapUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DodoApiServer implements ImServer {

    private final Authorization authorization;
    private final QueueService queueService;
    @Getter
    private String dodoApiPath = "https://botopen.imdodo.com";
    @Getter
    private EventManager<WebSocket> eventManager;
    @Getter
    private DodoApi dodoApi;
    @Getter
    public DodoReceiver dodoReceiver;
    private Boolean start = false;
    @Getter
    private RobotInfo robot;
    private Map<String, ChanneInfo> channeInfoMap;
    private List<ChanneInfo> logChannel;

    private BlockingDeque<DodoMessageInfoModel> writeLogQueue = new LinkedBlockingDeque<>(1024);
    private volatile AtomicBoolean writeLogStatus = new AtomicBoolean();

    public void start() {
        if (start) {
            return;
        } else {
            synchronized (start) {
                if (start) {
                    return;
                }
                try {
                    dodoApi = new DodoApi(this.dodoApiPath, this.authorization);
                    dodoApi.init();
                    robot = dodoApi.GetBotInfo().getData();

                    eventManager = new DodoEventManager(this, queueService, robot);
                    eventManager.appendEventHandle(new EventBodyChannelMessageHandler(eventManager)).appendEventHandle(new EventBodyPersonalMessageHandler(eventManager));

                    dodoReceiver = new DodoReceiver(dodoApi, authorization, eventManager);
                    dodoReceiver.start();
                    writeLogStart();
                    initChannelInfo();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                start = true;
            }
        }
    }

    private void initChannelInfo() throws IOException {
        if (MapUtils.isEmpty(channeInfoMap)) {
            GetChannelListResponse response = dodoApi.GetChannelList(authorization.getIslandSourceId());
            if (null != response) {
//                channeInfoMap = response.getData().stream().collect(Collectors.toMap(ChanneInfo::getChannelId, Function.identity()));
                logChannel = new ArrayList<>();
                channeInfoMap = response.getData().stream().collect(Collectors.toMap(ChanneInfo::getChannelId, new Function<ChanneInfo, ChanneInfo>() {
                    @Override
                    public ChanneInfo apply(ChanneInfo channeInfo) {
                        channeInfo.setVip(channeInfo.getChannelName().toUpperCase().contains("VIP"));
                        if (channeInfo.getChannelName().contains("日志")) {
                            logChannel.add(channeInfo);
                        }
                        return channeInfo;
                    }
                }));
            }
        }
    }

    public void stop() {
        if (start) {
            dodoReceiver.stop();
            writeLogStatus.set(false);
        }
    }

    private void writeLogStart() {
        if (writeLogStatus.get()) {
            return;
        }
        Thread writeLogThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    writeLogStatus.set(true);
                    writeLogQueue.clear();
                    System.out.println("dodo远程日志服务启动。");
                    GlobalLogger.appendln("dodo远程日志服务启动。");
                    while (writeLogStatus.get()) {
                        DodoMessageInfoModel msg = writeLogQueue.takeFirst();
                        if (null == msg) {
                            synchronized (logChannel) {
                                wait(3000);
                            }
                            continue;
                        }
                        logChannel.forEach(item -> {
                            try {
                                channelMessageSend(item.getChannelId(), msg.getMessageBody().getContent());
                            } catch (IOException e) {
                                e.printStackTrace();
                                GlobalLogger.append(e);
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    System.out.println("dodo日志停止写入。");
                    GlobalLogger.append(e);
                    GlobalLogger.appendln("dodo远程日志停止写入。");
                }
            }
        });
        writeLogThread.start();
    }

    public void personalMessageSend(String toId, String islandSourceId, String message) throws IOException {
        dodoApi.SetPersonalMessageSend(new SetPersonalMessageSendRequest().setDodoSourceId(toId).setIslandSourceId(islandSourceId).setMessageBody(new MessageBodyTextRequest().setContent(message)));

    }

    public void channelMessageSend(String channel, String atId, boolean atAll, String message) throws IOException {
        dodoApi.SetChannelMessageSend(new SetChannelMessageSendRequest().setChannelId(channel).setMessageBody(new MessageBodyTextRequest().setContent(String.format("<@%s>%s", atAll ? atId : "!" + atId, message))));
    }

    private void channelMessageSend(String channel, String message) throws IOException {
        dodoApi.SetChannelMessageSend(new SetChannelMessageSendRequest().setChannelId(channel).setMessageBody(new MessageBodyTextRequest(message)));
    }

    public ChanneInfo getChanneInfo(String channelID) throws IOException {
        initChannelInfo();
        return channeInfoMap.get(channelID);
    }

    public void writeLogAndWriteChannel(String islandSourceId, String channel, String atId, String message) throws IOException {
        //https://open.imdodo.com/dev/api/message.html#消息语法
        boolean atAll = "all".equals(atId);
//        channelMessageSend(channel, atId, atAll, message);
        channelMessageSend(channel, message);
        writeLogQueue.add(
                new DodoMessageInfoModel()
                        .setIslandSourceId(islandSourceId)
//                        .setChannelId(item.getChannelId())
                        .setAt(atId)
                        .setMessageBody(new MessageBody().setContent(message))
        );
    }

}
