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
        },
        lang:{
            rangeSelectorZoom: ''
        }
    });

    //BTC
    $.getJSON('/account_info_rest/equity/BTC', function (data) {
        btc_equity_chart = $('#btc_equity').highcharts('StockChart', {
            title: {text: 'BTC Equity'},
            chart: {animation: false},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            scrollbar: {enabled: false},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'BTC Equity',
                data: data,
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                buttons: [
                    {type: 'hour', count: 1, text: 'Hour'},
                    {type: 'day', count: 1, text: 'Day'},
                    {type: 'week', count: 1, text: 'Week'},
                    {type: 'all', count: 1, text: 'All'}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();
    });

    $.getJSON('/account_info_rest/profit/BTC', function (data) {
        btc_profit_chart = $('#btc_profit').highcharts('StockChart', {
            title: {text: 'BTC Realized Profit'},
            chart: {animation: false},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            scrollbar: {enabled: false},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'BTC Realized Profit',
                data: data,
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                buttons: [
                    {type: 'hour', count: 1, text: 'Hour'},
                    {type: 'day', count: 1, text: 'Day'},
                    {type: 'week', count: 1, text: 'Week'},
                    {type: 'all', count: 1, text: 'All'}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();
    });

    $.getJSON('/account_info_rest/margin/BTC', function (data) {
        btc_margin_chart = $('#btc_margin').highcharts('StockChart', {
            title: {text: 'BTC Margin'},
            chart: {animation: false},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            scrollbar: {enabled: false},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'BTC Margin',
                data: data,
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                buttons: [
                    {type: 'hour', count: 1, text: 'Hour'},
                    {type: 'day', count: 1, text: 'Day'},
                    {type: 'week', count: 1, text: 'Week'},
                    {type: 'all', count: 1, text: 'All'}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();
    });

    //LTC

    $.getJSON('/account_info_rest/equity/LTC', function (data) {
        ltc_equity_chart = $('#ltc_equity').highcharts('StockChart', {
            title: {text: 'LTC Equity'},
            chart:{animation: true},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{type : 'area',  threshold : null, name: 'LTC Equity',
                data: data,
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
                //selected : 1,
                inputEnabled : false
            }
        }).highcharts();
    });

    $.getJSON('/account_info_rest/profit/LTC', function (data) {
        ltc_profit_chart = $('#ltc_profit').highcharts('StockChart', {
            title: {text: 'LTC Realized Profit'},
            chart: {animation: false},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'LTC Realized Profit',
                data: data,
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                buttons: [
                    {type: 'hour', count: 1, text: 'Hour'},
                    {type: 'day', count: 1, text: 'Day'},
                    {type: 'week', count: 1, text: 'Week'},
                    {type: 'all', count: 1, text: 'All'}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();
    });

    $.getJSON('/account_info_rest/margin/LTC', function (data) {
        ltc_margin_chart = $('#ltc_margin').highcharts('StockChart', {
            title: {text: 'LTC Margin'},
            chart: {animation: false},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'LTC Margin',
                data: data,
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                buttons: [
                    {type: 'hour', count: 1, text: 'Hour'},
                    {type: 'day', count: 1, text: 'Day'},
                    {type: 'week', count: 1, text: 'Week'},
                    {type: 'all', count: 1, text: 'All'}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();
    });

    //ORDER
    all_order_rate_chart = $('#all_order_rate').highcharts({
        title: {text: 'Order Time', style:{"fontSize": "16px"}},
        chart:{animation: false},
        tooltip: {valueDecimals: 3},
        credits: {enabled: false},
        scrollbar: {enabled: false},
        navigator: {enabled: false, xAxis:{labels:{enabled:false}}},
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

    $('.highcharts-navigator').hide();
    $('.highcharts-navigator-handle-left').hide();
    $('.highcharts-navigator-handle-right').hide();



});

