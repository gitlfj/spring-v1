package com.lfj.dome.framework.context;

import com.lfj.dome.framework.annotation.LAutowired;
import com.lfj.dome.framework.annotation.LController;
import com.lfj.dome.framework.annotation.LService;
import com.lfj.dome.framework.aop.JdkDynamicAopProxy;
import com.lfj.dome.framework.aop.config.LAopConfig;
import com.lfj.dome.framework.aop.support.LAdvisedSupport;
import com.lfj.dome.framework.beans.LBeanWrapper;
import com.lfj.dome.framework.beans.config.LBeanDefinition;
import com.lfj.dome.framework.beans.support.LBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.*;

/**
 *  自定义 ApplicationContext类
 * @author lifangjin
 */
public class LApplicationContext {

    /**
     *  读取配置文件解析成bean
     */
    private LBeanDefinitionReader definitionReader;

    /**
     *  beanDefinition 缓存
     */
    private Map<String, LBeanDefinition> beanDefinitionMap = new HashMap<String, LBeanDefinition>(16);

    /**
     *  beanWrapper 缓存
     */
    private Map<String, LBeanWrapper> beanFactoryInstanceCache = new HashMap<String, LBeanWrapper>(16);

    /**
     *  bean实例缓存，缓存原生对象
     */
    private Map<String, Object> beanFactoryObjectCache = new HashMap<String, Object>(16);


    /**
     *  构造函数初始化Spring容器
     * @param contextConfigLocations 配置文件
     */
    public LApplicationContext(String contextConfigLocations) {
        // 1. 加载配置文件
        definitionReader = new LBeanDefinitionReader(contextConfigLocations);

        // 2. 解析配置文件，封装成BeanDefinition：beanName， beanNameClass
        List<LBeanDefinition> lBeanDefinitionList = definitionReader.loadBeanDefinition();

        // 3. 把BeanDefinition缓存起来
        doRegisterBeanDefinition(lBeanDefinitionList);

        // 4. 依赖注入
        doAutowired();

        System.out.println("IOC 容器初始化完成。。。");
    }

    /**
     * 把BeanDefinition缓存起来
     * @param lBeanDefinitionList beanDefinition
     */
    private void doRegisterBeanDefinition(List<LBeanDefinition> lBeanDefinitionList) {
        if (lBeanDefinitionList == null || lBeanDefinitionList.size() == 0) {
            throw new RuntimeException("没有需要Spring管理的类！");
        }
        for (LBeanDefinition beanDefinition : lBeanDefinitionList) {
            if(this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                continue;
            }
            // 根据全类名缓存
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
            // 根据类名首字母小写缓存
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }
    }

    /**
     *  依赖注入
     */
    private void doAutowired() {
        for (Map.Entry<String, LBeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String factoryBeanName = beanDefinitionEntry.getKey();
            getBean(factoryBeanName);
        }
    }

    /**
     *  获取bean
     * @param beanName beanName默认类名首字母小写
     * @return Object
     */
    public Object getBean(String beanName) {
        //1 获取beanDefinition配置信息
        LBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        LBeanWrapper lBeanWrapper = new LBeanWrapper();
        try {
            // 2 反射实例化对象
            Object instance = instantiateInstance(beanDefinition, beanName);

            // 3 封装成 BeanWrapper
            lBeanWrapper = new LBeanWrapper(instance);

            // 4 ioc 注入
            beanFactoryInstanceCache.put(beanName, lBeanWrapper);

            // 5 依赖注入
            populateBean(beanName, beanDefinition, lBeanWrapper);
        }catch (Exception e) {
            e.getStackTrace();
        }
        return lBeanWrapper.getInstance();
    }

    /**
     *  创建实例
     * @param beanDefinition beanDefinition配置
     * @param beanName beanName
     * @return Object
     * @throws Exception  Exception
     */
    private Object instantiateInstance(LBeanDefinition beanDefinition, String beanName) throws Exception{
        Object instance = null;
        try {
            if (this.beanFactoryObjectCache.containsKey(beanName)) {
                instance =  this.beanFactoryObjectCache.get(beanName);
            }else {
                Class<?> aClass = Class.forName(beanDefinition.getBeanClassName());
                instance = aClass.newInstance();
    
                //=============AOP开始==============
                // 如果满足条件就返回proxy对象
                // 加载配置文件
                LAdvisedSupport config = instanceAopConfig(beanDefinition);
                config.setTargetClass(aClass);
                config.setTarget(instance);
    
                // 判断规则，要不要生成代理类，如果要就覆盖原来的对象， 如果不要就不做如何处理
                if (config.pointCutMath()) {
                    JdkDynamicAopProxy jdkDynamicAopProxy = new JdkDynamicAopProxy(config);
                    instance =  jdkDynamicAopProxy.getProxy();
                }
                //==============AOP结束===================
                this.beanFactoryObjectCache.put(beanName, instance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return instance;
    }

    /**
     * 读取配置文件
     * @param beanDefinition beanDefinition
     * @return LAdvisedSupport
     */
    private LAdvisedSupport instanceAopConfig(LBeanDefinition beanDefinition) {
        Properties properties = this.definitionReader.getProperties();

        LAopConfig aopConfig = new LAopConfig();
        aopConfig.setPointCut(properties.getProperty("pointCut"));
        aopConfig.setAspectClass(properties.getProperty("aspectClass"));
        aopConfig.setAspectBefore(properties.getProperty("aspectBefore"));
        aopConfig.setAspectAfter(properties.getProperty("aspectAfter"));
        aopConfig.setAspectThrow(properties.getProperty("aspectThrow"));
        aopConfig.setThrowTypeClassName(properties.getProperty("throwTypeClassName"));
        return new LAdvisedSupport(aopConfig);
    }

    /**
     *  依赖注入
     * @param beanName beanName
     * @param beanDefinition beanDefinition
     * @param lBeanWrapper lBeanWrapper
     */
    private void populateBean(String beanName,
                              LBeanDefinition beanDefinition,
                              LBeanWrapper lBeanWrapper) {
        //可能涉及到循环依赖？
        //A{ B b}
        //B{ A a}
        //用两个缓存，循环两次
        //1、把第一次读取结果为空的BeanDefinition存到第一个缓存
        //2、等第一次循环之后，第二次循环再检查第一次的缓存，再进行赋值
        Object instance = lBeanWrapper.getInstance();
        Class<?> clazz = lBeanWrapper.getClazz();
        if (!(clazz.isAnnotationPresent(LController.class) || clazz.isAnnotationPresent(LService.class))) {
            return;
        }
        Field[] declaredFields = clazz.getDeclaredFields();
            // 循环遍历 类的属性
            for (Field field : declaredFields) {
                if (!field.isAnnotationPresent(LAutowired.class)) {
                    // 没有依赖注入的时候不管
                    continue;
                }
                String value = field.getAnnotation(LAutowired.class).value().trim();
                if ("".equals(value)) {
                    value = field.getType().getName();
                }
                // 暴力访问
                field.setAccessible(true);
                try {
                    if (this.beanFactoryInstanceCache.get(value) == null) {
                        continue;
                    }
                    field.set(instance, this.beanFactoryInstanceCache.get(value).getInstance());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
    }


    public Object getBean(Class<?> clazz) {
        return getBean(clazz.getSimpleName());
    }

    public String[] getBeanDefinitionNames() {
        return this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }


    public int getApplicationBeanCount() {
        return beanDefinitionMap.size();
    }


    public Properties getConfig() {
        return definitionReader.getProperties();
    }
}
