package com.byw.stock.house.data.center.web.utils.app;

public enum AppReferent {

    app_cfg_log_path("app.cfg.log.path"),
    app_cfg_service_Path("app.cfg.service.path"),
    app_run_startup_time("app.run.startup.time"),
    session_current_user("session.current.user"),
    session_current_permissions("session.current.permissions");

    private final String _parameterName;

    private AppReferent(String parameterName) {

        this._parameterName = parameterName;
    }

    public String getValue() {

        return this._parameterName;
    }


    @Override
    public String toString() {

        return _parameterName;
    }
}
