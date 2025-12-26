package com.upi_switch.demo.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
public class ResponseDTO<T> {

    private boolean success;
    private T data;
    private String error;

    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<>(true, data, null);
    }

    public static <T> ResponseDTO<T> error(String error) {
        return new ResponseDTO<>(false, null, error);
    }
}
