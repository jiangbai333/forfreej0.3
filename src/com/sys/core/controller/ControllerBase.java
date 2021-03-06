package com.sys.core.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;

import com.App;
import com.sys.common.RequestInfo;
import com.sys.common.ResponseInfo;
import com.sys.common.StaticMassage;
import com.sys.core.model.ModelBase;
import com.sys.libs.Template;
import com.sys.tools.Reflection;
import com.sys.tools.Str;

@SuppressWarnings("serial")
public class ControllerBase {
	
	@SuppressWarnings("unchecked")
	public Map info = new HashMap();
	
	private PrintWriter out = null;
	
	private HttpServletRequest request = null;
	
	private HttpServletResponse response = null;
	
	@SuppressWarnings("unused")
	private RequestInfo res = null;
	
	@SuppressWarnings("unused")
	private ResponseInfo resq = null;
	
	protected Map<String, String> _GET = new HashMap<String, String>();
	
	protected Map<String, String> _POST = new HashMap<String, String>();
	
	protected Map<String, String> _ROUTER = new HashMap<String, String>();
	
	protected ModelBase model = null;
	
	public ControllerBase() {}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public ControllerBase init(RequestInfo res, ResponseInfo resq, String todo){

		this.out = resq.getWriter();
		this.response = resq.getResponse();
		this.request = res.getRequest();
		this.res = res;
		this.resq = resq;
		
		this.info.put("url", request.getScheme() + 
				"://" + request.getServerName() + 
				":" + request.getServerPort() + 
				request.getRequestURI() + 
				"?" + request.getQueryString());
		this.info.put("path", request.getRealPath(""));
		this.info.put("contextPath", request.getContextPath());
		this.info.put("localAddr", request.getLocalAddr());
		this.info.put("getServerName", request.getServerName());
				
		this._GET = res.getGet();
		this._POST = res.getPost();
		this._ROUTER = res.getRouter();
		
		if ( !Str.is_invalid(todo) ) {
			try {
				this.getClass().getMethod(todo).invoke(this);
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				this.error("MissingActoin", new String[] {
					"请在 app.controller." + this._ROUTER.get("p") + "." + this._ROUTER.get("c") + "Controller 中查看 " + this._ROUTER.get("a") + "()" + " 方法名是否书写正确",
					"请查看 app.controller." + this._ROUTER.get("p") + "." + this._ROUTER.get("c") + "Controller." + this._ROUTER.get("a") + "() 方法是否存在"
				});
			}
		}
		
		return this;
	}
	
	public void error(String type, String[] msg) {
		BufferedReader ready;
		
		try {
			ready = new BufferedReader(new InputStreamReader(new FileInputStream(new File(this.info.get("path") + "/error.html")), "UTF-8"));
			String	temp = "",
					sendOut = "";
			while(null != (temp = ready.readLine())){
				sendOut += temp;
			}

			for ( String str : msg ) {
				sendOut = sendOut.replaceAll("\\{\\$msg\\}", "<span class='msg'>" + str + "</span><br><br>\\{\\$msg\\}");
			}
			
			sendOut = sendOut.replaceAll("\\{\\$msg\\}", "");
			sendOut = sendOut.replaceAll("\\{\\$errorType\\}", (String) StaticMassage.errorType.get(type));
			
			this.echo(sendOut, true);			
		} catch (IOException e) {
			this.echo((String)StaticMassage.errorType.get("missingErrorViewFile"), true);
		}
	}	
	
	/**
	 * 渲染视图
	 */
	protected void display(){
		
		String file = this.info.get("path") + "/" + this._ROUTER.get("p") + "/" + this._ROUTER.get("a") + ".html";

		this.echo(new Template().compile(file, this), true);
	}
	
	/**
	 * 打印一个特定的字符串, 目前只为了调试程序使用
	 * @param str 将要打印的字符串
	 */
	protected void echo(String str) {
		
		this.out.print(str);
	}
	
	/**
	 * 打印一个特定的字符串并根据需要终止输出流, 目前只为了调试程序使用
	 * @param str 将要打印的字符串
	 * @param flag 是否终止输出流 true为终止输出流
	 */
	protected void echo(String str,  boolean flag) {
		
		this.echo(str);
		
		if ( flag ) {
			this.out.flush();
			this.out.close();
		}
	}
	
	/**
	 * 打印 json 串儿, 目前只为了调试程序使用
	 * @param temp 将要打印的对象
	 */
	protected void print_r(Object temp) {
		
		this.out.print(JSONArray.fromObject(temp));
	}
	
	/**
	 * 打印 json 串儿并根据需要终止输出流, 目前只为了调试程序使用
	 * @param temp 将要打印的对象
	 * @param flag 是否终止输出流 true为终止输出流
	 */
	protected void print_r(Object temp, boolean flag) {
		
		this.print_r(temp);
		
		if ( flag ) {
			this.out.flush();
			this.out.close();
		}
	}

	/**
	 * url请求转发
	 * @param p 请求的包
	 * @param c 请求的控制
	 * @param a 请求的控制器动作
	 */
	protected void forward(String p, String c, String a) {
		//获取ServletContext对象, 构建请求转发对象(RequestDispatcher)
		RequestDispatcher rd = 
			App.context.getRequestDispatcher("/index?p=" + p + "&c=" + c + "&a=" + a);
		
		try {
			rd.forward(request, response);
		} catch (ServletException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//调用forward方法实现请求转发
	}
	
	/**
	 * 构造配置文件路径, 方便配置文件读取
	 * @param 文件名
	 * @return String 文件的绝对路径
	 */
	public String P(String file) {
		
		return this.info.get("path") + "/WEB-INF/classes/" + file + ".properties";
	}
	
	protected ModelBase M(String modelName) {
		
		return model;
	}
	
	protected ModelBase M() {
		
		String cls = "app.model." + this._ROUTER.get("p") + "." + this._ROUTER.get("c") + "." + Str.toUpper(this._ROUTER.get("a"), 0) + "Model";
		
		try {
			return new Reflection<ModelBase>(cls, new Class[]{ControllerBase.class}, new Object[]{ this }).getCls();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return null;
	}
}