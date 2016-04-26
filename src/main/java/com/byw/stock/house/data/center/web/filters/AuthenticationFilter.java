package com.byw.stock.house.data.center.web.filters;

import javax.servlet.*;
import java.io.IOException;

/**
 * Created by martin on 4/26/16.
 */
public class AuthenticationFilter implements javax.servlet.Filter {
    public void destroy() {
    }

    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws ServletException, IOException {
        chain.doFilter(req, resp);
    }

    public void init(FilterConfig config) throws ServletException {

    }

}