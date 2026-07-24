package com.xinyi.framework.web.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import com.xinyi.common.core.domain.model.LoginUser;
import com.xinyi.system.domain.SysUserAuthState;

class TokenServiceTest
{
    private final TokenService tokenService = new TokenService();

    @Test
    void authorizationRequiresMatchingVersionAndActiveAccount()
    {
        LoginUser loginUser = new LoginUser();
        loginUser.setAuthVersion(3L);
        SysUserAuthState state = state(3L, "0", "0");

        assertThat(tokenService.isCurrentAuthorization(loginUser, state)).isTrue();
        assertThat(tokenService.isCurrentAuthorization(loginUser, state(4L, "0", "0"))).isFalse();
        assertThat(tokenService.isCurrentAuthorization(loginUser, state(3L, "1", "0"))).isFalse();
        assertThat(tokenService.isCurrentAuthorization(loginUser, state(3L, "0", "2"))).isFalse();
        assertThat(tokenService.isCurrentAuthorization(loginUser, null)).isFalse();
    }

    private SysUserAuthState state(Long version, String status, String delFlag)
    {
        SysUserAuthState state = new SysUserAuthState();
        state.setAuthVersion(version);
        state.setStatus(status);
        state.setDelFlag(delFlag);
        return state;
    }
}
