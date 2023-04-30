package com.example.Controller;

import com.example.Entity.Good;
import com.example.Entity.Order;
import com.example.Entity.User;
import com.example.Mapper.GoodMapper;
import com.example.Mapper.OrderMapper;
import com.example.Mapper.UserMapper;
import com.example.Service.GoodService;
import com.example.Service.OrderService;
import com.alibaba.fastjson.JSON;
import com.example.Service.UserService;
import com.example.Util.DecodeJwtUtils;
import com.example.Util.SnowFlakeUtil;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RestController
public class OrderController {

    Map<String, Object>map=new HashMap<>();

    @Autowired
    private OrderService orderService;
    private DecodeJwtUtils decodeJwtUtils;
    private SnowFlakeUtil snowFlakeUtil =new SnowFlakeUtil(1, 3,0);
    private GoodService goodService;
    private UserService userService;
    private UserMapper userMapper;
    private OrderMapper orderMapper;
    private GoodMapper goodMapper;

    @PostMapping("/order/bid")
    public String bid(HttpServletRequest request, Long good_id, BigDecimal money)
    {
        String token = request.getHeader("token");
        Long id = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(id);
        int status = user.getStatus();
        if(decodeJwtUtils.validity(token))
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
                Long rid = snowFlakeUtil.nextId();
                Good good=goodService.queryById(good_id);
                Order order=new Order();
                order.setId(rid);
                order.setGood_id(good_id);
                order.setBuyer_id(id);
                order.setSeller_id(good.getSeller_id());
                order.setPrice(money);
                order.setStatus(0);
                order.setBuyer_status(0);
                order.setSeller_status(0);
                orderService.InsertOrder(order);
                map.put("code",400);
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

    @PostMapping("/order/pay/{id}")
    public String pay(HttpServletRequest request,@PathVariable("id") Long id) //id是订单id
    {
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int status = user.getStatus();
        if(decodeJwtUtils.validity(token))
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
                Order order=orderService.getOrderById(id);
                BigDecimal price = order.getPrice();
                BigDecimal money = user.getMoney();
                int flag=money.compareTo(price);
                if(flag>=0)
                {
                    money=money.subtract(price);
                    user.setMoney(money);
                    userMapper.updateById(user);
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
        else
        {
            map.put("code",401);
            map.put("msg","登录过期");
        }
        return JSON.toJSONString(map);
    }

    @PostMapping("order/deal/{id}") //id是订单id
    public String sell(HttpServletRequest request,@PathVariable("id") Long id,int status)
    {
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int user_status = user.getStatus();
        if(decodeJwtUtils.validity(token))
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
                Order order=orderService.getOrderById(id);
                order.setStatus(status);
                orderMapper.updateById(order);
                if(status==-1)
                {
                    map.put("code",200);
                    map.put("msg","拒绝订单成功");
                }
                else
                {
                    //变化状态，且把其他订单拒绝
                    Long good_id=order.getGood_id();
                    Good good=goodService.queryById(good_id);
                    good.setStatus(3);
                    goodMapper.updateById(good);
                    map.put("code",200);
                    map.put("msg","确认订单成功");
                }
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
