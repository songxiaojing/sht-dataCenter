package com.byw.stock.house.data.center.platform.services;

import com.byw.stock.house.platform.core.metrics.AbstractMetricMBean;
import com.byw.stock.house.platform.core.services.IPlatformService;
import com.byw.stock.house.platform.log.PlatformLogger;

import javax.management.DynamicMBean;



/**
 * 
 * 用户与模块日志操作记录服务.
 * 
 * 系统通过此服务记录用户的操作日志与系统各模块关键数据更新日志.
 * 
 * @title AppSystemLogService
 * @package com.topsec.tss.core.platform.service
 * @author baiyanwei
 * @version 1.0
 * @date 2014-5-29
 * 
 */
public class AppSystemLogService extends AbstractMetricMBean implements IPlatformService, DynamicMBean {

    /**
     * 用户操作日志标识.
     */
    final public static String USER_OPERATION = "user";
    /**
     * 模块日志标识.
     */
    final public static String MODULE_OPERATION = "module";
    /**
     * 数据库触发器前缀.
     */
    final public static String LOG_RECORD_MESSAGE_FORMATTER = "{}{}{}{}#@##@#";
    //
    final private static PlatformLogger theLogger = PlatformLogger.getLogger(AppSystemLogService.class);

    public String _jmxObjectName = "sht.platform.services:type=AppSystemLogService";

    @Override
    public void start() throws Exception {

        long currentPoint = System.currentTimeMillis();
        this.registerMBean(_jmxObjectName, this);
        theLogger.info("startUp", LOG_RECORD_MESSAGE_FORMATTER, String.valueOf(System.currentTimeMillis() - currentPoint));
    }

    @Override
    public void stop() throws Exception {

        // unregister itself
        this.unRegisterMBean(_jmxObjectName);

    }

    /**
     * 写入用户操作日志到数据库表中.
     * 
     * @param operator
     * @param operation
     * @param result
     * @param message
     */
    public void recordUserOperation(String operatorUser, String operation, boolean isSuccess, String message) {

        if (operatorUser == null) {
            return;
        }
        //INSERT INTO APP_OPERATION_LOG(CREATE_AT,OPERATOR,OPERATION,RESULT,MESSAGE,LOGGING_ID) 
        //VALUES (NEW.timestmp,NEW.arg0,NEW.arg1,NEW.arg2,NEW.formatted_message,NEW.event_id);
        //theLogger.info("yourMessage{}{}{}{}", 1, 1, 3, 4);
        try {
            theLogger.info(LOG_RECORD_MESSAGE_FORMATTER + message, operatorUser, operation, isSuccess ? "成功" : "失败", USER_OPERATION);
        } catch (Exception e) {
            theLogger.exception(e);
        }
    }

    /**
     * 写入模块操作日志到数据库表中.
     * 
     * @param module
     * @param operation
     * @param result
     * @param message
     */
    public void recordModuleOperation(String module, String operation, boolean isSuccess, String message) {

        //INSERT INTO APP_SYS_LOG(CREATE_AT,MODULE,CLAZZ,METHOD,OPERATION,RESULT,MESSAGE,LOGGING_ID) 
        //VALUES (NEW.timestmp,NEW.arg0,NEW.logger_name,NEW.caller_method,NEW.arg1,NEW.arg2,NEW.formatted_message,NEW.event_id);
        try {
            theLogger.info(LOG_RECORD_MESSAGE_FORMATTER + message, module, operation, isSuccess ? "成功" : "失败", MODULE_OPERATION);
        } catch (Exception e) {
            theLogger.exception(e);
        }
    }
}
