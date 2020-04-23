package ru.inheaven.aida.stat.web;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import ru.inheaven.aida.stat.Module;
import ru.inheaven.aida.stat.bitcoin.BlockService;

/**
 * @author inheaven on 17.12.2016.
 */
public class AidaStatApplication extends WebApplication{
    @Override
    protected void init() {
        super.init();

        Module.getInjector().getInstance(BlockService.class);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }
}
