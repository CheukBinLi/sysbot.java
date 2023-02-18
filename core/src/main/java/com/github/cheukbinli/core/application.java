package com.github.cheukbinli.core;

import com.github.cheukbinli.core.im.ImServer;
import com.github.cheukbinli.core.im.dodo.DodoApiServer;
import com.github.cheukbinli.core.im.dodo.model.Authorization;
import com.github.cheukbinli.core.model.NoticeFunctionModel;
import com.github.cheukbinli.core.model.TradeElementModel;
import com.github.cheukbinli.core.ns.model.response.GeneratePokemonResponse;
import com.github.cheukbinli.core.queue.DefaulttQueueService;
import com.github.cheukbinli.core.queue.QueueService;
import com.github.cheukbinli.core.ns.service.SwitchSysbotAggregateService;
import com.github.cheukbinli.core.util.NetUtil;
import lombok.Getter;
import org.apache.commons.codec.DecoderException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class application {

    @Getter
    ImServer imServer;
    @Getter
    SwitchSysbotAggregateService switchSysbotAggregateService;
    QueueService<TradeElementModel> verifiedQueueService;//待验证队列
    @Getter
    QueueService<TradeElementModel> queueService;

    private String switchIp = "192.168.1.125";
    private int switchPort = 6000;
    private String sysbotIp = "127.0.0.1";
    private int sysbotPort = 1111;
    private String islandSourceId = "192169";
    private String dodoClientId = "27574261";
    private String dodoToken = "Mjc1NzQyNjE.JMqT77-9.ddCaLpqQq9hkKv_FXf74--Qgck0uzpIVXbI3PDgnwhs";
    private String dodoApiPath = "https://botopen.imdodo.com";
    Thread queueConsumer;
    Thread verifiedQueueConsumer;
    volatile AtomicBoolean queueConsumerStatus = new AtomicBoolean(false);
    volatile AtomicBoolean verifiedQueueConsumerStatus = new AtomicBoolean(false);
    private Map<String, Object> trainerInfo = null;
    //    private boolean emptyPassWord = true;
    private boolean emptyPassWord = false;

    public void start() {
        queueService = new DefaulttQueueService();
        verifiedQueueService = new DefaulttQueueService();
        switchSysbotAggregateService = new SwitchSysbotAggregateService(switchIp, switchPort, sysbotIp, sysbotPort);
        imServer = new DodoApiServer(new Authorization(islandSourceId, dodoClientId, dodoToken), verifiedQueueService);
        imServer.start();
        //列队消费
        verifiedQueueConsumStart();
        queueConsumStart();
        trainerInfo = new HashMap<>();
        trainerInfo.put("GenerateOT", "伍六七");
        trainerInfo.put("DisplayTID", "800001");
        trainerInfo.put("DisplaySID", 249781);
    }

    void verifiedQueueConsumStart() {
        if (verifiedQueueConsumerStatus.get()) {
            return;
        }
        synchronized (verifiedQueueConsumerStatus) {
            if (verifiedQueueConsumerStatus.get()) {
                return;
            }
            verifiedQueueConsumerStatus.set(true);
        }
        verifiedQueueConsumer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("验证队列启动消费");
                    while (verifiedQueueConsumerStatus.get()) {
                        TradeElementModel trade = verifiedQueueService.dequeue();
                        if (null == trade) {
                            synchronized (this) {
                                wait(500);
                                continue;
                            }
                        }
                        //验证
                        try {
                            if ("位置".equals(trade.getData().getContent())) {
                                long position = queueService.position(trade.getQueueChannel());
                                imServer.channelMessageSend(trade.getChannel(), trade.getIdentity(), false, position < 0 ? "您不在队列。" : "您当前的位置是:" + (position == 0 ? "交换搜索中，请赶快链接！否则我会退出" : position));
                                continue;
                            } else if ("取消".equals(trade.getData().getContent())) {
                                queueService.remove(trade.getQueueChannel());
                                imServer.channelMessageSend(trade.getChannel(), trade.getIdentity(), false, "取消成功。");
                                continue;
                            }
                            GeneratePokemonResponse response = switchSysbotAggregateService.generatePokemon(trade.getData().getContent(), trainerInfo, trade.getData().getPkmLimit());
                            if (response.getCode() != 0) {
                                imServer.channelMessageSend(trade.getChannel(), trade.getIdentity(), false, response.getError());
                                continue;
                            }
                            //补全信息
                            trade.getData().setPkmName(switchSysbotAggregateService.getSysBotApi().pkmNameExtraction(response.getData().getSpecies()));
                            if (!emptyPassWord) {
//                                trade.getData().setRandomCode(NetUtil.getRandomNo("0000", 4));
                                trade.getData().setRandomCode(NetUtil.getRandomNo("", 4));
                            }
                            queueService.append(trade, new Function<NoticeFunctionModel<TradeElementModel, Long>, Long>() {
                                @Override
                                public Long apply(NoticeFunctionModel<TradeElementModel, Long> aLong) {
                                    String msg = aLong.getMsg();
                                    if (aLong.getCode() >= 0) {
                                        long pos = aLong.getAdditional() < 1 ? 1 : aLong.getAdditional();
                                        msg = String.format("\n排队成功，你的当前位置在**%s**,约**%s分钟**。\n请规划一下时间，耐心等待！\n%s-排号总数:%s", pos, pos * 2, trade.getQueueChannel(), queueService.statistics(trade.getQueueChannel()));
                                    }
                                    try {
                                        imServer.channelMessageSend(trade.getChannel(), trade.getIdentity(), false, msg);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        GlobalLogger.append(e);
                                    }
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                imServer.channelMessageSend(trade.getChannel(), trade.getIdentity(), false, e.getMessage());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                                GlobalLogger.append(e);
                            }
                        } finally {
                            verifiedQueueService.resetCurrentElement();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    GlobalLogger.append(e);
                }
                verifiedQueueConsumerStatus.set(false);
                System.out.println("验证队列停止消费");
                GlobalLogger.append("验证队列停止消费");
            }
        });
        verifiedQueueConsumer.start();
    }

    void queueConsumStart() {
        if (queueConsumerStatus.get()) {
            return;
        }
        synchronized (queueConsumerStatus) {
            if (queueConsumerStatus.get()) {
                return;
            }
            queueConsumerStatus.set(true);
        }
        queueConsumer = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    System.out.println("队列启动消费");
                    while (queueConsumerStatus.get()) {
                        TradeElementModel trade = queueService.dequeue();
                        if (null == trade) {
                            synchronized (this) {
                                wait(2000);
                                continue;
                            }
                        }
                        try {
//                            imServer.channelMessageSend(trade.getChannel(), trade.getIdentity(), false, "\n派送:" + trade.getData().getPkmName() + "\n密码:见私信\n状态:搜索中");
                            imServer.personalMessageSend(trade.getIdentity(), trade.getIslandSource(), "\n派送:" + trade.getData().getPkmName() + "\n配对密语：" + trade.getData().getRandomCode());
                            switchSysbotAggregateService.trade(trade, new Function<TradeElementModel, String>() {
                                @Override
                                public String apply(TradeElementModel s) {
//                                    imServer.personalMessageSend(s.getIdentity(), s.getIslandSource(), s.getOperationMessage());
                                    try {
                                        imServer.writeLogAndWriteChannel(s.getIslandSource(), s.getChannel(), s.getIdentity(), s.getOperationMessage());
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        GlobalLogger.append(e);
                                    }
                                    return null;
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            break;
                        } finally {
                            queueService.resetCurrentElement();
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                queueConsumerStatus.set(false);
                System.out.println("队列停止消费");
            }
        });
        queueConsumer.start();
    }

    public static void main(String[] args) throws DecoderException, IOException {
        application a = new application();
        a.start();
//        a.switchSysbotAggregateService.getSwitchCommandApi().SetScreen(ScreenState.On);
//
//        a.switchSysbotAggregateService.getSwitchCommandApi().SetScreen(ScreenState.Off);
//        Object o = ((DodoApiServer) a.imServer).getDodoApi().GetChannelList("192169");
//        System.out.println(1);
//        a.queueService.append((TradeElementModel) new TradeElementModel().setIdentity("1"), null);
//        a.queueService.append((TradeElementModel) new TradeElementModel().setIdentity("2"), null);
//        a.queueService.append((TradeElementModel) new TradeElementModel().setIdentity("3"), null);
//        a.queueService.append((TradeElementModel) new TradeElementModel().setIdentity("4"), null);
//        a.queueService.append((TradeElementModel) new TradeElementModel().setIdentity("5"), null);
//        System.out.println(a.queueService.position("5"));
//        a.switchSysbotAggregateService.getSwitchService().IsInPokePortal();
    }

}
