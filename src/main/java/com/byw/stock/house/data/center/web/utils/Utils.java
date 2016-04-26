package com.byw.stock.house.data.center.web.utils;


import com.byw.stock.house.platform.core.utils.Assert;
import com.byw.stock.house.platform.core.utils.Constants;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

;


/**
 * 
 * WEB 应用中工具类.
 * 
 * WEB 应用中工具类.
 * 
 * @title Utils
 * @package com.topsec.tss.core.web.utils
 * @author baiyanwei
 * @version
 * @date 2014-7-18
 * 
 */
public class Utils {

    
    final public static String _REPLACE = "_replace";

    /**
     * 取得系统参数.
     * 
     * @param paremeterName
     * @return
     */
    public static String getSystemParameter(String paremeterName) {

        return System.getProperty(paremeterName);
    }

    /**
     * 设置系统参数.
     * 
     * @param paremeterName
     * @param value
     */
    public static void setJvmEnvParameter(String paremeterName, String value) {

        System.setProperty(paremeterName, value);
    }

    /**
     * 将字符串的第一个节字大写.
     * 
     * @param context
     * @return
     */
    public static String firstLitterUp(String context) {

        if (Assert.isEmptyString(context) == true) {
            return context;
        }
        if (context.length() == 1) {
            return context.toUpperCase();
        } else {
            return context.substring(0, 1).toUpperCase() + context.substring(1);
        }

    }

    /**
     * 取得当前使用的样式.
     * 
     * @param userName
     * @return
     */
    public static String getCurrentStyleName(String userName) {

        return "tss";
    }

   
   

    /**
     * 获取N天后的日期
     * 
     * @param date
     * @param days
     * @return
     */
    public static Date getDelayDate(Date date, int days) {

        if (date == null)
            return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setLenient(false);
        calendar.setTime(date);
        calendar.add(Calendar.DATE, days);
        return calendar.getTime();
    }

   
    
   

    /**
     * 将下载文件名进行编码处理，支持中文.
     * 
     * @param targetFileName
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String enCodingDownloadFileNameForHttpResponseHeader(String targetFileName, String fileNameEncodeName) throws UnsupportedEncodingException {

        if (Assert.isEmptyString(targetFileName) == true) {
            return "";
        }

        if (Assert.isEmptyString(fileNameEncodeName) == true) {
            return new String(targetFileName.getBytes(Constants.UTF_8), "ISO8859-1");
        } else {
            //
            return new String(targetFileName.getBytes(fileNameEncodeName), "ISO8859-1");
        }
    }
}
