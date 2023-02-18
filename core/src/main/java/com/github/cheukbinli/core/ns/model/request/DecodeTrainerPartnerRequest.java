package com.github.cheukbinli.core.ns.model.request;

import com.github.cheukbinli.core.ns.model.CommandModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/***
 * 训练家
 */
public class DecodeTrainerPartnerRequest extends CommandModel<DecodeTrainerPartnerRequest.Param, Map> {

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Param {
        private List<String> data;
    }
}
