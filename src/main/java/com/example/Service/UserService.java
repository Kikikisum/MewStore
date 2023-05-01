package com.example.Service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.Entity.Good;
import com.example.Entity.User;

public interface UserService extends IService<User> {
    public User getUserById(Long id);

}
