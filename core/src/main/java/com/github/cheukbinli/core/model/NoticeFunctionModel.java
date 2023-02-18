package com.github.cheukbinli.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class NoticeFunctionModel<T, A> {

    private int code = 0;
    private int level = 0;
    private String msg;
    private T data;
    private A additional;

}
