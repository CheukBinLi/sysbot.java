package com.github.cheukbinli.core.ns.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class CommandModel<P,D> implements Serializable {

    private static final long serialVersionUID = -7861237340281626382L;

    private String command;
    private P param;
    private D data;
    public int code;
    public String error;
}
