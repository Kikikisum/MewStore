package com.example.Controller;

import com.alibaba.fastjson.JSON;
import com.example.Entity.Freeze;
import com.example.Entity.Good;
import com.example.Entity.Order;
import com.example.Entity.User;
import com.example.Mapper.FavoriteMapper;
import com.example.Service.FreezeService;
import com.example.Service.GoodService;
import com.example.Service.OrderService;
import com.example.Service.UserService;
import com.example.Util.DecodeJwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@CrossOrigin
@RestController
public class UserController {

    @Autowired
    DecodeJwtUtils decodeJwtUtils;
    @Autowired
    UserService userService;
    @Autowired
    GoodService goodService;
    @Autowired
    FreezeService freezeService;
    @Autowired
    OrderService orderService;

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
                map.put("msg","黑户不能申请找回账号");
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
                    Freeze freeze=new Freeze(uid,order_id,reason,0);
                    freezeService.InsertFreeze(freeze);
                    map.put("code",201);
                    map.put("msg","申请找回账号成功");
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

    //id为被冻结账户的用户id
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
                Freeze freeze=freezeService.queryById(id);
                if(freeze!=null)
                {
                    freeze.setStatus(status);
                    User user1=userService.getUserById(id);
                    if(status==1)
                    {
                        map.put("code",200);
                        map.put("msg","同意申请成功");
                        Order order=orderService.getOrderById(freeze.getOrder_id());
                        BigDecimal price=order.getPrice();
                        BigDecimal money=user1.getMoney();
                        int flag=money.compareTo(price);
                        if(flag>=0)
                        {
                            user1.setStatus(0);
                            user1.setMoney(money.subtract(price));
                            User user2=userService.getUserById(order.getBuyer_id());
                            BigDecimal money2=user2.getMoney();
                            user2.setMoney(money2.add(price));
                            userService.updateById(user1);
                            userService.updateById(user2);
                        }
                        else
                        {
                            User user2=userService.getUserById(order.getBuyer_id());
                            BigDecimal money2=user2.getMoney();
                            user2.setMoney(money2.add(price));
                            userService.updateById(user2);
                            user1.setStatus(2);
                            userService.updateById(user1);
                        }
                    }
                    else if(status==-1)
                    {
                        user1.setStatus(0);
                        userService.updateById(user1);
                        map.put("code",200);
                        map.put("msg","拒绝申请成功");
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

}
