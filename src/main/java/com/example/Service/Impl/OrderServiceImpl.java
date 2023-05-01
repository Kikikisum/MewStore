package com.example.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.Order;
import com.example.Mapper.OrderMapper;
import com.example.Service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {
    @Autowired
    OrderMapper orderMapper;
    private Order order;

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


}
