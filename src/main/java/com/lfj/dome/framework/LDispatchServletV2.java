package com.lfj.dome.framework;

import com.lfj.dome.framework.annotation.*;
import com.lfj.dome.framework.context.LApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * 手写Spring核心实现原理V2版本，高仿版，非常接近Spring实现的核心原理
 * 核心流程：
 * 1. 调用 Servlet.init() 方法的时候 初始化Spring容器 LApplicationContext
 * 2. BeanDefinitionReader 负责读取配置，扫描bean，封装成一个BeanDefinition
 * 3. 调用getBean() 方法  负责bean初始化，和依赖注入
 *
 *
 */
public class LDispatchServletV2 extends HttpServlet {

    private LApplicationContext applicationContext;

    /**
     *  <k, v>  url - Method
     */
    private Map<String, Method> handlerMapping = new HashMap<String, Method>(16);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       doPost(req, resp);
    }

    /**
     *  处理每一个请求的方法，并且找到对应处理的方法处理
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 获取URL
        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String url = requestURI + contextPath;
        if (!handlerMapping.containsKey(url)) {
            resp.getWriter().write("404 Page");
            return;
        }

        Method method = handlerMapping.get(url);

        Map<String,String[]> params = req.getParameterMap();

        //获取形参列表
        Class<?> [] parameterTypes = method.getParameterTypes();

        //参数
        Object [] paramValues = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {

            Class paramterType = parameterTypes[i];
            if(paramterType == HttpServletRequest.class){
                paramValues[i] = req;
            }else if(paramterType == HttpServletResponse.class){
                paramValues[i] = resp;
            }else if(paramterType == String.class){
                //通过运行时的状态去拿到你
                Annotation[] [] pa = method.getParameterAnnotations();

                for (int j = 0; j < pa.length ; j ++) {
                    for(Annotation a : pa[i]){
                        if(a instanceof LRequestParam){
                            String paramName = ((LRequestParam) a).value();
                            if(!"".equals(paramName.trim())){
                                String value = Arrays.toString(params.get(paramName))
                                        .replaceAll("\\[|\\]","")
                                        .replaceAll("\\s+",",");
                                paramValues[i] = value;
                            }
                        }
                    }
                }
            }
        }
        String beanName = toLowerCase(method.getDeclaringClass().getSimpleName());
        try {
            method.invoke(this.applicationContext.getBean(beanName), paramValues);
        } catch (Exception e) {
            e.printStackTrace();
            String s = "500, detail :" + e.getStackTrace().toString();
            resp.getWriter().write(s);
        }
    }

    /**
     *  启动Servlet容器初始化调用的方法
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {

        //初始化ApplicationContext
        try {
            applicationContext = new LApplicationContext(config.getInitParameter("contextConfigLocation"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // mvc
        doHandlerMapping();

        System.out.println("Spring framework 初始化完成... ");

    }

    /**
     *  MVC
     */
    private void doHandlerMapping() {

        if (this.applicationContext.getApplicationBeanCount() == 0) {
            return;
        }

        for (String beanName : this.applicationContext.getBeanDefinitionNames()) {
            Class<?> aClass = this.applicationContext.getBean(beanName).getClass();

            boolean annotationPresent = aClass.isAnnotationPresent(LController.class);
            if (!annotationPresent) {
                // 不是Controller就进行下一次循环
                continue;
            }
            // 获取base URL
            String baseUrl = "";
            if (aClass.isAnnotationPresent(LRequestMapping.class)) {
                baseUrl = aClass.getAnnotation(LRequestMapping.class).value();
            }

            Method[] methods = aClass.getMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(LRequestMapping.class)) {continue;}

                String url = method.getAnnotation(LRequestMapping.class).value();
                String uri = ("/" + baseUrl + "/" + url).replaceAll("/+", "/");
                if (handlerMapping.containsKey(uri)) {
                    continue;
                }
                handlerMapping.put(uri, method);
                System.out.println("url = " + uri + ", method= " + method.getName());
            }
        }

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

}
