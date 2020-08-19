package com.lfj.dome.action;

import com.lfj.dome.framework.annotation.LAutowired;
import com.lfj.dome.framework.annotation.LController;
import com.lfj.dome.framework.annotation.LRequestMapping;
import com.lfj.dome.framework.annotation.LRequestParam;
import com.lfj.dome.framework.webmvc.LModelAndView;
import com.lfj.dome.service.IModifyService;
import com.lfj.dome.service.IQueryService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 公布接口url
 * @author Tom
 *
 */
@LController
@LRequestMapping("/web")
public class MyAction {

	@LAutowired
	private IQueryService queryService;

	@LAutowired
	private IModifyService modifyService;

	@LRequestMapping("/query.json")
	public LModelAndView query(HttpServletRequest request, HttpServletResponse response,
							   @LRequestParam("name") String name){
		String result = queryService.query(name);
		return out(response,result);
	}
	
	@LRequestMapping("/add*.json")
	public LModelAndView add(HttpServletRequest request, HttpServletResponse response,
                              @LRequestParam("name") String name, @LRequestParam("addr") String addr){
		String result = modifyService.add(name,addr);
		return out(response,result);
	}
	
	@LRequestMapping("/remove.json")
	public LModelAndView remove(HttpServletRequest request, HttpServletResponse response,
                                 @LRequestParam("id") Integer id){
		String result = modifyService.remove(id);
		return out(response,result);
	}
	
	@LRequestMapping("/edit.json")
	public LModelAndView edit(HttpServletRequest request, HttpServletResponse response,
                               @LRequestParam("id") Integer id,
                               @LRequestParam("name") String name){
		String result = modifyService.edit(id,name);
		return out(response,result);
	}
	
	
	
	private LModelAndView out(HttpServletResponse resp, String str){
		try {
			resp.getWriter().write(str);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
