package com.lfj.dome.framework.webmvc;

import java.util.Map;

/**
 *  自定义ModelAndView, Spring将返回结果都封装成了ModelAndView
 */
public class LModelAndView {

    /**
     *  返回页面对应的名字
     */
    private String viewName;

    /**
     *  返回的值
     */
    private Map<String, ?> map;


    public LModelAndView(String viewName, Map<String, ?> map) {
        this.viewName = viewName;
        this.map = map;
    }

    public LModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public Map<String, ?> getMap() {
        return map;
    }

    public void setMap(Map<String, ?> map) {
        this.map = map;
    }

    public Map<String, ?> getModel() {
        return map;
    }
}
