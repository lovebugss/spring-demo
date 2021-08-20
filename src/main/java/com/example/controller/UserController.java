package com.example.controller;

import com.example.annotation.JsonOrFormBody;
import com.example.bean.User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户controller
 *
 * @author renjp
 * @date 2021/8/20 10:34
 */
@RestController
@RequestMapping("api/user")
public class UserController {
    @RequestMapping
    public User add(@Validated @JsonOrFormBody User user) {
        return user;
    }

    @RequestMapping("v2")
    public User addV2(@Validated @RequestBody User user) {
        return user;
    }

    @RequestMapping("v3")
    public User addV3(@Validated User user) {
        return user;
    }
}
