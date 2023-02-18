package com.github.cheukbinli.core.util;

public class MessageTemplate {

    public enum MessageTemplateStatus {
        SEND_PASSWORD,
        CONNECTION,
        SUCCESS,
        FAIL,
        ROW_NUMBER;

    }

    public static String messageTemplate(String dodoId, String dodoName, String pkmName, String myTrainer, String toTrainer, int position, String errMsg, MessageTemplateStatus status) {
        switch (status) {
            case SEND_PASSWORD:
                return String.format(
                        "<@!%s>\n您你宝可梦：**%s**\n密码我已经私信你了！\n请赶快输入密码！",
                        dodoId,
                        pkmName
                );
            case CONNECTION:
                return String.format("<@!%s>\n我已经在连接连接了! %s\n我的游戏ID是：**%s**\n您你宝可梦：**%s**\n请赶快链接！否则我会退出！",
                        dodoId,
                        dodoName,
                        myTrainer,
                        pkmName
                );
            case SUCCESS:
                return String.format("<@!%s>\n交易完成，你可以继续排号下一只宝可梦了！", dodoId);
            case FAIL:
                return String.format("<@!%s>\n您人呢！消失了吗！交易失败原因：**%s!**", dodoId, errMsg);
            case ROW_NUMBER:
                return String.format("<@!%s>注意%s个排号以后该到您了！", dodoId, position);
            default:
                return "";
        }
    }

}
