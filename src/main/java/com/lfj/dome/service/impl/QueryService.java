package com.lfj.dome.service.impl;

import com.lfj.dome.framework.annotation.LService;
import com.lfj.dome.service.IQueryService;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 查询业务
 * @author Tom
 *
 */
@LService
public class QueryService implements IQueryService {

	/**
	 * 查询
	 */
	@Override
	public String query(String name) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String time = sdf.format(new Date());
		return "{name:\"" + name + "\",time:\"" + time + "\"}";
	}

	/**
	 * AOP异常测试
	 *
	 * @return String
	 */
	@Override
	public String aopExceptionTest() {
		throw new NullPointerException("我是AOP故意抛出的异常");
	}

}
