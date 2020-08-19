package com.lfj.dome.action;

import com.lfj.dome.framework.annotation.LAutowired;
import com.lfj.dome.framework.annotation.LController;
import com.lfj.dome.framework.annotation.LRequestMapping;
import com.lfj.dome.framework.annotation.LRequestParam;
import com.lfj.dome.framework.webmvc.LModelAndView;
import com.lfj.dome.service.IQueryService;

import java.util.HashMap;
import java.util.Map;

/**
 * 公布接口url
 * @author Tom
 *
 */
@LController
@LRequestMapping("/")
public class PageAction {

    @LAutowired
    IQueryService queryService;

    @LRequestMapping("/first.html")
    public LModelAndView query(@LRequestParam("teacher") String teacher){
        String result = queryService.query(teacher);
        Map<String,Object> model = new HashMap<String,Object>();
        model.put("teacher", teacher);
        model.put("data", result);
        model.put("token", "123456");
        return new LModelAndView("first.html",model);
    }

}
