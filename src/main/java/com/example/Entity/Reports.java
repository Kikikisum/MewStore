package com.example.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class Reports {
    private String id;
    private String reported_id;
    private String report_order;
    private String reporter_id;
    private int status;
    private String content;
    private int type; //1.试图找回账户 2.取消交易 3.拉入黑名单
}
