package com.byw.stock.house.data.center.web.services.impl;

import com.byw.stock.house.data.center.web.services.BaseService;
import com.byw.stock.house.data.center.web.services.ICompanyService;
import com.byw.stock.house.data.center.web.utils.UploadFile;
import com.byw.stock.house.platform.log.PlatformLogger;
import com.byw.stock.house.track.module.stock.dao.ListedStockMapper;
import com.byw.stock.house.track.module.stock.module.ListedStock;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by martin on 5/13/16.
 */
@Service("CompanyService")
public class CompanyServiceImpl extends BaseService implements ICompanyService {
    final private static PlatformLogger LOGGER = PlatformLogger.getLogger(CompanyServiceImpl.class);
    //
    private ListedStockMapper _listedStockMapper = null;

    @Autowired
    public void setMapper(ListedStockMapper listedStockMapper) {

        this._listedStockMapper = listedStockMapper;
    }

    @Override
    public void importCompany(boolean isUpdateExistsCompany, HashMap<String, List<UploadFile>> uploadFileMap) throws Exception {
        if (uploadFileMap == null || uploadFileMap.isEmpty()) {
            throw new Exception("没有上传文件");
        }
        //{szCompanyFile=[/home/martin/projects/run/apache-tomcat-8.0.33/bin/../temp/upload/upload_764a10ea_273f_4e82_a8eb_a127c751d621_00000032.tmp], shCompanyFile=[/home/martin/projects/run/apache-tomcat-8.0.33/bin/../temp/upload/upload_764a10ea_273f_4e82_a8eb_a127c751d621_00000031.tmp]}

        if (uploadFileMap.containsKey("szCompanyFile")) {
            List<UploadFile> uploadList = uploadFileMap.get("szCompanyFile");
            if (uploadList != null && !uploadList.isEmpty()) {
                importCompanyFromUploadFile(uploadList.get(0), "SZ");
            }
        }


        if (uploadFileMap.containsKey("shCompanyFile")) {
            List<UploadFile> uploadList = uploadFileMap.get("shCompanyFile");
            if (uploadList != null && !uploadList.isEmpty()) {
                importCompanyFromUploadFile(uploadList.get(0), "SH");
            }
        }
    }


    private int importCompanyFromUploadFile(UploadFile uploadFile, String type) throws Exception {

        // 验证FILE是否存在
        if (uploadFile.getFile() == null || !uploadFile.getFile().exists()) {
            throw new Exception(uploadFile.getFileName() + "不存在");
        }
        FileInputStream fileInputStream = null;
        int createCounter = 0;
        try {
            fileInputStream = new FileInputStream(uploadFile.getFile());
            XSSFWorkbook wb = new XSSFWorkbook(fileInputStream);
            if (wb.getNumberOfSheets() == 0) {
                throw new Exception(uploadFile.getFileName() + "文件中没有数据");
            }

            //遍历XLS的每个sheet.
            for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
                //
                LOGGER.debug("Start to read sheet:" + wb.getSheetName(sheetIndex));
                XSSFSheet sheet = wb.getSheetAt(sheetIndex);
                XSSFRow headRow = sheet.getRow(0);
                LOGGER.debug("Head Line:" + headRow.toString());
                for (int n = 1; n <= sheet.getLastRowNum(); n++) {
                    ListedStock listStockCompany = buildListedStock(sheet.getRow(n), type);
                    if (listStockCompany == null) {
                        continue;
                    }
                    createCounter = createCounter + this._listedStockMapper.insert(listStockCompany);
                }
            }
            return createCounter;
        } catch (Exception e) {
            throw e;
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    private ListedStock buildListedStock(XSSFRow row, String type) throws Exception {
        ListedStock listStockCompany = null;
        if ("SH".equals(type)) {
//            公司代码 	公司简称 	A股代码	A股简称	A股上市日期	A股总股本	A股流通股本
//            600000	  浦发银行	600000	  浦发银行	  1999-11-10	1965298.17	1865347.14
//            600004	  白云机场	600004	  白云机场	  2003-04-28	115000	115000

            listStockCompany = new ListedStock();
            listStockCompany.setBourse("SH");
            listStockCompany.setStockCode("");
            listStockCompany.setCompanyName("");
            listStockCompany.setStockName("");
            listStockCompany.setListingTime(0L);
            listStockCompany.setTotalMarketCapitalization(0L);
            listStockCompany.setCirculationMarketValue(0L);

        } else if ("SZ".equals(type)) {
//            公司代码	公司简称	公司全称	英文名称	注册地址	A股代码	A股简称	A股上市日期	A股总股本	A股流通股本	B股代码	B股 简 称	B股上市日期	B股总股本	B股流通股本	地 区	省 份	城 市	所属行业	公司网址
//            000001	平安银行	平安银行股份有限公司	Ping An Bank Co., Ltd.	广东省深圳市罗湖区深南东路5047号	1	平安银行	4/3/1991	1,423,774,251	-1,087,497,555				0	0	华南	广东	深圳市	J 金融业	www.bank.pingan.com
//            000002	万 科Ａ	万科企业股份有限公司	CHINA VANKE CO., LTD	广东省深圳市盐田区大梅沙环梅路33号万科中心	2	万 科Ａ	1/29/1991	1,134,261,941	1,114,535,755				0	0	华南	广东	深圳市	K 房地产	www.vanke.com

            listStockCompany = new ListedStock();
            listStockCompany.setBourse("SH");
            listStockCompany.setStockCode("");
            listStockCompany.setCompanyName("");
            listStockCompany.setStockName("");
            listStockCompany.setListingTime(0L);
            listStockCompany.setTotalMarketCapitalization(0L);
            listStockCompany.setCirculationMarketValue(0L);
        }


        return listStockCompany;
    }
}
