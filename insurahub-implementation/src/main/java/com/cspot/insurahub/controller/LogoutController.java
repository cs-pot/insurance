package com.cspot.insurahub.controller;

import com.cspot.insurahub.api.AuthenticationApi;
import com.cspot.insurahub.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class LogoutController implements AuthenticationApi {

    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout() {
        tokenBlacklistService.blacklistTokenFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication()
        );
    }
}
