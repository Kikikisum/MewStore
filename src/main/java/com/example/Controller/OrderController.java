package com.example.Controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;
    @Autowired
    private DecodeJwtUtils decodeJwtUtils;

    private SnowFlakeUtil snowFlakeUtil =new SnowFlakeUtil(1, 3,0,466666666666L);
    @Autowired
    private GoodService goodService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private GoodMapper goodMapper;

    @ResponseBody
    @PostMapping("/bid")
    public String bid(HttpServletRequest request,Long uid,Long good_id, BigDecimal money)
    {
        Map<String, Object>map=new HashMap<>();
        String token = request.getHeader("token");
        Long id = Long.valueOf(decodeJwtUtils.getId(token));
        System.out.println(id);
        User user=userService.getUserById(id);
        System.out.println(user);
        int status = user.getStatus();
        if(decodeJwtUtils.validity(token))
        {
            if(status==1)
            {
                map.put("code",401);
                map.put("msg","黑户无法出价");
            }
            else if (status==2)
            {
                map.put("code",401);
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
                System.out.println(money.getClass().toString());
                orderService.InsertOrder(order);
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
    @GetMapping("/pay/{id}")
    public String pay(HttpServletRequest request,@PathVariable("id") Long id) //id是订单id
    {
        Map<String, Object>map=new HashMap<>();
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        Order order=orderService.getOrderById(id);
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
                if (order.getBuyer_status()==1)
                {
                    map.put("code",401);
                    map.put("msg","订单已支付");
                }
                else {
                    BigDecimal price = order.getPrice();
                    BigDecimal money = user.getMoney();
                    int flag=money.compareTo(price);
                    if(flag>=0)
                    {
                        money=money.subtract(price);
                        user.setMoney(money);
                        userMapper.updateById(user);
                        order.setBuyer_status(1);
                        orderMapper.updateById(order);
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
    @PutMapping("/deal/{id}") //id是订单id
    public String sell(HttpServletRequest request,@PathVariable("id") Long id,int status)
    {
        Map<String, Object>map=new HashMap<>();
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        Order order=orderService.getOrderById(id);
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
                if (order.getSeller_status()==0)
                {
                    order.setSeller_status(status);
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
    @GetMapping("Myorder/{curPage}/{size}")
    public String getMyorder(HttpServletRequest request,@PathVariable("curPage")int curPage,@PathVariable("size")int size)
    {
        Map<String, Object>map=new HashMap<>();
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int user_status = user.getStatus();
        if(decodeJwtUtils.validity(token))
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
                Page<Order> page = new Page<>(curPage, size);
                QueryWrapper<Order> wrapper = new QueryWrapper<>();
                wrapper.like("buyer_id", uid);
                IPage<Order> data = orderMapper.selectPage(page, wrapper);
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
    @GetMapping("/{curPage}/{size}")
    public String getOrder(HttpServletRequest request,@PathVariable("curPage")int curPage,@PathVariable("size")int size,int status)
    {
        Map<String, Object>map=new HashMap<>();
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int user_status = user.getStatus();
        if(decodeJwtUtils.validity(token))
        {
            if (user_status==3)
            {
                Page<Order> page = new Page<>(curPage, size);
                QueryWrapper<Order> wrapper = new QueryWrapper<>();
                wrapper.eq("status", status);
                IPage<Order> data = orderMapper.selectPage(page, wrapper);
                map.put("code",200);
                map.put("msg","查询成功");
                map.put("data",data);
            }
            else {
                map.put("code",401);
                map.put("msg","用户没有权限访问");
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录过期");
        }
        return JSON.toJSONString(map);
    }
    @GetMapping("Mysell/{curPage}/{size}")
    public String getMysell(HttpServletRequest request,@PathVariable("curPage")int curPage,@PathVariable("size")int size)
    {
        Map<String, Object>map=new HashMap<>();
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int user_status = user.getStatus();
        if(decodeJwtUtils.validity(token))
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
                Page<Order> page = new Page<>(curPage, size);
                QueryWrapper<Order> wrapper = new QueryWrapper<>();
                wrapper.like("seller_id", uid);
                IPage<Order> data = orderMapper.selectPage(page, wrapper);
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
