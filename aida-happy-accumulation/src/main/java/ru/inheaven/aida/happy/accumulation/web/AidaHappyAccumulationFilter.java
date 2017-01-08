package ru.inheaven.aida.happy.accumulation.web;

import ru.inheaven.aida.happy.accumulation.service.AccumulationService;
import ru.inheaven.aida.happy.accumulation.service.Module;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

/**
 * @author Anatoly A. Ivanov
 *         Date: 08.01.2017.
 */
@WebFilter
public class AidaHappyAccumulationFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Module.getInjector().getInstance(AccumulationService.class);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    }

    @Override
    public void destroy() {

    }
}
