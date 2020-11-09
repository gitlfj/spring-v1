package com.lfj.dome.framework.beans.support;

import com.lfj.dome.framework.annotation.LController;
import com.lfj.dome.framework.annotation.LService;
import com.lfj.dome.framework.beans.config.LBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 *  自定义BeanDefinitionReader, 负责配置文件的加载
 *  并且将扫描到的class 定义成BeanDefinition
 * @author lifangjin
 */
public class LBeanDefinitionReader {

    /**
     *  jdk提供的配置文件类 properties 继承 HashTable
     */
    private final Properties properties = new Properties();

    /**
     *  存放扫描指定包路径下面的所以class文件的全路径
     *  存放的值为：registerBeanClasses = [com.lfj.dome.framework.context.LApplicationContext, com.lfj.dome.framework.webmvc.LViewResolver, com.lfj.dome.framework.webmvc.LHandlerMapping, com.lfj.dome.framework.webmvc.LModelAndView, com.lfj.dome.framework.webmvc.LHandlerAdapter, com.lfj.dome.framework.webmvc.LView, com.lfj.dome.framework.annotation.LAutowired, com.lfj.dome.framework.annotation.LRequestParam, com.lfj.dome.framework.annotation.LController, com.lfj.dome.framework.annotation.LService, com.lfj.dome.framework.annotation.LRequestMapping, com.lfj.dome.framework.aop.config.LAopConfig, com.lfj.dome.framework.aop.JdkDynamicAopProxy, com.lfj.dome.framework.aop.support.LAdvisedSupport, com.lfj.dome.framework.aop.aspect.LogAspect, com.lfj.dome.framework.aop.aspect.LAdvice, com.lfj.dome.framework.LDispatchServletV1, com.lfj.dome.framework.beans.config.LBeanDefinition, com.lfj.dome.framework.beans.support.LBeanDefinitionReader, com.lfj.dome.framework.beans.LBeanWrapper, com.lfj.dome.framework.LDispatchServletV2, com.lfj.dome.action.PageAction, com.lfj.dome.action.DomeAction, com.lfj.dome.action.DomeAction1, com.lfj.dome.action.LfjController, com.lfj.dome.action.MyAction, com.lfj.dome.service.impl.QueryService, com.lfj.dome.service.impl.ModifyService, com.lfj.dome.service.IDomeService, com.lfj.dome.service.DomeServiceImpl, com.lfj.dome.service.LfjServiceImpl, com.lfj.dome.service.IQueryService, com.lfj.dome.service.IModifyService, com.lfj.dome.service.ILfjService]
     */
    List<String> registerBeanClasses = new ArrayList<String>();

    /**
     *  配置文件名称 application.properties
     */
    private final String contextConfigLocation;

    /**
     *  构造函数初始化配置
     * @param contextConfigLocation 配置文件
     */
    public LBeanDefinitionReader(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
        // 加载配置文件
        loadProperties();
        // 扫描指定包下面的class文件，放入到List<String> 中
        scanPackage(properties.getProperty("scanPackage"));
        System.out.println("registerBeanClasses = " + registerBeanClasses);
    }



    /**
     *  加载配置文件到 properties中
     **/
    private void loadProperties() {
        // 传入配置文件名，读取配置文件到properties中
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            if (inputStream == null) {
                throw new RuntimeException("读取配置文件为空");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            throw new RuntimeException("加载配置文件异常");
        }finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {

                }
            }
        }
    }

    /**
     * 将Spring 容器管理的bean封装成BeanDefinition
     * @return List<LBeanDefinition>
     */
    public List<LBeanDefinition> loadBeanDefinition() {
        List<LBeanDefinition> lBeanDefinitionList = new ArrayList<LBeanDefinition>();
        // 循环遍历扫描到的class
        for (String className : registerBeanClasses) {
            try {
                // 反射
                Class<?> clazz = Class.forName(className);
                // 接口不管
                if (clazz.isInterface()) {
                    continue;
                }
                // 如果不是要Spring 容器管理的类就不管了
                if (!(clazz.isAnnotationPresent(LController.class) ||
                        clazz.isAnnotationPresent(LService.class))) {
                    continue;
                }
                // beanName默认等于类名小写
                String beanName = toLowerCase(clazz.getSimpleName());
                if (clazz.isAnnotationPresent(LService.class)) {
                    String value = clazz.getAnnotation(LService.class).value();
                    if (!"".equals(value)) {
                        // 自定义了beanName
                        beanName = value;
                    }
                }
                // 创建BeanDefinition
                lBeanDefinitionList.add(doCreateBeanDefinition(beanName, clazz.getName()));
                // 类实现了接口的情况
                for (Class<?> i : clazz.getInterfaces()) {
                    for (LBeanDefinition beanDefinition : lBeanDefinitionList) {
                        if (beanDefinition.getFactoryBeanName().equals(i.getName())) {
                            throw new RuntimeException("一个接口只能有一个实现类！");
                        }
                    }
                    lBeanDefinitionList.add(doCreateBeanDefinition(i.getName(), clazz.getName()));
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return lBeanDefinitionList;
    }

    /**
     *  创建beanDefinition
     * @param beanName beanName
     * @param beanClassName beanClassName
     * @return LBeanDefinition
     */
    private LBeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        LBeanDefinition beanDefinition = new LBeanDefinition();
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    /**
     *  将字符串首字母转大写
     * @param toLowerCase toLowerCase
     * @return java.lang.String
     */
    private String toLowerCase(String toLowerCase) {
        char[] chars = toLowerCase.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }



    /**
     *  递归扫描指定包下面类放到List集合里面
     * @param scanPackage 扫描的包路径
     */
    private void scanPackage(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        // ioc容器管理的bean
        assert url != null;
        File classPath = new File(url.getFile());
        File[] files = classPath.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                scanPackage(scanPackage + "." + file.getName());
            }else {
                if (!file.getName().endsWith(".class")) { continue; }
                String replace = file.getName().replace(".class", "");
                registerBeanClasses.add(scanPackage + "." + replace);
            }
        }
    }

    /**
     *  获取配置Properties对象
     * @return Properties
     */
    public Properties getProperties() {
        return properties;
    }

}
