var ltc_equity_chart;
var ltc_margin_chart;
var ltc_profit_chart;

var btc_equity_chart;
var btc_margin_chart;
var btc_profit_chart;

$(function () {
    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });
    //BTC
    btc_equity_chart = $('#btc_equity').highcharts('StockChart', {
        title:{text: 'BTC Equity'},
        credits:{enabled: false},
        //rangeSelector: {enabled: false},
        navigator: {enabled: false},
        scrollbar: {enabled: false},
        xAxis: {type: 'datetime'},
        series: [{type : 'area',  threshold : null, name : 'BTC Equity'}]
    }).highcharts();

    btc_margin_chart = $('#btc_margin').highcharts('StockChart', {
        title:{text: 'BTC Margin'},
        credits:{enabled: false},
        //rangeSelector: {enabled: false},
        navigator: {enabled: false},
        scrollbar: {enabled: false},
        xAxis: {type: 'datetime'},
        series: [{type : 'area',  threshold : null, name : 'BTC Margin'}]
    }).highcharts();

    //LTC

    ltc_equity_chart = $('#ltc_equity').highcharts('StockChart', {
        title: {text: 'LTC Equity'},
        credits: {enabled: false},
        scrollbar: {enabled: false},
        //rangeSelector: {enabled: false},
        navigator: {enabled: false},
        xAxis: {type: 'datetime'},
        series: [{type : 'area',  threshold : null, name: 'LTC Equity'}]
    }).highcharts();

    ltc_margin_chart = $('#ltc_margin').highcharts('StockChart', {
        title: {text: 'LTC Margin'},
        credits: {enabled: false},
        scrollbar: {enabled: false},
        //rangeSelector: {enabled: false},
        navigator: {enabled: false},
        xAxis: {type: 'datetime'},
        series: [{type : 'area',  threshold : null, name: 'LTC Margin'}]
    }).highcharts();


});

