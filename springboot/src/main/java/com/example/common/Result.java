package com.example.common;

import com.example.common.enums.ResultCodeEnum;

/**
 * Common result class
 * @param <T> Generic data type parameter
 */
public class Result<T> {
    private String code;
    private String msg;
    private T data;

    private Result(T data) {
        this.data = data;
    }

    public Result() {
    }

    public static <T> Result<T> success() {
        Result<T> tResult = new Result<>();
        tResult.setCode(ResultCodeEnum.SUCCESS.code);
        tResult.setMsg(ResultCodeEnum.SUCCESS.msg);
        return tResult;
    }

    public static <T> Result<T> success(T data) {
        Result<T> tResult = new Result<>(data);
        tResult.setCode(ResultCodeEnum.SUCCESS.code);
        tResult.setMsg(ResultCodeEnum.SUCCESS.msg);
        return tResult;
    }

    public static <T> Result<T> error() {
        Result<T> tResult = new Result<>();
        tResult.setCode(ResultCodeEnum.SYSTEM_ERROR.code);
        tResult.setMsg(ResultCodeEnum.SYSTEM_ERROR.msg);
        return tResult;
    }

    public static <T> Result<T> error(String code, String msg) {
        Result<T> tResult = new Result<>();
        tResult.setCode(code);
        tResult.setMsg(msg);
        return tResult;
    }

    public static <T> Result<T> error(ResultCodeEnum resultCodeEnum) {
        Result<T> tResult = new Result<>();
        tResult.setCode(resultCodeEnum.code);
        tResult.setMsg(resultCodeEnum.msg);
        return tResult;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
