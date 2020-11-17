package com.lfj.dome.service;

/**
 * 查询业务
 * @author Tom
 *
 */
public interface IQueryService {
	
	/**
	 * 查询
	 * @param name name
	 * @return String
	 */
	String query(String name);

	/**
	 * AOP异常测试
	 * @return String
	 */
	String aopExceptionTest();

}
