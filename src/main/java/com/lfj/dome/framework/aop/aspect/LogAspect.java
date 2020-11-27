package com.lfj.dome.framework.aop.aspect;

/**
 *
 */
public class LogAspect {


    /**
     *  在调用一个方法之前，执行before方法
     */
    public void before(){
        //这个方法中的逻辑，是由我们自己写的
        System.out.println("AOP切面调用成功，Invoker Before Method!!!");
    }

    /**
     *  在调用一个方法之后，执行after方法
     */
    public void after(){
        System.out.println("AOP切面调用成功，Invoker After Method!!!");
    }

    public void afterThrowing(){
        System.out.println("AOP异常捕捉成功---出现异常");
    }

}
