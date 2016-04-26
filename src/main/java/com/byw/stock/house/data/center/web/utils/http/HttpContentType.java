package com.byw.stock.house.data.center.web.utils.http;

/**
 * 
 * HTTP头中ContentType定义.
 * 
 * 主要用用于定义HTTP Response的header中的contentType常量，主要包括HTML/TXT/JSON/XML/ZIP.
 * 
 * @title HttpContentType
 * @package com.topsec.tss.core.web.util.http
 * @author baiyanwei
 * @version
 * @date 2014-5-14
 * 
 */
public enum HttpContentType {

    TXT("text/plain"),
    JSON("text/json"),
    HTML("text/html"),
    XML("text/xml"),
    ZIP("application/zip");

    private final String contentType;

    private HttpContentType(String type) {

        this.contentType = type;
    }

    /**
     * Return the integer value of this status code.
     */
    public String value() {

        return this.contentType;
    }

    /**
     * Return a string representation of this status code.
     */
    @Override
    public String toString() {

        return contentType;
    }
}
