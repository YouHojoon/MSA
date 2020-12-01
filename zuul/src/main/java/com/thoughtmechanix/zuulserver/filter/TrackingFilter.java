package com.thoughtmechanix.zuulserver.filter;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import com.thoughtmechanix.zuulserver.util.FilterUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.UUID;

@Slf4j
@Component
public class TrackingFilter extends ZuulFilter {
    private static final int FILTER_ORDER=1;
    private static final boolean SHOULD_FILTER=true;
    @Autowired
    private FilterUtils filterUtils;

    @Value("${signing.key}")
    private String signingKey;

    @Override
    public String filterType() {
        return FilterUtils.PRE_FILTER_TYPE;
    }

    @Override
    public int filterOrder() {
        return FILTER_ORDER;
    }

    @Override
    public boolean shouldFilter() {
        return SHOULD_FILTER;
    }
    @PostConstruct
    private void init(){
       signingKey=Base64.getEncoder().encodeToString(signingKey.getBytes());
    }
    @Override
    public Object run() throws ZuulException {
        if(isCorrelationIdPresent()){
            log.info("find-tmx-correlation-id:"+filterUtils.getCorrelationId());
        }
        else {
            filterUtils.setCorrelationId(generateCorrelationId());
            log.info("generate-tmx-correlation-id:"+filterUtils.getCorrelationId());
        }
        RequestContext ctx = RequestContext.getCurrentContext();
        log.info("request for-"+ctx.getRequest().getRequestURI());
        log.info("The organization id from the token is : " +getOrganizationId());
        return null;
    }
    private String getOrganizationId(){
        String result = "";
        if(filterUtils.getAuthToken()!=null){
            String authToken = filterUtils.getAuthToken().replace("Bearer ","");
            try{
                Claims claims= Jwts.parser().setSigningKey(signingKey)
                        .parseClaimsJws(authToken).getBody();
                result = (String) claims.get("organizationId");
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return result;
    }
    private String generateCorrelationId(){
        return UUID.randomUUID().toString();
    }
    private boolean isCorrelationIdPresent(){
        if(filterUtils.getCorrelationId()!=null)
            return true;
        return false;
    }


}
