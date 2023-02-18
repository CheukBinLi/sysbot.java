package com.github.cheukbinli.core.model;

import com.github.cheukbinli.core.queue.ElementModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class TradeElementModel extends ElementModel<TradeElementModel.Data> {

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String pkmName;
        private String randomCode;
        private String content;
        //批量限制数值
        private int pkmLimit = 2;

        public Data(String pkmName, String randomCode, String content) {
            this.pkmName = pkmName;
            this.randomCode = randomCode;
            this.content = content;
        }
    }

}
