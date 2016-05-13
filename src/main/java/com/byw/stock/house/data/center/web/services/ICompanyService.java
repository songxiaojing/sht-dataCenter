package com.byw.stock.house.data.center.web.services;

import com.byw.stock.house.data.center.web.utils.UploadFile;

import java.util.HashMap;
import java.util.List;

/**
 * 股票公司管理服务
 * Created by martin on 5/13/16.
 */
public interface ICompanyService extends IService {
    /**
     * 导入股票公司代码
     *
     * @param isUpdateExistsCompany boolean
     * @param uploadFileMap         HashMap<String, List<UploadFile>>
     * @throws Exception
     */
    public void importCompany(boolean isUpdateExistsCompany, HashMap<String, List<UploadFile>> uploadFileMap) throws Exception;
}
