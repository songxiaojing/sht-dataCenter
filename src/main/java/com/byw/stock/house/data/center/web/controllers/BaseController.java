package com.byw.stock.house.data.center.web.controllers;

import com.byw.stock.house.data.center.platform.services.AppSystemLogService;
import com.byw.stock.house.data.center.web.utils.UploadFile;
import com.byw.stock.house.data.center.web.utils.http.HttpContentType;
import com.byw.stock.house.platform.core.utils.Assert;
import org.apache.struts2.ServletActionContext;
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper;
import org.springframework.http.HttpStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * 基础控制器.
 * <p>
 * 定制控制器都具有的功能.
 *
 * @author baiyanwei
 * @version 1.0
 * @title BaseController
 * @package com.topsec.tss.core.web.controller
 * @date 2014-5-30
 */
public abstract class BaseController implements IController {

    /**
     * record the user or module operation logging in system.
     */
    protected AppSystemLogService _applicationLoggingService = null;

    /**
     * 系统请求中session服务.
     */


    protected String getLogOperateTime() {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return "操作时间：" + dateFormatter.format(new Date());
    }


    @Override
    public HttpSession getHttpSession() {

        return ServletActionContext.getRequest().getSession();
    }

    @Override
    public HttpServletRequest getRequest() {

        return ServletActionContext.getRequest();
    }

    @Override
    public HttpServletResponse getResponse() {

        return ServletActionContext.getResponse();
    }

    /**
     * 向客户端写错误信息.
     *
     * @param errorMessage
     * @throws Exception
     */
    public void pushBackToClientWithError(String errorMessage) throws Exception {

        pushBackToClient(HttpStatus.INTERNAL_SERVER_ERROR, HttpContentType.TXT, errorMessage);
    }

    /**
     * 向客户端写信息.
     *
     * @param message
     * @throws Exception
     */
    public void pushBackToClient(HttpStatus code, HttpContentType contentType, String message) throws Exception {

        PrintWriter pw = null;
        try {
            HttpServletResponse response = ServletActionContext.getResponse();
            response.setStatus(code.value());
            response.setContentType(contentType.value());
            pw = response.getWriter();
            if (Assert.isEmptyString(message) == false) {
                pw.print(message);
            } else {
                pw.print(code.getReasonPhrase());
            }
            pw.flush();
        } catch (IOException e) {
            throw e;
        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    /**
     * 向客户端写信息 HTTP CODE 200.
     *
     * @param message
     * @throws Exception
     */
    public void pushBackToClient(String message) throws Exception {

        this.pushBackToClient(HttpStatus.OK, HttpContentType.HTML, message);
    }

    /**
     * 取得参数集盒.
     *
     * @return
     * @throws UnsupportedEncodingException
     */
    public HashMap<String, String> getParameterMap() {

        HttpServletRequest request = this.getRequest();
        Enumeration<?> names = request.getParameterNames();
        HashMap<String, String> formInputMap = new HashMap<String, String>();
        while (names.hasMoreElements()) {
            String key = names.nextElement().toString();
            try {
                formInputMap.put(key, request.getParameter(key) == null ? request.getParameter(key) : request.getParameter(key).trim());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //

        return replaceParamFilter(formInputMap, request);
    }

    /**
     * 参数替换.
     *
     * @param formInputMap
     * @param request
     * @return
     */
    private HashMap<String, String> replaceParamFilter(HashMap<String, String> formInputMap, HttpServletRequest request) {

        if (Assert.isEmptyMap(formInputMap) == true) {
            return formInputMap;
        }
        if (formInputMap.containsKey("replace.application.current.user") == true) {
            String replaceAppCurrentUser = formInputMap.get("replace.application.current.user");
            if (Assert.isEmptyString(replaceAppCurrentUser) == false && formInputMap.containsKey(replaceAppCurrentUser) == false) {
                try {
                   // formInputMap.put(replaceAppCurrentUser, this.getCurrentUserByRequest(request).getId().toString());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return formInputMap;
    }

    protected MultiPartRequestWrapper getMultiPartRequestWrapper(HttpServletRequest request) throws Exception {

        MultiPartRequestWrapper mwRequest = (MultiPartRequestWrapper) request;
        if (mwRequest == null) {
            return null;
        }
        validateUploadFileSize(mwRequest);

        return mwRequest;
    }

    private void validateUploadFileSize(MultiPartRequestWrapper mwRequest) throws Exception {

        if (mwRequest == null && mwRequest.getErrors().size() > 0) {
            throw new Exception("上传文件过大!");
        }
    }

    /**
     * 取得上传文件的对象 enctype="multipart/form-module".
     *
     * @param request
     * @return
     */
    public <T extends MultiPartRequestWrapper> HashMap<String, List<UploadFile>> getUploadFileMap(T request) {

        if (request == null) {
            return new HashMap<String, List<UploadFile>>();
        }
        //取得FORM中所有的FILE INPUT
        Enumeration<String> fileParameters = request.getFileParameterNames();
        if (fileParameters == null) {
            return new HashMap<String, List<UploadFile>>();
        }

        HashMap<String, List<UploadFile>> uploadFileMap = new HashMap<String, List<UploadFile>>();
        while (fileParameters.hasMoreElements()) {
            //取得FILE INPUT对应的文件名与上传到服务器后目录中的FILE
            String fileParameterName = fileParameters.nextElement();
            String[] fileNames = request.getFileNames(fileParameterName);
            File[] localfiles = request.getFiles(fileParameterName);
            int size = fileNames.length;
            if (size > localfiles.length) {
                size = localfiles.length;
            }
            ArrayList<UploadFile> uploadFileList = new ArrayList<UploadFile>();
            for (int i = 0; i < size; i++) {
                uploadFileList.add(new UploadFile(fileNames[i], localfiles[i], fileParameterName));
            }
            uploadFileMap.put(fileParameterName, uploadFileList);

        }
        //
        return uploadFileMap;
    }

    /**
     * 将资源list和总数量组成JSON串,返回前台进行解析
     *
     * @param resourceDataList 资源list
     * @param resourceCount    总数量
     * @return
     * @throws Exception
     */
//    protected String buildPageTableRowData(List<? extends AbstractResourceBean> resourceDataList, long resourceCount) throws Exception {
//
//        ResourceModuleService resourceService = ServiceHelper.findService(ResourceModuleService.class);
//
//        JSONObject rowsDataJSONObj = new JSONObject();
//        rowsDataJSONObj.put("total", resourceCount);
//        JSONArray dataArray = new JSONArray();
//        if (resourceDataList != null && resourceDataList.isEmpty() == false) {
//            for (AbstractResourceBean bean : resourceDataList) {
//                dataArray.put(resourceService.getAttributeMapForDataGrid(bean, false, true));
//            }
//        }
//        rowsDataJSONObj.put("rows", dataArray);
//        return rowsDataJSONObj.toString();
//    }

    /**
     * 生成DataTables需要的JSON数据.
     *
     * @param resourceDataList
     * @param resourceCount
     * @param draw
     * @return
     * @throws Exception
     */
//    protected String buildDataTablesRowData(List<? extends AbstractResourceBean> resourceDataList, long resourceCount, String draw) throws Exception {
//
//        ResourceModuleService resourceService = ServiceHelper.findService(ResourceModuleService.class);
//
//        JSONObject rowsDataJSONObj = new JSONObject();
//        JSONArray dataArray = new JSONArray();
//        if (resourceDataList != null && resourceDataList.isEmpty() == false) {
//            for (AbstractResourceBean bean : resourceDataList) {
//                dataArray.put(resourceService.getAttributeMapForDataGrid(bean, false, true));
//            }
//        }
//        rowsDataJSONObj.put("draw", draw);
//        rowsDataJSONObj.put("recordsTotal", resourceCount);
//        rowsDataJSONObj.put("recordsFiltered", resourceCount);
//        rowsDataJSONObj.put("module", dataArray);
//        return rowsDataJSONObj.toString();
//    }

    /**
     * 取得当前用户.
     *
     * @param request
     * @return
     */
//    protected AutUser getCurrentUserByRequest(HttpServletRequest request) throws LossUserException {
//
//        if (request == null) {
//            return null;
//        }
//
//        return this._httpSessionService.getCurrentUser(request);
//    }
}
