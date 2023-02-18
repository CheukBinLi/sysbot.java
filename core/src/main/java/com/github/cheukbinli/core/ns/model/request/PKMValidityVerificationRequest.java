package com.github.cheukbinli.core.ns.model.request;

import com.github.cheukbinli.core.ns.model.CommandModel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/***
 * 训练家
 */
public class PKMValidityVerificationRequest extends CommandModel<PKMValidityVerificationRequest.Param, Map> {

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Param {
        private List<String> data;
        private Map<String, String> additional;
    }

}
