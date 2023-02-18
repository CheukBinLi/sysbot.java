package com.github.cheukbinli.core.ns.model.response;

import com.github.cheukbinli.core.ns.model.CommandModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

public class DecodeTrainerResponse extends CommandModel<Map, DecodeTrainerResponse.Data> {

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        private String Language;
        private String GenerateOT;
        private String DisplaySID;
        private String DisplayTID;
        private String FullDisplayTID;
    }
}
