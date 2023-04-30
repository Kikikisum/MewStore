package com.example.Service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.Entity.Favorite;
import com.example.Mapper.FavoriteMapper;
import com.example.Mapper.UserMapper;
import com.example.Service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;

public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService{
    @Autowired
    FavoriteMapper favoriteMapper;

    @Override
    public int InsertFavorite(Favorite favorite)
    {
        return favoriteMapper.insert(favorite);
    }

}
