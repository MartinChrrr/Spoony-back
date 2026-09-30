package com.spoony.backend.application.rest.common;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class JSendResponse<T> {

    private String status;
    private T data;
    private String message;

    private JSendResponse() {
    }

    private JSendResponse(String status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public static <T> JSendResponse<T> success(T data) {
        return new JSendResponse<>("success", data, null);
    }

    public static JSendResponse<Map<String, String>> fail(String code, String message) {
        Map<String, String> errorData = new HashMap<>();
        errorData.put("code", code);
        errorData.put("message", message);
        return new JSendResponse<>("fail", errorData, null);
    }

    public static JSendResponse<Map<String, String>> fail(String code, String message, String field) {
        Map<String, String> errorData = new HashMap<>();
        errorData.put("code", code);
        errorData.put("message", message);
        errorData.put("field", field);
        return new JSendResponse<>("fail", errorData, null);
    }

    public static <T> JSendResponse<T> error(String message) {
        return new JSendResponse<>("error", null, message);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
