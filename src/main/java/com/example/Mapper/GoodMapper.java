package com.example.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.Entity.Good;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GoodMapper extends BaseMapper<Good> {
}
