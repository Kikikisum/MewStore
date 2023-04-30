package com.example.Controller;

import com.alibaba.fastjson.JSON;
import com.example.Entity.Favorite;
import com.example.Entity.Order;
import com.example.Entity.User;
import com.example.Util.DecodeJwtUtils;
import com.example.Util.SnowFlakeUtil;
import jakarta.servlet.http.HttpServletRequest;
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

    @PostMapping("fav/{id}")
    public String fav(HttpServletRequest request, @PathVariable("id") Long good_id)
    {
        String token = request.getHeader("token");
        Long uid = Long.valueOf(decodeJwtUtils.getId(token));
        int user_status = Integer.parseInt(decodeJwtUtils.getStatus(token));
        if(decodeJwtUtils.validity(token))
        {
            map.put("code",401);
            map.put("msg","登录过期");
        }
        else
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

            }
        }
        return JSON.toJSONString(map);
    }

}
