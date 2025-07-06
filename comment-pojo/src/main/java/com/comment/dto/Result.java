package com.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一返回结果
 * @param <T>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

    private Boolean success;
    private String errorMsg;
    private T data;
    private Long total;

    /**
     * 不带数据成功请求
     *
     * @return Result 响应结果
     */
    public static <T> Result<T> ok(){
        return new Result<>(true, null, null, null);
    }

    /**
     *
     * 带数据成功请求
     * @param data 响应数据
     * @return Result 响应结果
     */
    public static <T> Result<T> ok(T data){
        return new Result<>(true, null, data, null);
    }

    public static <T> Result<T> ok(T data, Long total){
        return new Result<>(true, null, data, total);
    }

    public static <T> Result<T> fail(String errorMsg){
        return new Result<>(false, errorMsg, null, null);
    }
}
