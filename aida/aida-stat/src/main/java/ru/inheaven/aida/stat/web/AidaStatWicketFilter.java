package ru.inheaven.aida.stat.web;

import org.apache.wicket.protocol.http.WicketFilter;

import javax.servlet.annotation.WebFilter;
import javax.servlet.annotation.WebInitParam;

/**
 * @author inheaven on 17.12.2016.
 */
@WebFilter(
        value = "/*",
        initParams = {
                @WebInitParam(name = "applicationClassName", value = "ru.aida.stat.web.AidaStatApplication")
        })
public class AidaStatWicketFilter extends WicketFilter {

}
