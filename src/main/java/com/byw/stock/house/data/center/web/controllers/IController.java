package com.byw.stock.house.data.center.web.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;



/**
 * 
 * WEB MVC 控制器接口.
 * 
 * 控制器具有的一般行为，包括控制器被初始后，注入的Service实例的操作.
 * 
 * @title IController
 * @package com.topsec.tss.core.web.controller
 * @author baiyanwei
 * @version 1.0
 * @date 2014-5-14
 * 
 */
public interface IController {

    /**
     * 取得Response实例.
     * 
     * @return
     */
    public HttpServletResponse getResponse();

    /**
     * 取得Request实例.
     * 
     * @return
     */
    public HttpServletRequest getRequest();

    /**
     * 从Request中取得Session.
     * 
     * @return
     */
    public HttpSession getHttpSession();
}
