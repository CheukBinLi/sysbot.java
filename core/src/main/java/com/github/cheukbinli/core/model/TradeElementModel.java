package com.github.cheukbinli.core.model;

import com.github.cheukbinli.core.queue.ElementModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.ByteArrayOutputStream;

@Data
public class TradeElementModel extends ElementModel<TradeElementModel.Data> {

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String pkmName;
        private String randomCode;
        private String content;
        private ByteArrayOutputStream dataStream;
        private int messageType;//	int	消息类型，1：文字消息，2：图片消息，3：视频消息，4：分享消息，5：文件消息，6：卡片消息，7：红包消息
        //批量限制数值
        private int pkmLimit = 2;
        private String additional;

        public Data(String pkmName, String randomCode, String content, ByteArrayOutputStream dataStream, int messageType) {
            this.pkmName = pkmName;
            this.randomCode = randomCode;
            this.content = content;
            this.dataStream = dataStream;
            this.messageType = messageType;
        }

        public boolean isFile() {
            return messageType == 5;
        }

        public boolean isText() {
            return messageType == 1;
        }
    }

}
