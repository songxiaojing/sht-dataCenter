package com.byw.stock.house.data.center.web.listeners; /**
 * Created by martin on 4/26/16.
 */

import com.byw.stock.house.data.center.platform.services.AppSystemLogService;
import com.byw.stock.house.data.center.platform.services.DataBaseStorageService;
import com.byw.stock.house.data.center.platform.services.MessageDigestService;
import com.byw.stock.house.platform.core.services.IPlatformService;
import com.byw.stock.house.platform.core.services.PlatformServiceHelper;
import com.byw.stock.house.platform.core.services.PropertyLoaderService;
import com.byw.stock.house.platform.log.PlatformLogger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.ArrayList;

public class AppPlatformServiceListener implements ServletContextListener{
    /**
     * 平台日志.
     */
    final private static PlatformLogger theLogger = PlatformLogger.getLogger(AppPlatformServiceListener.class);
    //
    /**
     * 启动的服务集合.
     */
    final private ArrayList<IPlatformService> _platformServicesList = new ArrayList<IPlatformService>();

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

        //停止服务
        for (int i = 0; i < _platformServicesList.size(); i++) {
            try {
                _platformServicesList.get(i).stop();
            } catch (Exception e) {
                theLogger.exception(e);
            }
        }

    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {

        //服务启动并注册，解析配置内容
        theLogger.info("startActivator");
        long currentPoint = System.currentTimeMillis();
        AppSystemLogService appSystemLogService = null;
        try {

            //由ServiceHelper统一注册，启动
            appSystemLogService = new AppSystemLogService();
            //###应用平台基础服务 此部分不可改变注册顺序
            //#1 配置文件分析与field字段配置写入服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new PropertyLoaderService(), true, false));
            //#2 用户与模块日志操作记录服务
            _platformServicesList.add(PlatformServiceHelper.registerService(appSystemLogService, true, false));
            //#3 应用节点服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new DataBaseStorageService()));
            //#4 消息签名服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new MessageDigestService()));
            /*
            //#5 消息加密服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new com.topsec.tss.platform.service.RSACryptService()));
            //#7 信息格式化服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new com.topsec.tss.platform.service.MessagePrepareService()));
            //###应用平台基础服务 此部分不可改变注册顺序结束

            //###业务服务注册
            //#1 数据缓存服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new com.topsec.tss.platform.service.CacheService()));
            //#2 鉴权服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new com.topsec.tss.platform.service.AuthenticationService()));
            //#3 资源模型支持服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new com.topsec.tss.platform.service.ResourceModuleService()));
            //#4 安全事件生命周期管理服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new com.topsec.tss.platform.service.EventProcessingEngineService()));
            //#5 目录管理
            _platformServicesList.add(PlatformServiceHelper.registerService(new com.topsec.tss.platform.service.DirectoryStoreService()));
            //#6 数据库计数生成服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new ApplicationCounterService()));
            //#7 系统菜单服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new com.topsec.tss.platform.service.TssCoreMenuService()));
            //#8 系统任务调度服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new com.topsec.tss.platform.service.ScheduleService()));
            //#9 数据汇部接口
            _platformServicesList.add(PlatformServiceHelper.registerService(new com.topsec.tss.platform.service.TssHdfsWebClientService()));
            //#10 DMS服务
            _platformServicesList.add(PlatformServiceHelper.registerService(new com.topsec.tss.platform.service.TssDMSService()));
            //
            applicationLoggingService.recordModuleOperation(ApplictionActivatorListener.class.getName(), "register service", true, "All services has been registered in " + (System.currentTimeMillis() - currentPoint) + "ms");
                */
        } catch (Exception e) {
            theLogger.exception(e);
            //applicationLoggingService.recordModuleOperation(ApplictionActivatorListener.class.getName(), "register service", false, e.toString());
        }

        theLogger.info("finishActivator", String.valueOf(_platformServicesList.size()), String.valueOf(System.currentTimeMillis() - currentPoint));
    }
}
