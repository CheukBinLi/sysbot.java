package com.github.cheukbinli.core.ns.model.request;

import com.github.cheukbinli.core.ns.model.CommandModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Map;

/***
 * 训练家
 */
public class DecodeTrainerRequest extends CommandModel<DecodeTrainerRequest.Param, Map> {

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        private String data;
    }
}
