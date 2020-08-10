package com.lfj.dome.framework.beans.config;

/**
 *  自定义BeanDefinition，Spring会将所有的bean定义成BeanDefinition
 */
public class LBeanDefinition {

    /**
     *  bean name
     */
    private String factoryBeanName;

    /**
     *  class 全路径
     */
    private String beanClassName;


    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }
}
