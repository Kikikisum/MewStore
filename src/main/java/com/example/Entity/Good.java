package com.example.Entity;


import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "good")
public class Good {
    private Long id;
    private int view;
    private String content;
    private String game;
    private String title;
    private String account;
    private String password;
    private String picture;
    private int status;
    private Long seller_id;
    private Long price;
}
