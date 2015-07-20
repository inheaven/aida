var ltc_equity_chart;
var ltc_margin_chart;
var ltc_profit_chart;

var btc_equity_chart;
var btc_margin_chart;
var btc_profit_chart;

var all_order_rate_chart;

$(function () {
    Highcharts.setOptions({
        global: {
            useUTC: false
        }
    });
    //BTC
    btc_equity_chart = $('#btc_equity').highcharts('StockChart', {
        title:{text: 'BTC Equity'},
        tooltip: {valueDecimals: 3},
        credits:{enabled: false},
        navigator: {enabled: false},
        scrollbar: {enabled: false},
        xAxis: {type: 'datetime'},
        series: [{type : 'area',  threshold : null, name : 'BTC Equity',
            fillColor : {
                linearGradient : {x1: 0, y1: 0, x2: 0, y2: 1},
                stops : [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
            }}],
        rangeSelector : {
            buttons : [
                {type : 'hour', count : 1, text : 'Hour'},
                {type : 'day', count : 1, text : 'Day'},
                {type : 'week', count : 1, text : 'Week'},
                {type : 'all', count : 1, text : 'All'}
            ],
            selected : 1,
            inputEnabled : false
        }
    }).highcharts();

    btc_profit_chart = $('#btc_profit').highcharts('StockChart', {
        title:{text: 'BTC Realized Profit'},
        tooltip: {valueDecimals: 3},
        credits:{enabled: false},
        navigator: {enabled: false},
        scrollbar: {enabled: false},
        xAxis: {type: 'datetime'},
        series: [{type : 'area',  threshold : null, name : 'BTC Realized Profit',
            fillColor : {
                linearGradient : {x1: 0, y1: 0, x2: 0, y2: 1},
                stops : [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
            }}],
        rangeSelector : {
            buttons : [
                {type : 'hour', count : 1, text : 'Hour'},
                {type : 'day', count : 1, text : 'Day'},
                {type : 'week', count : 1, text : 'Week'},
                {type : 'all', count : 1, text : 'All'}
            ],
            selected : 1,
            inputEnabled : false
        }
    }).highcharts();

    btc_margin_chart = $('#btc_margin').highcharts('StockChart', {
        title:{text: 'BTC Margin'},
        tooltip: {valueDecimals: 3},
        credits:{enabled: false},
        navigator: {enabled: false},
        scrollbar: {enabled: false},
        xAxis: {type: 'datetime'},
        series: [{type : 'area',  threshold : null, name : 'BTC Margin',
            fillColor : {
                linearGradient : {x1: 0, y1: 0, x2: 0, y2: 1},
                stops : [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
            }}],
        rangeSelector : {
            buttons : [
                {type : 'hour', count : 1, text : 'Hour'},
                {type : 'day', count : 1, text : 'Day'},
                {type : 'week', count : 1, text : 'Week'},
                {type : 'all', count : 1, text : 'All'}
            ],
            selected : 1,
            inputEnabled : false
        }
    }).highcharts();

    //LTC

    ltc_equity_chart = $('#ltc_equity').highcharts('StockChart', {
        title: {text: 'LTC Equity'},
        tooltip: {valueDecimals: 3},
        credits: {enabled: false},
        scrollbar: {enabled: false},
        navigator: {enabled: false},
        xAxis: {type: 'datetime'},
        series: [{type : 'area',  threshold : null, name: 'LTC Equity',
            fillColor : {
                linearGradient : {x1: 0, y1: 0, x2: 0, y2: 1},
                stops : [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
            }}],
        rangeSelector : {
            buttons : [
                {type : 'hour', count : 1, text : 'Hour'},
                {type : 'day', count : 1, text : 'Day'},
                {type : 'week', count : 1, text : 'Week'},
                {type : 'all', count : 1, text : 'All'}
            ],
            selected : 1,
            inputEnabled : false
        }
    }).highcharts();

    ltc_profit_chart = $('#ltc_profit').highcharts('StockChart', {
        title: {text: 'LTC Realized Profit'},
        tooltip: {valueDecimals: 3},
        credits: {enabled: false},
        scrollbar: {enabled: false},
        navigator: {enabled: false},
        xAxis: {type: 'datetime'},
        series: [{type : 'area',  threshold : null, name: 'LTC Realized Profit',
            fillColor : {
                linearGradient : {x1: 0, y1: 0, x2: 0, y2: 1},
                stops : [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
            }}],
        rangeSelector : {
            buttons : [
                {type : 'hour', count : 1, text : 'Hour'},
                {type : 'day', count : 1, text : 'Day'},
                {type : 'week', count : 1, text : 'Week'},
                {type : 'all', count : 1, text : 'All'}
            ],
            selected : 1,
            inputEnabled : false
        }
    }).highcharts();

    ltc_margin_chart = $('#ltc_margin').highcharts('StockChart', {
        title: {text: 'LTC Margin'},
        tooltip: {valueDecimals: 3},
        credits: {enabled: false},
        scrollbar: {enabled: false},
        navigator: {enabled: false},
        xAxis: {type: 'datetime'},
        series: [{type : 'area',  threshold : null, name: 'LTC Margin',
            fillColor : {
                linearGradient : {x1: 0, y1: 0, x2: 0, y2: 1},
                stops : [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
            }}],
        rangeSelector : {
            buttons : [
                {type : 'hour', count : 1, text : 'Hour'},
                {type : 'day', count : 1, text : 'Day'},
                {type : 'week', count : 1, text : 'Week'},
                {type : 'all', count : 1, text : 'All'}
            ],
            selected : 1,
            inputEnabled : false
        }
    }).highcharts();

    //ORDER
    all_order_rate_chart = $('#all_order_rate').highcharts({
        title: {text: 'Order Time', style:{"fontSize": "16px"}},
        tooltip: {valueDecimals: 3},
        credits: {enabled: false},
        scrollbar: {enabled: false},
        navigator: {enabled: false},
        rangeSelector: {enabled: false},
        legend: {enabled: false},
        xAxis: {type: 'datetime'},
        yAxis: {labels: {enabled: false}, tickWidth: 0, title:{text: null}, min:0},
        series: [{type : 'spline', name: 'Order Time',
            lineWidth : 0,
            marker : {
                enabled : true,
                radius : 5
            },
            states:{hover:{enabled: false}},
            tooltip:{pointFormat: ''}
            }]
    }).highcharts();

});

