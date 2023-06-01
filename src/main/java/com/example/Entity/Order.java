package com.example.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TableName("orders")
public class Order {
    private Long id;
    private int status;
    private Long buyer_id;
    private Long seller_id;
    private Long good_id;
    private int buyer_status;
    private int seller_status;
    private BigDecimal price;
    private Timestamp generate_time;
}