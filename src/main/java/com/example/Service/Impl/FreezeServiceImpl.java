package com.example.Service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.Freeze;
import com.example.Entity.Good;
import com.example.Mapper.FreezeMapper;
import com.example.Mapper.GoodMapper;
import com.example.Service.FreezeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FreezeServiceImpl extends ServiceImpl<FreezeMapper, Freeze> implements FreezeService {

    @Autowired
    private FreezeMapper freezeMapper;

    @Override
    public int InsertFreeze(Freeze freeze)
    {
        return baseMapper.insert(freeze);
    }

    @Override
    public Freeze queryById(Long id)
    {
        LambdaQueryWrapper<Freeze> lqw1=new LambdaQueryWrapper<Freeze>();
        lqw1.eq(Freeze::getUser_id,id);
        lqw1.eq(Freeze::getStatus,0);
        Freeze freeze=freezeMapper.selectOne(lqw1);
        return freeze;
    }

}
