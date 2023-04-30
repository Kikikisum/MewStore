package com.example.Controller;

import com.alibaba.fastjson.JSON;
import com.example.Entity.*;
import com.example.Entity.Favorite;
import com.example.Service.FavoriteService;
import com.example.Service.GoodService;
import com.example.Service.UserService;
import com.example.Util.DecodeJwtUtils;
import com.example.Util.SnowFlakeUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
public class FavoriteController {

    Map<String, Object> map=new HashMap<>();
    @Autowired
    private DecodeJwtUtils decodeJwtUtils;
    private UserService userService;
    private FavoriteService favoriteService;
    private GoodService goodService;

    @PostMapping("fav/{id}")
    public String fav(HttpServletRequest request, @PathVariable("id") Long good_id)
    {
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        User user=userService.getUserById(uid);
        int user_status = user.getStatus();
        if(decodeJwtUtils.validity(token))
        {
            if(user_status==1)
            {
                map.put("code",201);
                map.put("msg","黑户无法收藏");
            }
            else if (user_status==2)
            {
                map.put("code",201);
                map.put("msg","用户处于冻结状态，无法收藏");
            }
            else
            {
                Good good=goodService.queryById(good_id);
                if(good!= null)
                {
                    Favorite favorite=new Favorite(uid,good_id);
                    favoriteService.InsertFavorite(favorite);
                    map.put("code",200);
                    map.put("msg","收藏成功");
                }
                else
                {
                    map.put("code",404);
                    map.put("msg","商品不存在");
                }
            }
        }
        else
        {
            map.put("code",401);
            map.put("msg","登录过期");
        }
        return JSON.toJSONString(map);
    }

}
