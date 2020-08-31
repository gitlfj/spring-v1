package com.lfj.dome.framework.aop.config;

/**
 * AOP config 配置
 */
public class LAopConfig {

    /**
     *  切面表达式
     */
    private String pointCut;

    /**
     *  切面类
     */
    private String aspectClass;

    /**
     *  前置通知方法名
     */
    private String aspectBefore;

    /**
     *  后置通知方法名
     */
    private String aspectAfter;

    /**
     *  异常通知方法名
     */
    private String aspectThrow;

    /**
     *  异常类型
     */
    private String throwTypeClassName;

    public String getPointCut() {
        return pointCut;
    }

    public void setPointCut(String pointCut) {
        this.pointCut = pointCut;
    }

    public String getAspectClass() {
        return aspectClass;
    }

    public void setAspectClass(String aspectClass) {
        this.aspectClass = aspectClass;
    }

    public String getAspectBefore() {
        return aspectBefore;
    }

    public void setAspectBefore(String aspectBefore) {
        this.aspectBefore = aspectBefore;
    }

    public String getAspectAfter() {
        return aspectAfter;
    }

    public void setAspectAfter(String aspectAfter) {
        this.aspectAfter = aspectAfter;
    }

    public String getAspectThrow() {
        return aspectThrow;
    }

    public void setAspectThrow(String aspectThrow) {
        this.aspectThrow = aspectThrow;
    }

    public String getThrowTypeClassName() {
        return throwTypeClassName;
    }

    public void setThrowTypeClassName(String throwTypeClassName) {
        this.throwTypeClassName = throwTypeClassName;
    }

}
