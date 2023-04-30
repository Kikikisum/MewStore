package com.example.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.Good;
import com.example.Mapper.GoodMapper;
import com.example.Service.GoodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GoodServiceImpl extends ServiceImpl<GoodMapper,Good> implements GoodService {
    @Autowired
    private GoodMapper goodMapper;

    @Override
    public Good queryById(Long id)
    {
        LambdaQueryWrapper<com.example.Entity.Good> lqw1=new LambdaQueryWrapper<Good>();
        lqw1.eq(Good::getId,id);
        Good good=goodMapper.selectOne(lqw1);
        return good;
    }

}
