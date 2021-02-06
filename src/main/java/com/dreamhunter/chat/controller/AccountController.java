package com.dreamhunter.chat.controller;

import com.dreamhunter.chat.model.Result;
import com.dreamhunter.chat.model.body.BodyLogin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Api(tags = "登录注册相关接口")
@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {

    @ApiOperation("登录接口")
    @PostMapping("/login")
    public Mono<Result> login(@Validated @RequestBody BodyLogin body) {
        return Mono.just(Result.success(body.getUsername()));
    }
}
