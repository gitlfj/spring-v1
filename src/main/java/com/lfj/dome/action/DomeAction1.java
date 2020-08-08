package com.lfj.dome.action;

import com.lfj.dome.annotation.LAutowired;
import com.lfj.dome.annotation.LController;
import com.lfj.dome.annotation.LRequestMapping;
import com.lfj.dome.annotation.LRequestParam;
import com.lfj.dome.service.IDomeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@LController
@LRequestMapping("/action")
public class DomeAction1 {

    @LAutowired
    private IDomeService domeService;

    @LRequestMapping("/query")
    public void get(HttpServletRequest request, HttpServletResponse response,
                      @LRequestParam("name") String name) throws IOException {

        String result = domeService.get(name);
        response.getWriter().write(result);
    }


}
