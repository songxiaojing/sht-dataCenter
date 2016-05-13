package com.byw.stock.house.data.center.web.controllers.data;

import com.byw.stock.house.data.center.web.controllers.BaseController;
import com.byw.stock.house.data.center.web.services.ICompanyService;
import com.byw.stock.house.data.center.web.utils.UploadFile;
import com.byw.stock.house.platform.core.utils.Assert;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

/**
 * 股票公司管理
 * Created by martin on 5/10/16.
 */
@Scope("prototype")
@Controller("CompanyManagerController")
public class CompanyManagerController extends BaseController {
    //
    private ICompanyService _companyService = null;

    @Resource(name = "CompanyService")
    public void setService(ICompanyService companyService) {
        this._companyService = companyService;
    }

    /**
     * 导入股票公司与代码
     *
     * @return String
     */
    public String companyImport() {
        HttpServletRequest request = this.getRequest();
        try {
//            取得上传数据文件
            HashMap<String, List<UploadFile>> uploadFileMap = this.getUploadFileMap(this.getMultiPartRequestWrapper(request));
            if(uploadFileMap==null||uploadFileMap.isEmpty()){
                throw new Exception("没有上传文件");
            }
            boolean isUpdateExistsCompany = false;
            String isUpdateExists = request.getParameter("isUpdateExists");
            if (!Assert.isEmptyString(isUpdateExists)) {
                isUpdateExistsCompany = true;
            }
            this._companyService.importCompany(isUpdateExistsCompany, uploadFileMap);
            setForwardMessage("导入成功");
            return "message";
        } catch (Exception e) {
            e.printStackTrace();
            setForwardMessage(e.toString());
            return "message";
        }

    }
}
