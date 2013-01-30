package ru.inhell.aida.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.util.time.Duration;
import org.wicketstuff.flot.Color;
import org.wicketstuff.flot.DataSet;
import org.wicketstuff.flot.LineGraphType;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.common.inject.AidaInjector;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.ssa.VectorForecastSSA;
import ru.inhell.aida.util.QuoteUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 25.07.11 23:10
 */
public class EurUsdOraclePage extends WebPage{
    final static QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);
//    final static VectorForecastSSA vf1 = new VectorForecastSSA(512, 256, 8, 30);
    final static VectorForecastSSA vf2 = new VectorForecastSSA(2400, 480, 8
        , 120);
    final static VectorForecastSSA vf3 = new VectorForecastSSA(2400, 480, 64, 120);

    final int width = 300;
    final int delta = 120;

    final int size = 4800;
    final int minutes = 5;

    final int update = 5;

    public EurUsdOraclePage() {
        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        add(container);

        container.add(new Label("time", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return new Date().toString();
            }
        }).setOutputMarkupId(true));

        final IModel<List<Series>> model = new ListModel<Series>(new ArrayList<Series>());
//
        List<Series> data = new ArrayList<Series>();
        List<DataSet> dataSets = new ArrayList<DataSet>();
//
        List<Quote> quotes = quotesBean.getQuotes("EDU1", width+delta);
//
        int index = 0;

        for (Quote quote : quotes){
            dataSets.add(new DataSet(index++, quote.getClose()));
        }

        data.add(new Series(dataSets, "EDU1-1min-average", Color.BLACK, new LineGraphType(null, false, null)));
//        data.add(new Series(dataSets, "EDU1-" + vf1.getName(), Color.GREEN, new LineGraphType(null, false, null)));
//        data.add(new Series(dataSets, "EDU1-" + vf2.getName(), Color.RED, new LineGraphType(null, false, null)));
//        data.add(new Series(dataSets, "EDU1-" + vf3.getName(), Color.BLUE, new LineGraphType(null, false, null)));

        model.setObject(data);


        final AidaFlotPanel flotPanel = new AidaFlotPanel("flot", model);
        flotPanel.setClickable(false);

        container.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(update)) {


            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {

                List<Quote> allMinute = quotesBean.getQuotes("EDU1", size*minutes);

                List<Quote> allQuotes = new ArrayList<Quote>();
                for (int i = 0; i < size; ++i){
                    float avr = 0;

                    for (int k = 0; k < minutes; ++k){
                        Quote q = allMinute.get(i*minutes + k);
                        avr += q.getClose() + q.getOpen() + q.getHigh() + q.getLow();
                    }

                    allQuotes.add(new Quote(null, 0, 0, 0, avr/(minutes*4), 0));
                }

                List<Series> data = new ArrayList<Series>();

                //Quote
                List<DataSet> dataSets = new ArrayList<DataSet>();

                List<Quote> quotes = allQuotes.subList(size - width, size);
                int index = 0;
                for (Quote quote : quotes) {
                    dataSets.add(new DataSet(index++, quote.getClose()));
                }



                //VF1
//                List<DataSet> dataSetsF1 = new ArrayList<DataSet>();
//
//                float[] f1 = vf1.execute(QuoteUtil.getAveragePrices(allQuotes.subList(size - vf1.getN(), size)));
//
//                index = 0;
//                for (int i = vf1.getN() - width; i < vf1.getN() + vf1.getM(); ++i) {
//                    dataSetsF1.add(new DataSet(index++, f1[i]));
//                }
//
//                data.add(new Series(dataSetsF1, "EDU1-" + vf1.getName(), Color.GREEN, new LineGraphType(null, false, null)));

                //VF2
                List<DataSet> dataSetsF2 = new ArrayList<DataSet>();

                float[] f2 = vf2.execute(QuoteUtil.getClosePrices(allQuotes.subList(size - vf2.getN(), size)));

                index = 0;
                for (int i = vf2.getN() - width; i < vf2.getN() + vf2.getM(); ++i) {
                    dataSetsF2.add(new DataSet(index++, f2[i]));
                }

                //VF3
                List<DataSet> dataSetsF3 = new ArrayList<DataSet>();

                float[] f3 = vf3.execute(QuoteUtil.getClosePrices(allQuotes.subList(size - vf3.getN(), size)));

                index = 0;
                for (int i = vf3.getN() - width; i < vf3.getN() + vf3.getM(); ++i) {
                    dataSetsF3.add(new DataSet(index++, f3[i]));
                }

                data.add(new Series(dataSetsF2, "EDU1-" + vf2.getName(), Color.RED, new LineGraphType(null, false, null)));
                data.add(new Series(dataSets, "EDU1-"+minutes+"min-average", Color.BLACK, new LineGraphType(null, false, null)));
                data.add(new Series(dataSetsF3, "EDU1-" + vf3.getName(), Color.GREEN, new LineGraphType(null, false, null)));

                target.appendJavaScript("plot.setData("+getData(data)+"); plot.setupGrid(); plot.draw();");
            }
        });

        add(flotPanel);
    }

    private StringBuffer getData(List<Series> data) {
        final StringBuffer strData = new StringBuffer();
        strData.append("[");
        for(Series dataEntry : data) {
            strData.append(dataEntry.toString());
            strData.append(", ");
        }
        if(data.size()>0)
            strData.setLength(strData.length()-2);
        strData.append("]");
        return strData;
    }
}
