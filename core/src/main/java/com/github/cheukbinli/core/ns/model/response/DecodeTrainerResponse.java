package com.github.cheukbinli.core.ns.model.response;

import com.github.cheukbinli.core.ns.model.CommandModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Map;

public class DecodeTrainerResponse extends CommandModel<Map, DecodeTrainerResponse.Data> {

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Data {
        private String Language;
        private String GenerateOT;
        private String DisplaySID;
        private String DisplayTID;
        private String FullDisplayTID;
        private long NintendoId;
        private String additional;
    }
}
