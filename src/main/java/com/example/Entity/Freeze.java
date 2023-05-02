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
@TableName(value = "freeze")
public class Freeze {
    private Long user_id;
    private Long order_id;
    private String reason;
    private int status;
}
