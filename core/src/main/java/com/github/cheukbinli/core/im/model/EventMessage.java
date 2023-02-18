package com.github.cheukbinli.core.im.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage<T> {

    private String imType;
    private String event;
    private T message;

}
