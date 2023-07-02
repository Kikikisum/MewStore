package com.example.Controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.Entity.*;
import com.example.Service.GoodService;
import com.example.Service.Impl.WebSocketServer;
import com.example.Service.MessageService;
import com.example.Service.OrderService;
import com.alibaba.fastjson.JSON;
import com.example.Service.UserService;
import com.example.Util.DecodeJwtUtils;
import com.example.Util.SnowFlakeUtil;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/order")
public class OrderController {
    @Resource
    private OrderService orderService;

    private final SnowFlakeUtil snowFlakeUtil =new SnowFlakeUtil(1, 3,0,466666666666L);
    private final SnowFlakeUtil MessageSnowFlakeUtil=new SnowFlakeUtil(4,1,0,1366666666666L);
    @Resource
    private GoodService goodService;

    @Resource
    private UserService userService;

    @Resource
    private WebSocketServer webSocketServer;

    @Resource
    private MessageService messageService;

    @ResponseBody
    @PostMapping("/bid")
    public String bid(HttpServletRequest request,Long good_id, BigDecimal money)
    {
        Map<String, Object> map=new HashMap<>();
        String token = request.getHeader("Authorization");
        Long id = Long.valueOf(DecodeJwtUtils.getId(token));
        System.out.println(id);
        User user=userService.getUserById(id);
        System.out.println(user);
        int status = user.getStatus();
        if(DecodeJwtUtils.validity(token))
        {
            if(status==1)
            {
                map.put("code",201);
                map.put("msg","黑户无法出价");
            }
            else if (status==2)
            {
                map.put("code",201);
                map.put("msg","用户处于冻结状态，无法出价");
            }
           else
            {
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                Long rid = snowFlakeUtil.nextId();
                Good good=goodService.queryById(good_id);
                Order order=new Order();
                order.setId(rid);
                order.setGood_id(good_id);
                order.setBuyer_id(id);
                order.setSeller_id(good.getSeller_id());
                order.setPrice(money);
                order.setStatus(2);
                order.setBuyer_status(0);
                order.setSeller_status(0);
                order.setGenerate_time(timestamp);
                order.setGood_title(good.getTitle());
                System.out.println(money.getClass().toString());
                orderService.InsertOrder(order);
                Map<String,Object> messageMap=orderService.getMap(order);
                messageMap.put("msg","商品有新的出价!");
                Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,order.getSeller_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                messageMap.put("message_id",message.getId());
                messageService.InsertMessage(message);
                webSocketServer.sendMessage(order.getSeller_id(),JSON.toJSONString(messageMap));
                map.put("code",200);
                map.put("msg","出价成功");
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录过期");
        }
        return JSON.toJSONString(map);
    }

    @ResponseBody
    @PutMapping("/pay")
    public String pay(HttpServletRequest request,Long id) //id是订单id
    {
        Map<String, Object> map=new HashMap<>();
        String token = request.getHeader("Authorization");
        Long uid = Long.valueOf(DecodeJwtUtils.getId(token));
        Order order=orderService.getOrderById(id);
        User user=userService.getUserById(uid);
        int status = user.getStatus();
        if(DecodeJwtUtils.validity(token))
        {
            if(status==1)
            {
                map.put("code",201);
                map.put("msg","黑户无法支付");
            }
            else if (status==2)
            {
                map.put("code",201);
                map.put("msg","用户处于冻结状态，无法支付");
            }
            else
            {
                if (order.getBuyer_status()==1)
                {
                    map.put("code",401);
                    map.put("msg","订单已支付");
                }
                else if(order.getSeller_status()==-1)
                {
                    map.put("code",401);
                    map.put("msg","订单已被卖家拒绝，不可支付!");
                }
                else {
                    User seller=userService.getUserById(order.getSeller_id());
                    BigDecimal price = order.getPrice();
                    BigDecimal money = user.getMoney();
                    BigDecimal seller_money = seller.getMoney();
                    int flag=money.compareTo(price);
                    if(flag>=0)
                    {
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        money=money.subtract(price);
                        seller_money=seller_money.add(price);
                        user.setMoney(money);
                        seller.setMoney(seller_money);
                        userService.updateById(user);
                        userService.updateById(seller);
                        order.setBuyer_status(1);
                        order.setStatus(0);
                        orderService.updateById(order);
                        Map<String,Object> messageMap=orderService.getMap(order);
                        messageMap.put("msg","您的订单被买家支付!");
                        Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,order.getSeller_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                        messageMap.put("message_id",message.getId());
                        messageService.InsertMessage(message);
                        webSocketServer.sendMessage(order.getSeller_id(),JSON.toJSONString(messageMap));
                        map.put("code",200);
                        map.put("msg","支付成功");
                    }
                    else
                    {
                        map.put("code",400);
                        map.put("msg","余额不足");
                    }
                }
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录过期");
        }
        if(order.getBuyer_status()==1)
        {
            if(order.getSeller_status()==1)
            {
                order.setStatus(1);
                orderService.updateById(order);
            }
        }
        return JSON.toJSONString(map);
    }

    @ResponseBody
    @PutMapping("/deal") //id是订单id
    public String sell(HttpServletRequest request,Long id,int status)
    {
        Map<String, Object> map=new HashMap<>();
        String token = request.getHeader("Authorization");
        Long uid = Long.valueOf(DecodeJwtUtils.getId(token));
        Order order=orderService.getOrderById(id);
        User user=userService.getUserById(uid);
        int user_status = user.getStatus();
        if(DecodeJwtUtils.validity(token))
        {
            if(user_status==1)
            {
                map.put("code",201);
                map.put("msg","黑户无法处理订单");
            }
            else if (user_status==2)
            {
                map.put("code",201);
                map.put("msg","用户处于冻结状态，无法处理订单");
            }
            else
            {
                if (order.getSeller_status()==0)
                {
                    order.setSeller_status(status);
                    orderService.updateById(order);
                    if(status==-1)
                    {
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        map.put("code",200);
                        map.put("msg","拒绝订单成功");
                        Map<String,Object> messageMap=orderService.getMap(order);
                        messageMap.put("msg","您的订单被卖家拒绝!");
                        Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,order.getBuyer_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                        messageMap.put("message_id",message.getId());
                        messageService.InsertMessage(message);
                        webSocketServer.sendMessage(order.getBuyer_id(),JSON.toJSONString(messageMap));
                        //检查如果订单已被支付进行退款
                        if(order.getBuyer_status()==1)
                        {
                            User seller=userService.getUserById(order.getSeller_id());
                            BigDecimal price=order.getPrice();
                            BigDecimal seller_money=seller.getMoney();
                            User buyer=userService.getUserById(order.getBuyer_id());
                            BigDecimal money=buyer.getMoney();
                            money=money.add(price);
                            seller_money=seller_money.subtract(price); //为负的处理？
                            buyer.setMoney(money);
                            seller.setMoney(seller_money);
                            userService.updateById(buyer);
                            userService.updateById(seller);
                            // 向买家发送退款的websocket消息
                            Message message1=new Message(MessageSnowFlakeUtil.nextId(),true,6L,order.getBuyer_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                            Map<String,Object> moneyMap=orderService.getMap(order);
                            moneyMap.put("msg","将订单进行退款!");
                            messageMap.put("message_id",message1.getId());
                            messageService.InsertMessage(message1);
                            webSocketServer.sendMessage(order.getBuyer_id(),JSON.toJSONString(moneyMap));
                        }
                    }
                    else
                    {
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        //变化状态，且把其他订单拒绝
                        Long good_id=order.getGood_id();
                        Good good=goodService.queryById(good_id);
                        good.setStatus(3);
                        goodService.updateById(good);
                        Map<String,Object> messageMap=orderService.getMap(order);
                        messageMap.put("msg","您的订单被买家同意!");
                        Message message=new Message(MessageSnowFlakeUtil.nextId(),true,6L,order.getBuyer_id(),JSON.toJSONString(messageMap),timestamp,0,false);
                        messageService.InsertMessage(message);
                        messageMap.put("message_id",message.getId());
                        webSocketServer.sendMessage(order.getBuyer_id(),JSON.toJSONString(messageMap));
                        map.put("code",200);
                        map.put("msg","确认订单成功");
                    }
                }
                else
                {
                    map.put("code",400);
                    map.put("msg","订单已处理，不可重复处理");
                }
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录过期");
        }
        if(order.getBuyer_status()==1)
        {
            if(order.getSeller_status()==1)
            {
                order.setStatus(1);
                orderService.updateById(order);
            }
        }
        return JSON.toJSONString(map);
    }

    //查询自身订单
    @GetMapping("Myorder/buy")
    public String getMyorder(HttpServletRequest request)
    {
        Map<String, Object> map=new HashMap<>();
        String token = request.getHeader("Authorization");
        Long uid = Long.valueOf(DecodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int user_status = user.getStatus();
        if(DecodeJwtUtils.validity(token))
        {
            if(user_status==1)
            {
                map.put("code",201);
                map.put("msg","黑户无法查询");
            }
            else if (user_status==2)
            {
                map.put("code",201);
                map.put("msg","用户处于冻结状态，无法查询");
            }
            else
            {
                LambdaQueryWrapper<Order> lqw1= new LambdaQueryWrapper<>();
                lqw1.eq(Order::getBuyer_id,uid);
                List<Order> data=orderService.list(lqw1);
                map.put("code",200);
                map.put("msg","查询成功");
                map.put("data",data);
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录过期");
        }
        return JSON.toJSONString(map);
    }

    //查询不同状态的订单
    @GetMapping("/status")
    public String getOrder(HttpServletRequest request,int status)
    {
        Map<String, Object> map=new HashMap<>();
        String token = request.getHeader("Authorization");
        Long uid = Long.valueOf(DecodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int user_status = user.getStatus();
        if(DecodeJwtUtils.validity(token))
        {
            if (user_status==3)
            {
                LambdaQueryWrapper<Order> lqw1= new LambdaQueryWrapper<>();
                lqw1.eq(Order::getStatus,status);
                List<Order> data=orderService.list(lqw1);
                map.put("code",200);
                map.put("msg","查询成功");
                map.put("data",data);
            }
            else {
                map.put("code",401);
                map.put("msg","用户没有权限查询");
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录过期");
        }
        return JSON.toJSONString(map);
    }
    @GetMapping("/my/sell")
    public String getMysell(HttpServletRequest request)
    {
        Map<String, Object> map=new HashMap<>();
        String token = request.getHeader("Authorization");
        Long uid = Long.valueOf(DecodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int user_status = user.getStatus();
        if(DecodeJwtUtils.validity(token))
        {
            if(user_status==1)
            {
                map.put("code",201);
                map.put("msg","黑户无法查询");
            }
            else if (user_status==2)
            {
                map.put("code",201);
                map.put("msg","用户处于冻结状态，无法查询");
            }
            else
            {
                LambdaQueryWrapper<Order> lqw1= new LambdaQueryWrapper<>();
                lqw1.eq(Order::getSeller_id,uid);
                List<Order> data=orderService.list(lqw1);
                map.put("code",200);
                map.put("msg","查询成功");
                map.put("data",data);
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录过期");
        }
        return JSON.toJSONString(map);
    }
}
