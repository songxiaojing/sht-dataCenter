package com.byw.stock.house.data.center.web.services;

import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;


/**
 * Created by martin on 5/13/16.
 * 基础服务类
 */
@Service("BaseService")
public abstract class BaseService {
    final public static String INPUT_DATE_FORMATTOR = "yyyy-MM-dd HH:mm:ss";

    /**
     * 取得默认的日期格式
     *
     * @return
     */
    protected SimpleDateFormat getDefaultDataFormat() {
        return new SimpleDateFormat(INPUT_DATE_FORMATTOR);
    }

}
