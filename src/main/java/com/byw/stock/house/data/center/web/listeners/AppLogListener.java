package com.byw.stock.house.data.center.web.listeners;

import com.byw.stock.house.data.center.web.utils.Utils;
import com.byw.stock.house.data.center.web.utils.app.AppReferent;
import com.byw.stock.house.data.center.web.utils.app.AppServletContextUtils;
import com.byw.stock.house.platform.core.utils.Assert;
import com.byw.stock.house.platform.log.PlatformLoggerConfiguration;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;


public class AppLogListener implements ServletContextListener {


    @Override
    public void contextDestroyed(ServletContextEvent arg0) {

    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        //填写配置文件
        fillParameterInSystem(arg0);
        //设置APPLICATION的引用
        AppServletContextUtils.setServletContext(arg0.getServletContext());
        //取得LOGGING配置文件名称
        String loggingCfgPath = arg0.getServletContext().getInitParameter(AppReferent.app_cfg_log_path.getValue());
        if (Assert.isEmptyString(loggingCfgPath)==true){
            System.out.println(AppReferent.app_cfg_log_path.getValue()+" is invalidate.");
            System.exit(1);
        }
        //
        try {
            ArrayList<Class<?>> frameworkClassList = new ArrayList<Class<?>>();
            //frameworkClassList.add(AppSystemLogService.class);
            //初始化LOGGING
            PlatformLoggerConfiguration.getInstance().initConfigurationForLogging(loggingCfgPath, frameworkClassList);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 将参数写入系统中, 根据WEB.XML中的context-param参数填充到System中.
     * 
     * @param sce
     */
    private void fillParameterInSystem(ServletContextEvent sce) {
        //设置启动时间
        Utils.setJvmEnvParameter(AppReferent.app_run_startup_time.getValue(), String.valueOf(System.currentTimeMillis()));
        //LOGGING配置文件
        Utils.setJvmEnvParameter(AppReferent.app_cfg_log_path.getValue(), sce.getServletContext().getInitParameter(AppReferent.app_cfg_log_path.getValue()));
        //Services配置文件
        Utils.setJvmEnvParameter(AppReferent.app_cfg_service_Path.getValue(), sce.getServletContext().getInitParameter(AppReferent.app_cfg_service_Path.getValue()));
    }
}
