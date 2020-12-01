package com.thoughtmechanix.licenses.util;

import org.springframework.util.Assert;

public class UserContextHolder {
    private static final ThreadLocal<UserContext> USER_CONTEXT = new ThreadLocal<>();
    public static final UserContext getContext(){
        UserContext context = USER_CONTEXT.get();
        if(context==null){
            context=createEmptyContext();
            USER_CONTEXT.set(context);
        }
        return USER_CONTEXT.get();
    }
    public static final UserContext createEmptyContext(){
        return new UserContext();
    }
    public static final void setContext(UserContext context){
        Assert.notNull(context,"거부");
        USER_CONTEXT.set(context);
    }

}
