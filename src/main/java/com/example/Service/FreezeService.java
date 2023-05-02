package com.example.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Entity.Favorite;
import com.example.Entity.Freeze;

public interface FreezeService extends IService<Freeze> {
    public int InsertFreeze(Freeze freeze);
    public Freeze queryById(Long id);
}
