package com.lfj.dome.framework;

import com.lfj.dome.framework.annotation.*;
import com.lfj.dome.framework.context.LApplicationContext;
import com.lfj.dome.framework.webmvc.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 手写Spring核心实现原理V2版本，高仿版，非常接近Spring实现的核心原理
 * 核心流程：
 * 1. 调用 Servlet.init() 方法的时候 初始化Spring容器 LApplicationContext
 * 2. BeanDefinitionReader 负责读取配置，扫描bean，封装成一个BeanDefinition
 * 3. 调用getBean() 方法  负责bean初始化，和依赖注入
 * @author lfj
 *
 */
public class LDispatchServletV2 extends HttpServlet {

    private LApplicationContext applicationContext;

    /**
     *  handlerMapping
     */
    private List<LHandlerMapping> handlerMappingList = new ArrayList<LHandlerMapping>(16);

    /**
     *
     */
    private Map<LHandlerMapping, LHandlerAdapter> handlerAdapterMap = new HashMap<LHandlerMapping, LHandlerAdapter>(16);

    /**
     *
     */
    private List<LViewResolver> viewResolvers = new ArrayList<LViewResolver>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
       doPost(req, resp);
    }

    /**
     *  处理每一个请求的方法，并且找到对应处理的方法处理
     * @param req req
     * @param resp resp
     * @throws IOException IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            // 委派，根据URL找到一个Method
            doDispatch(req, resp);
        } catch (Exception e) {
            try {
                processDispatchResult(req,resp,new LModelAndView("500"));
            } catch (Exception e1) {
                e1.printStackTrace();
                resp.getWriter().write("500 Exception,Detail : " + Arrays.toString(e.getStackTrace()));
            }
        }

    }


    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        // 1. 通过URL获取一个HandlerMapping
        LHandlerMapping handlerMapping = getHandler(req);

        if (handlerMapping == null) {
            processDispatchResult(req, resp, new LModelAndView("404"));
            return;
        }
        // 2. 通过handlerMapping获取handlerAdapter
        LHandlerAdapter handlerAdapter = getHandlerAdapter(handlerMapping);

        // 3. 解析某个方法的形参和返回值后，封装成一个modelAndView
        assert handlerAdapter != null;
        LModelAndView modelAndView = handlerAdapter.handler(req, resp, handlerMapping);

        // 4. 把modelAndView 解析成VViewResolver
        processDispatchResult(req, resp, modelAndView);

    }


    /**
     *  获取HandlerAdapter
     * @param handlerMapping
     * @return
     */
    private LHandlerAdapter getHandlerAdapter(LHandlerMapping handlerMapping) {
        if (handlerAdapterMap.isEmpty()) {return null;}
        return handlerAdapterMap.get(handlerMapping);
    }
    private void processDispatchResult(HttpServletRequest req,
                                       HttpServletResponse resp,
                                       LModelAndView mv) throws Exception {
        if(null == mv){return;}
        if(this.viewResolvers.isEmpty()){return;}

        for (LViewResolver viewResolver : this.viewResolvers) {
            LView view = viewResolver.resolveViewName(mv.getViewName());
            //直接往浏览器输出
            view.render(mv.getModel(),req,resp);
            return;
        }
    }

    /**
     *  获取handler
     * @param req
     * @return
     */
    private LHandlerMapping getHandler(HttpServletRequest req) {
        if (handlerMappingList.isEmpty()) {return null;}

        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        String url = requestURI + contextPath;
        for (LHandlerMapping handlerMapping : handlerMappingList) {
            Matcher matcher = handlerMapping.getPattern().matcher(url);
            if (!matcher.matches()) { continue; }
            return handlerMapping;
        }
        return null;
    }

    /**
     *  启动Servlet容器初始化调用的方法, spring 容器的人口
     * @param config  config
     */
    @Override
    public void init(ServletConfig config) {
        try {
            // 会去读取Servlet容器 web.xml配置文件配置的配置文件名
            String contextConfigLocation = config.getInitParameter("contextConfigLocation");
            // 初始化ApplicationContext
            if (contextConfigLocation == null || "".equals(contextConfigLocation)) {
                throw new RuntimeException("请在web.xml中配置：contextConfigLocation");
            }
            applicationContext = new LApplicationContext(contextConfigLocation);
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 初始化mvc组件
        initStrategies(applicationContext);
        System.out.println("Spring framework 初始化完成... ");
    }

    /**
     *  初始化mvc组件
     * @param context
     */
    private void initStrategies(LApplicationContext context) {
//        //多文件上传的组件
//        initMultipartResolver(context);
//        //初始化本地语言环境
//        initLocaleResolver(context);
//        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
//        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
//        //初始化视图预处理器
//        initRequestToViewNameTranslator(context);
        //初始化视图转换器
        initViewResolvers(context);
//        //FlashMap管理器
//        initFlashMapManager(context);
    }

    /**
     *  初始化 ViewResolvers 主要是解析模版
     * @param context
     */
    private void initViewResolvers(LApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("templateRoot");

        URL resource = this.getClass().getClassLoader().getResource(templateRoot);
        String templateRootPath = resource.getFile();

        File templateRootDir = new File(templateRootPath);
        for (File file : templateRootDir.listFiles()) {
            this.viewResolvers.add(new LViewResolver(templateRoot));
        }

    }

    /**
     *  初始化handlerAdapter
     * @param context
     */
    private void initHandlerAdapters(LApplicationContext context) {
        for (LHandlerMapping handlerMapping : handlerMappingList) {
            handlerAdapterMap.put(handlerMapping, new LHandlerAdapter());
        }
    }

    /**
     *  初始化handlerMapping
     * @param context
     */
    private void initHandlerMappings(LApplicationContext context) {
        if (context.getApplicationBeanCount() == 0) {
            return;
        }

        for (String beanName : context.getBeanDefinitionNames()) {
            Object instance = context.getBean(beanName);
            Class<?> aClass = instance.getClass();

            if (!aClass.isAnnotationPresent(LController.class)) {
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

                String uri = method.getAnnotation(LRequestMapping.class).value();
                String regex = ("/" + baseUrl + "/" + uri).replaceAll("\\*", ".*").replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(regex);
                handlerMappingList.add(new LHandlerMapping(pattern, method, instance));
                System.out.println("url = " + regex + ", method= " + method.getName());
            }
        }

    }

    /**
     *  将字符串首字母转大写
     * @param toLowerCase 字符串
     * @return String
     */
    private String toLowerCase(String toLowerCase) {
        char[] chars = toLowerCase.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
