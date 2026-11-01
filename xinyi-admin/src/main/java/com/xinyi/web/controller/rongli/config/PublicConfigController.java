package com.xinyi.web.controller.rongli.config;

import com.xinyi.rongli.config.dto.PublicServiceConfigView;
import com.xinyi.rongli.config.service.ConfigService;
import com.xinyi.rongli.identity.dto.ViSuccess;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 首页/登录前公开服务说明与已发布协议（C01，V6，PUBLIC 匿名）。仅公开白名单，no-store。 */
@RestController
@RequestMapping("/api/public")
public class PublicConfigController {

    private final ConfigService configService;

    public PublicConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping("/service-config")
    public ResponseEntity<ViSuccess<PublicServiceConfigView>> serviceConfig() {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ViSuccess.ok(configService.publicConfig()));
    }
}