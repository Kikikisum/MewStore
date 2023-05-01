package com.example.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.Good;
import com.example.Entity.Report;
import com.example.Entity.User;
import com.example.Mapper.ReportMapper;
import com.example.Mapper.UserMapper;
import com.example.Service.ReportService;
import com.example.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    UserMapper userMapper;


    @Override
    public User getUserById(Long id) {
        LambdaQueryWrapper<User> lqw1=new LambdaQueryWrapper<User>();
        lqw1.eq(User::getId,id);
        User user=userMapper.selectOne(lqw1);
        return user;
    }


}
