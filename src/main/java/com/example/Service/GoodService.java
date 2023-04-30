package com.example.Service;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Entity.Good;

public interface GoodService {
    public Good queryById(Long id);
}
