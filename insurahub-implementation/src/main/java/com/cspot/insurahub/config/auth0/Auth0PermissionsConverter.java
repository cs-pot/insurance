package com.cspot.insurahub.config.auth0;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class Auth0PermissionsConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String PERMISSIONS_CLAIM = "permissions";
    private static final String ROLES_CLAIM = "urn:insurahub:roles";
    private static final String ROLE_PREFIX = "ROLE_";

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        List<GrantedAuthority> authorities = new ArrayList<>();

        addPermissions(jwt, authorities);
        addRoles(jwt, authorities);

        return authorities;
    }

    private void addPermissions(
            Jwt jwt,
            Collection<GrantedAuthority> authorities
    ) {
        List<String> permissions =
                jwt.getClaimAsStringList(PERMISSIONS_CLAIM);

        if (permissions == null) {
            return;
        }

        permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
    }

    private void addRoles(
            Jwt jwt,
            Collection<GrantedAuthority> authorities
    ) {
        List<String> roles =
                jwt.getClaimAsStringList(ROLES_CLAIM);

        if (roles == null) {
            return;
        }

        roles.stream()
                .map(role -> new SimpleGrantedAuthority(ROLE_PREFIX + role))
                .forEach(authorities::add);
    }
}