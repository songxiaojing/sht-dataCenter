package com.byw.stock.house.data.center.web.controllers.application;

import com.byw.stock.house.data.center.web.controllers.BaseController;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;



@Scope("prototype")
@Controller("ApplicationView")
public class ApplicationView extends BaseController {

    /**
     * 
     * 系统信息.
     * 
     * @return
     */
    public String onlineView() {

        return "online";
    }

}
