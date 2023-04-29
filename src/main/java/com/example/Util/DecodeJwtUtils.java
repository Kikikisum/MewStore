package com.example.Util;

import cn.hutool.json.JSONObject;
import cn.hutool.jwt.JWT;
import cn.hutool.jwt.JWTUtil;

import java.nio.charset.StandardCharsets;


public class DecodeJwtUtils {

    private final static String key = "mewstore";



    public static String getBody(String token){
        JWT jwt = JWTUtil.parseToken(token);
        String body = String.valueOf(jwt.getPayloads());
        return body;
    }

    public static String getStatus(String token){
        JWT jwt = JWTUtil.parseToken(token);
        String status = String.valueOf(jwt.getPayload("status"));
        return status;
    }

    public static String getId(String token){
        JWT jwt = JWTUtil.parseToken(token);
        String id = String.valueOf(jwt.getPayload("id"));
        return id;
    }

    public static boolean validity(String token){
        JWT jwt = JWTUtil.parseToken(token);
        boolean validate = jwt.setKey(key.getBytes(StandardCharsets.UTF_8)).validate(0);
        return validate;
    }


}
