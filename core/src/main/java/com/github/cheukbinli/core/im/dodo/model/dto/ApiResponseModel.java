package com.github.cheukbinli.core.im.dodo.model.dto;

import lombok.Data;

@Data
public class ApiResponseModel<T> {

    private int status;//	int	返回码
    private String message;//	string	返回信息
    private T data;//	object	返回数据


}
