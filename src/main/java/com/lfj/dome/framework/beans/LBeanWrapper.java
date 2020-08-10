package com.lfj.dome.framework.beans;

/**
 *  自定义 BeanWrapper
 */
public class LBeanWrapper {

    private Object instance;

    private Class<?> clazz;

    public LBeanWrapper() {
    }

    public LBeanWrapper(Object instance) {
        this.instance = instance;
        this.clazz = instance.getClass();
    }

    public Object getInstance() {
        return instance;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
