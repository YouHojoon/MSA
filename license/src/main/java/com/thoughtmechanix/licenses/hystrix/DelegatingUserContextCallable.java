package com.thoughtmechanix.licenses.hystrix;

import com.thoughtmechanix.licenses.util.UserContext;
import com.thoughtmechanix.licenses.util.UserContextHolder;
import jdk.vm.ci.code.site.Call;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;

@AllArgsConstructor
public class DelegatingUserContextCallable<V> implements Callable<V> {
    private final Callable<V> delegate;
    private UserContext originalUserContext;
    public V call() throws Exception{
        UserContextHolder.setContext(originalUserContext);
        try {
            return delegate.call();
        }finally {
            this.originalUserContext=null;
        }
    }
    public static <V> Callable<V> create(Callable<V> delegate, UserContext userContext){
        return new DelegatingUserContextCallable<V>(delegate,userContext);
    }
}
