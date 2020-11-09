package com.lfj.dome.framework.webmvc;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 *  自定义HandlerMapping, 请求URL和方法的对应关系
 * @author lifangjin
 */
public class LHandlerMapping {

    /**
     *  请求URL
     */
    private Pattern pattern;

    /**
     *  请求对应的方法
     */
    private Method method;

    /**
     *  对应的实例
     */
    private Object controller;


    public LHandlerMapping(Pattern pattern, Method method, Object controller) {
        this.pattern = pattern;
        this.method = method;
        this.controller = controller;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

}
