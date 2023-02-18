package com.github.cheukbinli.core.ns.model.response;

import com.github.cheukbinli.core.ns.model.CommandModel;
import lombok.Data;

import java.util.List;
import java.util.Map;

public class GeneratePokemonResponse extends CommandModel<Map, GeneratePokemonResponse.GeneratePokemonData> {

    @Data
    public static class GeneratePokemonData {
        List<String> data;
        List<Integer> species;
    }

}
