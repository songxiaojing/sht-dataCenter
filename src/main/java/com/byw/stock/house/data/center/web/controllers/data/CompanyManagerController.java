package com.byw.stock.house.data.center.web.controllers.data;

import com.byw.stock.house.data.center.web.controllers.BaseController;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

/**
 * Created by martin on 5/10/16.
 */
@Scope("prototype")
@Controller("CompanyManagerController")
public class CompanyManagerController extends BaseController {
    public String companyImport(){
        setForwardMessage("New Message");
        return "message";
    }
}
