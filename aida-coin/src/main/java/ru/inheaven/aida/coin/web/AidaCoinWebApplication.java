package ru.inheaven.aida.coin.web;

import com.googlecode.wickedcharts.wicket6.JavaScriptResourceRegistry;
import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.core.settings.ITheme;
import de.agilecoders.wicket.core.settings.Theme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;
import org.apache.wicket.Application;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.convert.converter.BigDecimalConverter;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 06.01.14 15:13
 */
public class AidaCoinWebApplication extends WebApplication {
    @Override
    public Class<? extends Page> getHomePage() {
        return TraderList.class;
    }

    @Override
    protected void init() {
        super.init();

        Bootstrap.install(Application.get(), new BootstrapSettings().setThemeProvider(new BootswatchThemeProvider(BootswatchTheme.Slate)));

        getMarkupSettings().setCompressWhitespace(true);
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

        getPageSettings().setVersionPagesByDefault(false);

        getComponentInstantiationListeners().add(new JavaEEComponentInjector(this, (ejbName, ejbType)
                -> "java:module/" + (ejbName == null ? ejbType.getSimpleName() : ejbName)));


        JavaScriptResourceRegistry.getInstance().setHighchartsReference("http://code.highcharts.com/highcharts.js");
        JavaScriptResourceRegistry.getInstance().setHighchartsMoreReference( "http://code.highcharts.com/highcharts-more.js");
        JavaScriptResourceRegistry.getInstance().setHighchartsExportingReference("http://code.highcharts.com/modules/exporting.js");
    }

    @Override
    protected IConverterLocator newConverterLocator() {
        ConverterLocator locator = (ConverterLocator) super.newConverterLocator();

        locator.set(BigDecimal.class, new BigDecimalConverter() {
            @Override
            protected NumberFormat newNumberFormat(Locale locale) {
                NumberFormat numberFormat = super.newNumberFormat(locale);
                numberFormat.setMinimumFractionDigits(8);
                numberFormat.setMaximumFractionDigits(8);

                return numberFormat;
            }
        });

        return locator;
    }
}
