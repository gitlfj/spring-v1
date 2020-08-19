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

@LController
@LRequestMapping("/lfj")
public class DomeAction {

    @LAutowired
    private IDomeService domeService;

    @LAutowired
    private ILfjService lfjService;

    @LAutowired
    private LfjController controller;


    @LRequestMapping("/query")
    public void get(HttpServletRequest request, HttpServletResponse response,
                      @LRequestParam("name") String name) throws IOException {

        String result = domeService.get(name);
        String select = lfjService.select(name);
        response.getWriter().write(result + "_" + select + "_" + controller.get());
    }

    @LRequestMapping("/add")
    public void add(HttpServletRequest request, HttpServletResponse response,
                    @LRequestParam("name") String name) throws IOException {

        String result = domeService.get(name);
        response.getWriter().write(result);
    }


    @LRequestMapping("/add*.json")
    public void json(HttpServletResponse response) throws IOException {
        response.getWriter().write("I am is json");
    }


    public String get() {
        return "I am is DomeAction";
    }


}
