package com.example.Controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.Entity.Message;
import com.example.Entity.Order;
import com.example.Service.MessageService;
import com.example.Util.DecodeJwtUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
@RestController
public class MessageController {
    @Resource
    private DecodeJwtUtils decodeJwtUtils;
    @Resource
    private MessageService messageService;

    //查询自身的未读系统消息
    @GetMapping("/system/messages")
    public String get_SystemMessage(HttpServletRequest request)
    {
        Map<String, Object> map=new HashMap<>();
        String token=request.getHeader("Authorization");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        if(decodeJwtUtils.validity(token))
        {
            LambdaQueryWrapper<Message> lqw1=new LambdaQueryWrapper<Message>();
            lqw1.eq(Message::getReceive_id,uid);
           //lqw1.eq(Message::is_System,true);
            lqw1.eq(Message::isIs_read,false);
            lqw1.eq(Message::getSend_id,6L);
            List<Message> data=messageService.list(lqw1);
            if(data.isEmpty())
            {
                map.put("code",404);
                map.put("msg","无系统消息");
            }
            else
            {
                map.put("code",201);
                map.put("msg","查询成功");
                map.put("data",data);
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录超时");
        }
        return JSON.toJSONString(map);
    }

    @PostMapping("read/system")
    public String read_SystemMessage(HttpServletRequest request,Long id)
    {
        Map<String, Object> map=new HashMap<>();
        String token=request.getHeader("Authorization");
        if(decodeJwtUtils.validity(token))
        {
            Message message=messageService.getMessage(id);
            if(message.isIs_read()==false)
            {
                message.set_read(true);
                messageService.updateById(message);
                map.put("code",201);
                map.put("msg","消息设置已读成功");
            }
            else
            {
                map.put("code",401);
                map.put("msg","请勿重复设置已读");
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录超时");
        }
        return JSON.toJSONString(map);
    }

}
