package com.sys.common;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.sys.tools.Str;

public class RequestInfo {
	
	private HttpServletRequest request = null;
	
	/** 请求体中通过 POST 方式传递的参数 */
	private Map<String, String> post = new HashMap<String, String>();
	
	/** 请求体中通过 GET 方式传递的参数 */
	private Map<String, String> get = new HashMap<String, String>();
	
	/** 请求体中通过 GET 方式传递的路由参数 */
	private Map<String, String> router = new HashMap<String, String>();
	
	@SuppressWarnings("unchecked")
	public RequestInfo(HttpServletRequest request) {
		
		this.request = request;

		this.analysisPostParam(this.analysisGetParam());
    	
		this.analysisRequestHead();
	}
	
	/**
	 * 解析get传递的参数
	 * @return
	 */
	private Map<String, String> analysisGetParam() {

		String queryStr = request.getQueryString();
		
		if ( !Str.is_invalid(queryStr) ) {

			String[] temp = queryStr.split("&");
			
			for ( int i = 0; i < temp.length; i++ ) {
				
				String key = temp[i].split("=")[0];
				String value = temp[i].split("=")[1];
				
				if ( key.equals("a") || key.equals("c") || key.equals("p") ) {
					this.router.put(key, value);
				} else {
					this.get.put(key, value);
				}
			}
		}
		
		return this.get;
	}
	
	/**
	 * 解析post传递参数
	 * @param getParam get传递的参数
	 */
	private void analysisPostParam(Map<String, String> getParam) {
		
		Enumeration<?> pName = request.getParameterNames();
		
		while(pName.hasMoreElements()){
    		
			String param = (String) pName.nextElement();
    		
			if ( !getParam.containsKey(param) ) {
    			
				String key = param;
				String value = this.request.getParameterValues(param)[0];
				this.post.put(key, value);
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void analysisRequestHead() {

	}

	public Map<String, String> getPost() {
		
		return this.post;
	}

	public Map<String, String> getGet() {
		
		return this.get;
	}
	
	public Map<String, String> getRouter() {
		
		return this.router;
	}

	public String _POST(String key) {
		
		if ( this.post.containsKey(key) ) {
			
			return this.post.get(key);
		} else {
			
			return null;
		}
	}

	public String _GET(String key) {
		
		if ( this.get.containsKey(key) ) {
			
			return this.get.get(key);
		} else {
			
			return null;
		}
	}

	public String _ROUTER(String key) {
		
		if ( this.router.containsKey(key) ) {
			
			return this.router.get(key);
		} else {
			
			return null;
		}
	}

	/**
	 * 可以选择性的添加c、a、v, 为了防止 router 属性中, 缺少c、a、v, 或实现更复杂的控制器映射
	 * @param String key c|a|v
	 * @param String value 
	 * @return
	 */
	public String _setCoreQueryString(String key, String value) {
		
		if ( key.equals("c") || key.equals("a") || key.equals("p") ) {
			
			this.router.put(key, value);
			return value;
		} else {
			
			return null;
		}
	}

	/**
	 * 获取当前访问用户的 request 对象
	 * @return HttpServletRequest
	 */
	public HttpServletRequest getRequest() {
		return request;
	}
}