package com.thoughtmechanix.authentication.controller;

import com.netflix.discovery.converters.jackson.EurekaXmlJacksonCodec;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@RestController
public class AuthenticationController {
    @Value("${signing.key}")
    private String signingKey;
    @GetMapping("/user")
    public Map<String, Object> getUser(OAuth2Authentication user){
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("user", user.getUserAuthentication().getPrincipal());
        userInfo.put("authorities", AuthorityUtils.authorityListToSet(user.getUserAuthentication().getAuthorities()));
        return userInfo;
    }
    @PostMapping("/check")
    public void checkToken(String token) throws Exception {
        Claims claims= Jwts.parser().setSigningKey(signingKey.getBytes("UTF-8")).parseClaimsJws(token).getBody();
        log.info(claims.get("organizationId").toString());
    }
}
