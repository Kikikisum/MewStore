package com.example.Service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.Order;
import com.example.Mapper.OrderMapper;
import com.example.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Autowired
    OrderMapper orderMapper;

    @Override
    public List<Order> getAllOrder()
    {
        return orderMapper.selectList(null);
    }

    @Override
    public Order getOrderById(Long id)
    {
        LambdaQueryWrapper<Order> lqw1=new LambdaQueryWrapper<Order>();
        lqw1.eq(Order::getId,id);
        Order order1=orderMapper.selectOne(lqw1);
        return order1;
    }

    @Override
    public void InsertOrder(Order order)
    {
        orderMapper.insert(order);
    }

    @Override
    public List<Order> getOrderByStatus(int status)
    {
        LambdaQueryWrapper<Order> lqw1=new LambdaQueryWrapper<Order>();
        lqw1.eq(Order::getStatus,status);
        List<Order> order1=orderMapper.selectList(lqw1);
        return order1;
    }
    @Override
    public List<Order> getOrderBySellId(Long seller_id)
    {
        LambdaQueryWrapper<Order> lqw1=new LambdaQueryWrapper<Order>();
        lqw1.eq(Order::getSeller_id,seller_id);
        List<Order> order=orderMapper.selectList(lqw1);
        return order;
    }
    @Override
    public List<Order> getOrderByBuyerId(Long buyer_id)
    {
        LambdaQueryWrapper<Order> lqw1=new LambdaQueryWrapper<Order>();
        lqw1.eq(Order::getBuyer_id,buyer_id);
        List<Order> order=orderMapper.selectList(lqw1);
        return order;
    }
    @Override
    public Map<String,Object> getMap(Order order)
    {
        Map<String,Object> map=new HashMap<>();
        map.put("id",order.getId());
        map.put("status",order.getStatus());
        map.put("buyer_id",order.getBuyer_id());
        map.put("seller_id",order.getSeller_id());
        map.put("good_id",order.getGood_id());
        map.put("buyer_status",order.getBuyer_status());
        map.put("seller_status",order.getSeller_status());
        map.put("price",order.getPrice());
        return map;
    }


}
