package com.lfj.dome.framework.aop;

import com.lfj.dome.framework.aop.aspect.LAdvice;
import com.lfj.dome.framework.aop.support.LAdvisedSupport;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * 代理类
 */
public class JdkDynamicAopProxy implements InvocationHandler {


    private LAdvisedSupport config;


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Map<String,LAdvice> advices = config.getAdvices(method,null);

        Object returnValue;
        try {
            invokeAdivce(advices.get("before"));

            returnValue = method.invoke(this.config.getTarget(),args);

            invokeAdivce(advices.get("after"));
        }catch (Exception e){
            invokeAdivce(advices.get("afterThrow"));
            throw e;
        }

        return returnValue;
    }


    private void invokeAdivce(LAdvice advice) {
        try {
            advice.getAdviceMethod().invoke(advice.getAspect());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public Object getProxy() {
        return Proxy.newProxyInstance(this.getClass().getClassLoader(),
                this.config.getTargetClass().getInterfaces(),this);
    }
}
