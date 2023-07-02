package com.example.Entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.sql.Timestamp;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "messages")
public class Message {
    private Long id;  //聊天记录的雪花id
    private boolean isSystem; //是否来自系统
    private Long send_id; //发送者的id
    private Long receive_id; //接收者的id
    private String message;  //聊天的内容
    private Timestamp send_time; //发送的时间
    private int type;  //0为文本，1为图片
    private boolean is_read; //是否已读，默认未读

    public void Message(Long id,boolean isSystem,Long send_id,Long receive_id,String message,Timestamp send_time,int type,boolean is_read)
    {
        this.id=id;
        this.isSystem=isSystem;
        this.send_id=send_id;
        this.receive_id=receive_id;
        this.message=message;
        this.send_time=send_time;
        this.type=type;
        this.is_read=is_read;
    }

    public boolean isIs_read()
    {
        return is_read;
    }

}
