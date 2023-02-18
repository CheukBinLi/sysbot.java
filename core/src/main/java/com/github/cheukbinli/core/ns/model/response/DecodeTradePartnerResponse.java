package com.github.cheukbinli.core.ns.model.response;

import com.github.cheukbinli.core.ns.model.CommandModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class DecodeTradePartnerResponse extends CommandModel<Map, List<DecodeTradePartnerResponse.Data>> {

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String Game;
        private String Gender;
        private String Language;
        private String OT;
        private String DisplaySID;
        private String DisplayTID;
        private String FullDisplayTID;
    }
}
