package com.xinyi.web.controller.rongli.scope;

import com.xinyi.rongli.identity.dto.ViSuccess;
import com.xinyi.rongli.platform.actor.Actor;
import com.xinyi.rongli.platform.actor.BusinessActorResolver;
import com.xinyi.rongli.scope.dto.AuthorizedStation;
import com.xinyi.rongli.scope.service.StationGrantService;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 员工获授权站点（I06，API_094，MINI 域）。仅本人有效绑定+启用站点；无绑定返回空；
 * 不暴露无权站点单量/司机/订单。响应 no-store。
 */
@RestController
@RequestMapping("/api/app/station")
public class AuthorizedStationController {

    private final StationGrantService grantService;
    private final BusinessActorResolver actorResolver;

    public AuthorizedStationController(StationGrantService grantService, BusinessActorResolver actorResolver) {
        this.grantService = grantService;
        this.actorResolver = actorResolver;
    }

    @GetMapping("/authorized-stations")
    public ResponseEntity<ViSuccess<List<AuthorizedStation>>> authorizedStations() {
        Actor actor = actorResolver.requireMini();
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ViSuccess.ok(grantService.authorizedStations(actor.accountId())));
    }
}