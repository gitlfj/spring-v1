package com.lfj.dome.action;

import com.lfj.dome.framework.annotation.LAutowired;
import com.lfj.dome.framework.annotation.LController;
import com.lfj.dome.framework.annotation.LRequestMapping;
import com.lfj.dome.framework.annotation.LRequestParam;
import com.lfj.dome.service.IDomeService;
import com.lfj.dome.service.ILfjService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 *
 */
@LController
public class LfjController {


    @LAutowired
    private ILfjService lfjService;

    @LAutowired
    private IDomeService domeService;

    /**
     *  循环依赖
     */
    @LAutowired
    private DomeAction domeAction;

    @LRequestMapping("text/add")
    public void add(HttpServletRequest request, HttpServletResponse response,
                    @LRequestParam("name") String name) throws IOException {
        String select = lfjService.select(name);

        String s = domeService.get(name);

        response.getWriter().write(select + "_" + s + "_" + domeAction.get());
    }

    public String get() {
        return "I am is LfjController";
    }

}
