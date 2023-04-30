package com.example.Service;

import com.example.Entity.Order;

import java.util.List;

public interface OrderService {
    //返回所有订单
    public List<Order> getAllOrder();
    //通过id查找订单
    public Order getOrderById(Long id);
    //插入订单
    public void InsertOrder(Order order);
    //通过状态查询订单
    public List<Order> getOrderByStatus(int status);
    //通过卖家id查找订单
    public List<Order> getOrderBySellId(Long seller_id);
    //通过卖家id查找订单
    public List<Order> getOrderByBuyerId(Long buyer_id);
}