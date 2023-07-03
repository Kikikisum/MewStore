package com.example.Entity;

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
public class Orders {
    private String id;
    private int status;
    private String buyer_id;
    private String seller_id;
    private String good_id;
    private int buyer_status;
    private int seller_status;
    private BigDecimal price;
    private String generate_time;
    private String good_title;
}
