package com.github.cheukbinli.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FunctionResult<T> {

    public static FunctionResult DEFAULT_RESULT = new FunctionResult();

    private int code;
    private String msg;
    private Throwable error;
    private T data;

    public boolean isSuccess() {
        return code == 0;
    }

}
