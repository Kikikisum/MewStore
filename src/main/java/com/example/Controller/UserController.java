package com.example.Controller;

import com.alibaba.fastjson.JSON;
import com.example.Entity.*;
import com.example.Mapper.FavoriteMapper;
import com.example.Service.*;
import com.example.Service.Impl.WebSocketServer;
import com.example.Util.DecodeJwtUtils;
import com.example.Util.SnowFlakeUtil;
import org.apache.xmlbeans.impl.xb.xsdschema.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
@RestController
public class UserController {

    @Resource
    DecodeJwtUtils decodeJwtUtils;

    @Resource
    UserService userService;

    @Resource
    GoodService goodService;

    @Resource
    OrderService orderService;

    @Resource
    ReportService reportService;

    @Resource
    WebSocketServer webSocketServer;

    @Resource
    MessageService messageService;

    private final SnowFlakeUtil snowFlakeUtil=new SnowFlakeUtil(1,4,0,9666666666L);

    private final SnowFlakeUtil MessageSnowFlakeUtil=new SnowFlakeUtil(4,1,0,1366666666666L);

    private final Timestamp timestamp=new Timestamp(System.currentTimeMillis());
    //管理员审核商品
    @PutMapping("/good/verify/{id}")
    public String dealGood(HttpServletRequest request, @PathVariable("id")Long id,int status)
    {
        Map<String, Object> map=new HashMap<>();
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int userStatus=user.getStatus();
        if(decodeJwtUtils.validity(token))
        {
            Good good=goodService.queryById(id);
            if(good.getStatus()==0)
            {
                good.setStatus(status);
                goodService.updateById(good);
                if(userStatus==3)
                {
                    if(status==-1)
                    {
                        map.put("code",201);
                        map.put("msg","不通过审核成功");
                    }
                    else if(status==1)
                    {
                        map.put("code",201);
                        map.put("msg","通过审核成功");
                    }
                }
                else
                {
                    map.put("code",401);
                    map.put("msg","用户没有权限审核商品");
                }
            }
            else
            {
                map.put("code",400);
                map.put("msg","请勿重复审核商品");
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录超时");
        }
        return JSON.toJSONString(map);
    }

    //买家举报买家的类型1，卖家试图找回账户
    @PostMapping ("/freeze")
    public String freeze(HttpServletRequest request,Long order_id,String reason)
    {
        Map<String, Object> map=new HashMap<>();
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int userStatus=user.getStatus();
        if(decodeJwtUtils.validity(token))
        {
            if(userStatus==1)
            {
                map.put("code",401);
                map.put("msg","黑户不能举报");
            }
            else if (userStatus==2)
            {
                map.put("code",401);
                map.put("msg","已处于冻结状态，无法申请找回账号");
            }
            else
            {
                Order order=orderService.getOrderById(order_id);
                Good good=goodService.queryById(order.getGood_id());
                if (good.getStatus()==3)
                {
                    good.setStatus(2);
                    goodService.updateById(good);
                    user.setStatus(2);
                    userService.updateById(user);
                    Report report=new Report();
                    report.setId(snowFlakeUtil.nextId());
                    report.setStatus(0);
                    report.setReported_id(order.getSeller_id());
                    report.setReport_order(order_id);
                    report.setContent(reason);
                    report.setReporter_id(user.getId());
                    report.setType(1);
                    reportService.InsertReport(report);
                    map.put("code",201);
                    map.put("msg","举报卖家找回账号成功");
                }
                else
                {
                    map.put("code",400);
                    map.put("msg","商品未出售，不允许找回");
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

    //处理类型1的举报,id为举报id
    @PutMapping("/freeze/deal")
    public String dealFreeze(HttpServletRequest request,int status,Long id)
    {
        Map<String, Object> map=new HashMap<>();
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int userStatus=user.getStatus();
        if(decodeJwtUtils.validity(token))
        {
            if(userStatus==3)
            {
                Report report=reportService.getReportById(id);
                if(report!=null)
                {
                    report.setStatus(status);
                    reportService.updateById(report);
                    User buyer=userService.getUserById(report.getReporter_id());
                    User seller=userService.getUserById(report.getReported_id());
                    Order order=orderService.getOrderById(report.getReport_order());
                    if(status==1)
                    {
                        map.put("code",200);
                        map.put("msg","同意找回账户成功");
                        Map<String,Object> messageMap=reportService.ReportMap(report);
                        messageMap.put("msg","您的举报通过!");
                        Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,report.getReporter_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                        messageMap.put("message_id",message.getId());
                        messageService.InsertMessage(message);
                        webSocketServer.sendMessage(report.getReporter_id(),JSON.toJSONString(messageMap));
                        BigDecimal price=order.getPrice();
                        BigDecimal money=seller.getMoney();
                        int flag=money.compareTo(price);
                        if(flag>=0)
                        {
                            seller.setStatus(0);
                            seller.setMoney(money.subtract(price));
                            BigDecimal money2=buyer.getMoney();
                            buyer.setMoney(money2.add(price));
                            userService.updateById(seller);
                            userService.updateById(buyer);
                            messageMap.put("msg","您被举报了，已成功为买家退款,同时给你扣款!");
                            Message message1=new Message(MessageSnowFlakeUtil.nextId(),true,6L,order.getSeller_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                            messageMap.put("message_id",message1.getId());
                            messageService.InsertMessage(message1);
                            webSocketServer.sendMessage(report.getReported_id(),JSON.toJSONString(messageMap));
                        }
                        else
                        {
                            seller.setStatus(1);
                            seller.setMoney(BigDecimal.valueOf(0));
                            userService.updateById(seller);
                            BigDecimal money2=buyer.getMoney();
                            buyer.setMoney(money2.add(price));
                            userService.updateById(seller);
                            userService.updateById(buyer);
                            messageMap.put("msg","您被举报了，账户余额不足，您被拉入黑名单");
                            Message message1=new Message(MessageSnowFlakeUtil.nextId(),true,6L,order.getSeller_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                            messageMap.put("message_id",message1.getId());
                            messageService.InsertMessage(message1);
                            webSocketServer.sendMessage(report.getReported_id(),JSON.toJSONString(messageMap));
                        }
                    }
                    else if(status==-1)
                    {
                        seller.setStatus(0);
                        userService.updateById(seller);
                        map.put("code",200);
                        map.put("msg","拒绝申请成功");
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
                    map.put("code",401);
                    map.put("msg","不可重复处理申请");
                }

            }
            else
            {
                map.put("code",401);
                map.put("msg","没有权限审核账号找回");
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录超时");
        }
        return JSON.toJSONString(map);
    }


    //id为举报的id
    @PostMapping("/cancel/{id}")
    public String cancelOrder(HttpServletRequest request,@PathVariable Long id,int damage)
    {
        Map<String, Object> map=new HashMap<>();
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int userStatus=user.getStatus();
        if(decodeJwtUtils.validity(token))
        {
            if(userStatus==3)
            {
                Report report=reportService.getReportById(id);
                if(damage==-1)
                {
                    report.setStatus(-1);
                    reportService.updateById(report);
                    Map<String,Object> messageMap=reportService.ReportMap(report);
                    messageMap.put("msg","您的取消交易的申请被卖家拒绝!");
                    Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,report.getReporter_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                    messageMap.put("message_id",message.getId());
                    messageService.InsertMessage(message);
                    webSocketServer.sendMessage(report.getReported_id(),JSON.toJSONString(messageMap));
                }
                else
                {
                    report.setStatus(1);
                    reportService.updateById(report);
                    User buyer=userService.getUserById(report.getReporter_id());
                    User seller=userService.getUserById(report.getReported_id());
                    Order order=orderService.getOrderById(report.getReport_order());
                    BigDecimal price=order.getPrice();
                    BigDecimal buyer_money=buyer.getMoney();
                    BigDecimal seller_money=seller.getMoney();
                    if(damage==0)
                    {
                        //先退还买家资金
                        buyer.setMoney(buyer_money.add(price));
                        userService.updateById(buyer);
                        Map<String,Object> messageMap=reportService.ReportMap(report);
                        messageMap.put("msg","您的交易金已全部退回您的钱包!");
                        Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,report.getReporter_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                        messageMap.put("message_id",message.getId());
                        messageService.InsertMessage(message);
                        webSocketServer.sendMessage(report.getReporter_id(),JSON.toJSONString(message));
                        if(seller_money.compareTo(price)!=-1)
                        {
                            seller.setMoney(seller_money.subtract(price));
                            userService.updateById(seller);
                        }
                        else
                        {
                            seller.setMoney(BigDecimal.valueOf(0)); //账号金额不足时，扣光账户所有资金
                            seller.setStatus(2);  //设置为黑户状态
                            userService.updateById(seller);
                            messageMap.put("msg","您的账户余额不足，您被设置为黑户");
                            Message message1=new Message(MessageSnowFlakeUtil.nextId(),true,6L,report.getReported_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                            messageMap.put("message_id",message1.getId());
                            messageService.InsertMessage(message1);
                            webSocketServer.sendMessage(report.getReported_id(),JSON.toJSONString(messageMap));
                        }
                    }
                    if(damage==1) //稍微受损以3:7的比例返回
                    {
                        buyer.setMoney(buyer_money.add(price.multiply(BigDecimal.valueOf(0.7))));
                        userService.updateById(buyer);
                        Map<String,Object> messageMap=reportService.ReportMap(report);
                        messageMap.put("msg","您的交易金70%已退回你的钱包!");
                        Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,report.getReporter_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                        messageMap.put("message_id",message.getId());
                        messageService.InsertMessage(message);
                        webSocketServer.sendMessage(report.getReporter_id(),JSON.toJSONString(message));
                        if(seller_money.compareTo(price.multiply(BigDecimal.valueOf(0.7)))!=-1)
                        {
                            seller.setMoney(seller_money.add(price.multiply(BigDecimal.valueOf(0.7))));
                            userService.updateById(seller);
                        }
                        else
                        {
                            seller.setMoney(BigDecimal.valueOf(0)); //账号金额不足时，扣光账户所有资金
                            seller.setStatus(2);  //设置为黑户状态
                            userService.updateById(seller);
                            messageMap.put("msg","您的账户余额不足，您被设置为黑户");
                            Message message1=new Message(MessageSnowFlakeUtil.nextId(),true,6L,report.getReported_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                            messageMap.put("message_id",message1.getId());
                            messageService.InsertMessage(message1);
                            webSocketServer.sendMessage(report.getReported_id(),JSON.toJSONString(messageMap));
                        }
                    }
                    if(damage==2) //严重受损则不退还交易金
                    {
                        Map<String,Object> messageMap=reportService.ReportMap(report);
                        messageMap.put("msg","由于账户严重受损，将不进行返回交易金!");
                        Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,report.getReporter_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                        messageMap.put("message_id",message.getId());
                        messageService.InsertMessage(message);
                        webSocketServer.sendMessage(report.getReporter_id(),JSON.toJSONString(message));
                    }
                }
                map.put("code",201);
                map.put("msg","处理取消交易成功!");
            }
            else
            {
                map.put("code",401);
                map.put("msg","没有权限处理取消交易");
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
