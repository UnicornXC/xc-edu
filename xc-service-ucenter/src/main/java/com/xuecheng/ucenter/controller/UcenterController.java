package com.xuecheng.ucenter.controller;

import com.xuecheng.api.ucenter.UcenterControllerApi;
import com.xuecheng.framework.domain.ucenter.ext.XcUserExt;
import com.xuecheng.ucenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/ucenter")
public class UcenterController implements UcenterControllerApi {

    @Autowired
    private UserService userService;

    @Override
    @GetMapping("/getUserExt")
    public XcUserExt getUserExt(@RequestParam("username") String username) {
        return userService.getUserExt(username);
    }
}
