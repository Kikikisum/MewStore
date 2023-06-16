package com.example.Controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.Entity.Message;
import com.example.Mapper.MessageMapper;
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
    Map<String, Object> map=new HashMap<>();
    @Resource
    private DecodeJwtUtils decodeJwtUtils;
    @Resource
    private MessageMapper messageMapper;
    @Resource
    private MessageService messageService;

    //查询自身的未读系统消息
    @GetMapping("/system/messages")
    public String get_SystemMessage(HttpServletRequest request)
    {
        String token=request.getHeader("Authorization");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        if(decodeJwtUtils.validity(token))
        {
            LambdaQueryWrapper<Message> lqw=new LambdaQueryWrapper<>();
            lqw.eq(Message::getReceive_id,uid);
            lqw.eq(Message::is_read,false);
            List<Message> messages=messageMapper.selectList(lqw);
            if(messages.isEmpty())
            {
                map.put("code",404);
                map.put("msg","无系统消息");
            }
            else
            {
                map.put("code",201);
                map.put("msg","查询成功");
                map.put("data",messages);
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录超时");
        }
        return JSON.toJSONString(map);
    }

    @PostMapping("read/system/{id}")
    public String read_SystemMessage(HttpServletRequest request, @PathVariable("id") Long id)
    {
        String token=request.getHeader("Authorization");
        if(decodeJwtUtils.validity(token))
        {
            Message message=messageService.getMessage(id);
            message.set_read(true);
            messageMapper.updateById(message);
            map.put("code",201);
            map.put("msg","消息设置已读成功");
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录超时");
        }
        return JSON.toJSONString(map);
    }

}
