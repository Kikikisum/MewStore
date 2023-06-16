package com.example.Controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.Entity.Message;
import com.example.Entity.Order;
import com.example.Entity.Report;
import com.example.Entity.User;
import com.example.Mapper.ReportMapper;
import com.example.Service.Impl.WebSocketServer;
import com.example.Service.MessageService;
import com.example.Service.OrderService;
import com.example.Service.ReportService;
import com.example.Service.UserService;
import com.example.Util.DecodeJwtUtils;
import com.example.Util.SnowFlakeUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@CrossOrigin
@RestController
public class ReportController {

    @Resource
    Map<String, Object> map=new HashMap<>();

    @Resource
    private DecodeJwtUtils decodeJwtUtils;

    @Resource
    private UserService userService;

    @Resource
    private ReportMapper reportMapper;

    @Resource
    private ReportService reportService;

    @Resource
    private OrderService orderService;

    @Resource
    private MessageService messageService;

    @Resource
    private WebSocketServer webSocketServer;
    private final SnowFlakeUtil snowFlakeUtil=new SnowFlakeUtil(1,4,0,9666666666L);
    private final SnowFlakeUtil MessageSnowFlakeUtil=new SnowFlakeUtil(4,1,0,1366666666666L);
    private final Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    //根据举报的id来查找举报具体信息
    @GetMapping("/report")
    public String get_Report(HttpServletRequest request,Long id)
    {
        String token = request.getHeader("Authorization");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int status=user.getStatus();
        if(decodeJwtUtils.validity(token))
        {
            if (status==3)
            {
                LambdaQueryWrapper<Report> lqw1=new LambdaQueryWrapper<>();
                lqw1.eq(Report::getId,id);
                List<Report> reports=reportMapper.selectList(lqw1);
                if (reports.isEmpty())
                {
                    map.put("code",404);
                    map.put("msg","无该举报信息");
                }
                else {
                    map.put("code",201);
                    map.put("msg","查询成功");
                    map.put("data",reports);
                }

            }
            else
            {
                map.put("code",401);
                map.put("msg","没有权限访问");
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录超时");
        }
        return JSON.toJSONString(map);
    }

    //返回不同状态的举报
    @GetMapping("/report/{curPage}/{size}")
    public String report_status(HttpServletRequest request, int status,@PathVariable("curPage")int curPage,@PathVariable("size")int size)
    {
        String token=request.getHeader("Authorization");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        Page<Report> page=new Page<>(curPage,size);
        QueryWrapper<Report> wrapper=new QueryWrapper<>();
        wrapper.eq("status",status);
        IPage<Report> data=reportMapper.selectPage(page,wrapper);
        int userStatus=user.getStatus();
        if(decodeJwtUtils.validity(token))
        {
            if(userStatus==3)
            {
                map.put("code",200);
                map.put("msg","查询成功");
                map.put("data",data);
            }
            else
            {
                map.put("code",401);
                map.put("msg","没有权限访问");
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录超时");
        }
        return JSON.toJSONString(map);
    }

    //处理类型3的举报
    @PutMapping("/report/deal")
    public String deal_report(HttpServletRequest request,Long id,int status)
    {
        String token=request.getHeader("Authorization");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int userStatus=user.getStatus();
        if(decodeJwtUtils.validity(token))
        {
            if(userStatus==3)
            {
                Report report=reportService.getReportById(id);
                if (report.getStatus()==0)
                {
                    report.setStatus(status);
                    reportMapper.updateById(report);
                    if(status==1)
                    {
                        map.put("code",200);
                        map.put("msg","通过举报成功");
                        Map<String,Object> messageMap=reportService.ReportMap(report);
                        messageMap.put("msg","您的举报被通过!");
                        Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,report.getReporter_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                        messageMap.put("message_id",message.getId());
                        messageService.InsertMessage(message);
                        webSocketServer.sendMessage(report.getReporter_id(),JSON.toJSONString(messageMap));
                        messageMap.put("msg","您被举报了！");
                        Message message1=new Message(MessageSnowFlakeUtil.nextId(),true,6L,report.getReported_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                        messageMap.put("message_id",message1.getId());
                        messageService.InsertMessage(message1);
                        webSocketServer.sendMessage(report.getReported_id(),JSON.toJSONString(messageMap));
                    }
                    else if(status==-1)
                    {
                        map.put("code",200);
                        map.put("msg","拒绝举报成功");
                        Map<String,Object> messageMap=reportService.ReportMap(report);
                        messageMap.put("msg","您的举报被拒绝!");
                        Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,report.getReporter_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                        messageMap.put("message_id",message.getId());
                        messageService.InsertMessage(message);
                        webSocketServer.sendMessage(report.getReporter_id(),JSON.toJSONString(messageMap));
                    }
                }
                else
                {
                    map.put("code",404);
                    map.put("msg","举报已被处理，请勿重复处理");
                }
            }
            else
            {
                map.put("code",401);
                map.put("msg","没有权限处理举报");
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录超时");
        }
        return JSON.toJSONString(map);
    }

    //用户对举报的生成
    @PostMapping("/report/ini")
    public String iniReport(HttpServletRequest request,Long order_id,String content,int type)
    {
        String token=request.getHeader("Authorization");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int userStatus=user.getStatus();
        if(decodeJwtUtils.validity(token))
        {
            if(userStatus==1)
            {
                map.put("code",401);
                map.put("msg","黑户无法举报");
            }
            else if(userStatus==2)
            {
                map.put("code",401);
                map.put("msg","用户处于被冻结状态，无法举报");
            }
            else
            {
                Long rid=snowFlakeUtil.nextId();
                Order order=orderService.getOrderById(order_id);
                Long reported_id=order.getSeller_id();
                Report report=new Report(rid,reported_id,order_id,uid,0,content,type);
                reportService.InsertReport(report);
                map.put("code",201);
                map.put("msg","举报成功");
                if(type==2)
                {
                    Map<String,Object> messageMap=reportService.ReportMap(report);
                    messageMap.put("msg","您的订单被申请取消!");
                    Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,order.getSeller_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                    messageMap.put("message_id",message.getId());
                    messageService.InsertMessage(message);
                    webSocketServer.sendMessage(order.getSeller_id(),JSON.toJSONString(messageMap));
                }
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录超时");
        }
        return JSON.toJSONString(map);
    }

    //用户查询自身发出的举报
    @GetMapping("/myReport/{curPage}/{size}")
    public String myReport(HttpServletRequest request,@PathVariable("curPage")int curPage,@PathVariable("size")int size)
    {
        String token=request.getHeader("Authorization");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        Page<Report> page=new Page<>(curPage,size);
        QueryWrapper<Report> wrapper=new QueryWrapper<>();
        wrapper.like("reporter_id",uid);
        IPage<Report> data=reportMapper.selectPage(page,wrapper);
        if(decodeJwtUtils.validity(token))
        {
            map.put("code",200);
            map.put("msg","查询成功");
            map.put("data",data);
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录超时");
        }
        return JSON.toJSONString(map);
    }

    //管理员查询不同类型的举报
    @GetMapping("/report/type")
    public String getType(HttpServletRequest request,int type)
    {
        String token=request.getHeader("Authorization");
        Long uid=Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        if(decodeJwtUtils.validity(token))
        {
            if(user.getStatus()==3)
            {
                List<Report> reports=reportService.getByType(type);
                System.out.println(reports);
                map.put("code",201);
                map.put("msg","查询举报成功!");
                map.put("data",reports);
            }
            else
            {
                map.put("code",401);
                map.put("msg","没有权限查询举报");
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
