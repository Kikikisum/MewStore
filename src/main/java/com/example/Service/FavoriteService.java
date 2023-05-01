package com.example.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Entity.Favorite;
import com.example.Entity.Order;

public interface FavoriteService extends IService<Favorite> {
    public int InsertFavorite(Favorite favorite);
}
