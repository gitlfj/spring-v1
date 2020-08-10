package com.lfj.dome.framework;

import com.lfj.dome.framework.annotation.*;

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
 *  300多行代码手写Spring 核心实现原理V1版本， 此类是一个Servlet，完成了初始化操作，
 *  和请求拦截和请求分发，这是基于Servlet实现的，需要对Servlet有一定的了解，servlet需要在
 *  web.xml中配置使用
 *  主要关注两个方法：
 *  一. init() 初始化方法，里面的做的事情有：
 *  1，加载配置文件放入到 properties中
 *  2，通过配置文件配置的包路径扫描项目下面的类放到List中
 *  3，通过反射技术把List的类实例化，放到ioc容器map中
 *  4，完成依赖注入
 *  5，完成MVC的，URL -》 Method 的Map
 *
 *  二. doGet() / doPost() 处理请求方法
 *  获取请求的URL，然后根据URL找到处理的Method，然后通过反射，调用method.invoke()  完成方法的处理
 *
 *
 *  缺点： 所有代码都写在一个类中，没有分层，耦合度高，违背了单一原则
 *
 *
 *
 */
public class LDispatchServletV1 extends HttpServlet {

    /**
     *  存放配置文件
     */
    private Properties properties = new Properties();

    /**
     *  类
     */
    private List<String> classNames = new ArrayList<String>(16);

    /**
     *  ioc容器
     */
    private Map<String, Object> ioc = new HashMap<String, Object>(16);

    /**
     *  <k, v>  url - Method
     */
    private Map<String, Method> handlerMapping = new HashMap<String, Method>(16);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       doPost(req, resp);
    }

    /**
     *  处理每一个请求的方法
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // 处理逻辑

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
            method.invoke(ioc.get(beanName),paramValues);
        } catch (Exception e) {
            e.printStackTrace();
            String s = "500, detail :" + e.getStackTrace().toString();
            resp.getWriter().write(s);
            return;
        }
    }

    /**
     *  启动Servlet容器初始化调用的方法， 这是Servlet的内容
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        // 加载配置文件
        loadConfig(config.getInitParameter("contextConfigLocation"));

        // 扫描相关类
        scanPackage(properties.getProperty("scanPackage"));

        // 初始化扫描到的类，通过反射将类放入到IOC容器中
        initIOC();

        // 完成DI依赖注入
        doAutowired();

        // 初始化handlerMapping URL和method对应
        doHandlerMapping();

        System.out.println("Spring framework 初始化完成... ");

    }

    private void doHandlerMapping() {

        if (ioc.isEmpty()) {return;}

        Set<Map.Entry<String, Object>> entries = ioc.entrySet();

        for (Map.Entry<String, Object> entry : entries) {
            Class<?> aClass = entry.getValue().getClass();
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
                handlerMapping.put(uri, method);
                System.out.println("url = " + uri + ", method= " + method.getName());
            }
        }


    }

    private void doAutowired() {
        if (ioc.isEmpty()) {
            return;
        }

        Set<Map.Entry<String, Object>> entries = ioc.entrySet();
        // 循环遍历ioc容器的类，并且完成依赖注入
        for (Map.Entry<String, Object> entry : entries) {

            Field[] declaredFields = entry.getValue().getClass().getDeclaredFields();

            // 循环遍历 类的属性
            for (Field field : declaredFields) {

                if (!field.isAnnotationPresent(LAutowired.class)) {
                    // 不需要依赖注入的时候不管
                    continue;
                }

                String value = field.getAnnotation(LAutowired.class).value().trim();

                if ("".equals(value)) {
                    value = field.getType().getName();
                }
                // 暴力访问
                field.setAccessible(true);
                try {
                    field.set(entry.getValue(), ioc.get(value));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private void initIOC() {
        if (classNames.isEmpty()) {return;}

        for (String className : classNames) {
            try {
                Class<?> aClass = Class.forName(className);
                boolean anInterface = aClass.isInterface();
                if (anInterface) {continue;}
                // 判断有没有注解
                if (aClass.isAnnotationPresent(LController.class)) {
                    // 将首类名首字母转小写
                    String beanName = toLowerCase(aClass.getSimpleName());
                    Object object = aClass.newInstance();
                    ioc.put(beanName, object);
                }else if (aClass.isAnnotationPresent(LService.class)) {
                    // 1 自定义beanName
                    String beanName = aClass.getAnnotation(LService.class).value();
                    if ("".equals(beanName)) {
                       beanName = toLowerCase(aClass.getSimpleName());
                    }

                    // 2 用默认的
                    Object object = aClass.newInstance();
                    ioc.put(beanName, object);

                    // 3 如果是接口
                    Class<?>[] interfaces = aClass.getInterfaces();
                    for (Class<?> i : interfaces) {
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The " + i.getName() + " is exists!!");
                        }
                        ioc.put(i.getName(), object);
                    }
                }else {
                    continue;
                }

            } catch (Exception e) {
                e.printStackTrace();
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

    /**
     *  扫描指定包下面类放到List集合里面
     * @param scanPackage
     */
    private void scanPackage(String scanPackage) {
        URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
        assert url != null;
        File classPath = new File(url.getFile());
        File[] files = classPath.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                scanPackage(scanPackage + "." + file.getName());
            }else {
                if (!file.getName().endsWith(".class")) { continue; }
                String replace = file.getName().replace(".class", "");
                classNames.add(scanPackage + "." + replace);
            }
        }
    }

    /**
     *  加载配置文件到Properties中
     */
    private void loadConfig(String contextConfigLocation) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
        try {
            if (inputStream == null) {
                throw new RuntimeException("读取配置文件为空");
            }
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
