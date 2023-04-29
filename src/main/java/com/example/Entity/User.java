package com.example.Entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "user")
public class User {
    private Long id;
    private String nickname;
    private String username;
    private String profile_photo;
    private String password;
    private String phone_number;
    private BigDecimal money;
    private int status;
    private String name;
    private String id_card;
}
