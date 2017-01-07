package ru.inheaven.aida.happy.mining.web;

import ru.inheaven.aida.happy.mining.service.MiningService;
import ru.inheaven.aida.happy.mining.service.Module;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;
import java.io.IOException;

/**
 * @author Anatoly A. Ivanov
 * Date: 08.01.2017.
 */
@WebFilter
public class AidaHappyMiningFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Module.getInjector().getInstance(MiningService.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    }

    @Override
    public void destroy() {

    }
}
