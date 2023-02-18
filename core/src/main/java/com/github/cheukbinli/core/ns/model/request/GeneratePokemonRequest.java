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
public class GeneratePokemonRequest extends CommandModel<GeneratePokemonRequest.Param, Map> {

    @lombok.Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Accessors(chain = true)
    public static class Param {
        public static String TXT_DATATYPE = "txt";
        public static String FILE_DATATYPE = "file";
        /***
         * txt/file
         */
        private String dataType;
        private List<String> data;
        private Map<String, Object> additional;
    }

}
