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
@TableName(value = "order")
public class Order {
    private Long id;
    private int status;
    private Long buyer_id;
    private Long seller_id;
    private int buyer_status;
    private int seller_status;
    private Long money;
}