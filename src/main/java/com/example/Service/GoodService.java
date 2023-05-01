package com.example.Service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Entity.Good;

public interface GoodService extends IService<Good> {
    public Good queryById(Long id);
}
