package com.github.cheukbinli.core.im.dodo.handler;

import com.github.cheukbinli.core.im.EventHandler;
import com.github.cheukbinli.core.im.EventManager;
import com.github.cheukbinli.core.im.ImChannel;
import com.github.cheukbinli.core.im.dodo.DodoApiServer;
import com.github.cheukbinli.core.im.dodo.model.dto.ChanneInfo;
import com.github.cheukbinli.core.im.dodo.model.event.EventBodyChannelMessage;
import com.github.cheukbinli.core.im.dodo.model.event.EventSubjectModel;
import com.github.cheukbinli.core.model.TradeElementModel;
import com.github.cheukbinli.core.queue.ElementModel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import okhttp3.WebSocket;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
@RequiredArgsConstructor
public class EventBodyChannelMessageHandler implements EventHandler<WebSocket, EventBodyChannelMessage.EventBodyChannelMessageBody> {

    private final EventManager<WebSocket> eventManager;

    @Override
    public String getHandlType() {
        return "2001";
    }

    @Override
    public Class<EventBodyChannelMessage.EventBodyChannelMessageBody> getBodyType() {
        return EventBodyChannelMessage.EventBodyChannelMessageBody.class;
    }

    @Override
    public boolean doExcute(ImChannel<WebSocket> webSocket, EventSubjectModel eventSubjectModel, final EventBodyChannelMessage.EventBodyChannelMessageBody content) throws IOException {
//        System.out.println("收到消息:" + content.getMessageBody().getContent());
        switch (content.getMessageType()) {
            case 1:
                String msgContent = content.getMessageBody().getContent();
                if (null == msgContent) {
                    return false;
                }
                if (msgContent.startsWith("<@!")) {
                    int endIndex = msgContent.indexOf(">");
                    String robotId = msgContent.substring(3, endIndex);
                    if (!getEventManager().getRobotInfo().getDodoSourceId().equals(robotId)) {
                        return false;
                    }
                    content.getMessageBody().setContent(msgContent.substring(endIndex + 1).trim());
                    if (content.getMessageBody().getContent().length() < 1) {
                        return false;
                    }
                    return true;
                }
            case 5:
                if (!content.getMessageBody().getName().toLowerCase().endsWith(".pk9")) {
//                    throw new RuntimeException("只支持pk9文件");
                    getEventManager().getImServer().channelMessageSend(content.getChannelId(), content.getDodoSourceId(), false, "只支持pk9文件");
                    return false;
                }
                content.getMessageBody().setDataStream(((DodoApiServer) getEventManager().getImServer()).getDodoApi().download(content.getMessageBody().getUrl(), content.getMessageBody().getSize()));
                return true;
            default:
                break;
        }
        //无效数据
        return false;
    }

    @Override
    public ElementModel noticeObserver(EventSubjectModel eventSubjectModel, EventBodyChannelMessage.EventBodyChannelMessageBody mode) throws IOException {

        TradeElementModel elementModel = new TradeElementModel();
//        elementModel.setVip(true);
        TradeElementModel.Data data = new TradeElementModel.Data("", "", mode.getMessageBody().getContent(), mode.getMessageBody().getDataStream(), mode.getMessageType());
        ChanneInfo channeInfo = getEventManager().getImServer().getChanneInfo(mode.getChannelId());
        elementModel
                .setData(data)
                .setUserName(mode.getPersonal().getNickName())
                .setChannel(mode.getChannelId())
                .setIslandSource(mode.getIslandSourceId())
                .setVip(null == channeInfo ? false : channeInfo.isVip())
                .setInvalid(true)
                .setIdentity(mode.getDodoSourceId())
                .setUserName(mode.getPersonal().getNickName());
        data.setPkmLimit(elementModel.isVip() ? 5 : data.getPkmLimit());
        return elementModel;
    }

    public static void main(String[] args) {
        String a = "<@!3474969> 小明";
        String regFileName = "([0-9]+)";
        Matcher matcher = Pattern.compile(regFileName).matcher(a);
        String fileName = null;
        // 判断是否可以找到匹配正则表达式的字符
        if (matcher.find()) {
            // 将匹配当前正则表达式的字符串即文件名称进行赋值
            fileName = matcher.group();
        }
        System.out.println(fileName);
    }

}
