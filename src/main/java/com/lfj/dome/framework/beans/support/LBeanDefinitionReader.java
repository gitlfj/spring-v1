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
 */
public class LBeanDefinitionReader {

    private Properties properties = new Properties();

    /**
     *  需要注册的bean
     */
    List<String> regitryBeanClasses = new ArrayList<String>();

    private String contextConfigLocation;


    public LBeanDefinitionReader(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
        // 加载配置文件
        loadProperties();
        // 解析配置文件
        scanPackage(properties.getProperty("scanPackage"));
    }



    /**
     *  加载配置文件
     */
    private void loadProperties() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            if (inputStream == null) {
                throw new RuntimeException("读取配置文件为空");
            }
            properties.load(inputStream);
        } catch (IOException e) {

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
     *  将Spring 容器管理的bean封装成BeanDefinition
     * @return
     */
    public List<LBeanDefinition> loadBeanDefinition() {

        List<LBeanDefinition> lBeanDefinitionList = new ArrayList<LBeanDefinition>();
        for (String className : regitryBeanClasses) {
            try {
                Class<?> clazz = Class.forName(className);
                // 接口不管
                if (clazz.isInterface()) { continue; }

                // 如果不是要Spring 容器管理的类就不管了
                if (!(clazz.isAnnotationPresent(LController.class) || clazz.isAnnotationPresent(LService.class))) {
                    continue;
                }
                // beanName默认等于类名小写
                String factoryBeanName = toLowerCase(clazz.getSimpleName());
                if (clazz.isAnnotationPresent(LService.class)) {
                    String value = clazz.getAnnotation(LService.class).value();
                    if (!"".equals(value)) {
                        // 自定义了beanName
                        factoryBeanName = value;
                    }
                }
                lBeanDefinitionList.add(doCreateBeanDefinition(factoryBeanName, clazz.getName()));

                // 3 类实现了接口的情况
                for (Class<?> i : clazz.getInterfaces()) {
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
     * @param beanName
     * @param beanClassName
     * @return
     */
    private LBeanDefinition doCreateBeanDefinition(String beanName, String beanClassName) {
        LBeanDefinition beanDefinition = new LBeanDefinition();
        beanDefinition.setFactoryBeanName(beanName);
        beanDefinition.setBeanClassName(beanClassName);
        return beanDefinition;
    }

    /**
     *  将字符串首字母转大写
     * @param toLowerCase
     * @return
     */
    private String toLowerCase(String toLowerCase) {
        char[] chars = toLowerCase.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }



    /**
     *  扫描指定包下面类放到List集合里面
     * @param scanPackage
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
                regitryBeanClasses.add(scanPackage + "." + replace);
            }
        }
    }

}
