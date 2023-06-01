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
@TableName(value = "report")
public class Report {
    private Long id;
    private Long reported_id;
    private Long report_order;
    private Long reporter_id;
    private int status;
    private String content;
    private int type; //1.试图找回账户 2.取消交易 3.拉入黑名单
}
