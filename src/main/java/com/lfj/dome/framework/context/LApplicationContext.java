package com.lfj.dome.framework.context;

import com.lfj.dome.framework.annotation.LAutowired;
import com.lfj.dome.framework.annotation.LController;
import com.lfj.dome.framework.annotation.LService;
import com.lfj.dome.framework.beans.LBeanWrapper;
import com.lfj.dome.framework.beans.config.LBeanDefinition;
import com.lfj.dome.framework.beans.support.LBeanDefinitionReader;

import java.lang.reflect.Field;
import java.util.*;

/**
 *  自定义 ApplicationContext类
 */
public class LApplicationContext {

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
     *  bean实例缓存
     */
    private Map<String, Object> beanFactoryObjectCache = new HashMap<String, Object>(16);


    public LApplicationContext(String ... contextConfigLocations) throws Exception {
        // 1. 加载配置文件
        definitionReader = new LBeanDefinitionReader(contextConfigLocations[0]);

        // 2. 解析配置文件，封装成BeanDefinition：factoryBeanName， beanClassName
        List<LBeanDefinition> lBeanDefinitionList = definitionReader.loadBeanDefinition();

        // 3. 把BeanDefinition缓存起来
        for (LBeanDefinition beanDefinition : lBeanDefinitionList) {
            if(this.beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())){
                continue;
            }
            // 根据全类名缓存
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
            // 根据类名首字母小写缓存
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
        }

        // 4. 依赖注入
        doAutowired();

        System.out.println("IOC 容器初始化完成。。。");

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
     * @param beanName
     * @return
     */
    public Object getBean(String beanName) {
        //1 获取beanDefinition配置信息
        LBeanDefinition beanDefinition = this.beanDefinitionMap.get(beanName);
        LBeanWrapper lBeanWrapper = new LBeanWrapper();
        try {
            // 2 反射创建对象
            Object instance = createInstance(beanDefinition, beanName);

            // 3 封装成 BeanWrapper
            lBeanWrapper = new LBeanWrapper(instance);

            // 4 ioc 注入
            beanFactoryInstanceCache.put(beanName, lBeanWrapper);

            // 5 依赖注入
            populateBean(beanName, beanDefinition, lBeanWrapper);
        }catch (Exception e) {

        }
        return lBeanWrapper.getInstance();
    }

    /**
     *  创建实例
     * @param beanDefinition
     * @param beanName
     * @return
     * @throws Exception
     */
    private Object createInstance(LBeanDefinition beanDefinition, String beanName) throws Exception{
        Object instance;
        if (this.beanFactoryObjectCache.containsKey(beanName)) {
            instance =  this.beanFactoryObjectCache.get(beanName);
        }else {
            Class<?> aClass = Class.forName(beanDefinition.getBeanClassName());
            instance = aClass.newInstance();
            this.beanFactoryObjectCache.put(beanName, instance);
        }
        return instance;
    }

    /**
     *  依赖注入
     * @param beanName
     * @param beanDefinition
     * @param lBeanWrapper
     */
    private void populateBean(String beanName,
                              LBeanDefinition beanDefinition,
                              LBeanWrapper lBeanWrapper) {
        //可能涉及到循环依赖？
        //A{ B b}
        //B{ A b}
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
