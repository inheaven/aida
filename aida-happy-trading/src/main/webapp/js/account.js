var ltc_equity_chart;
var ltc_margin_chart;
var ltc_profit_chart;

var btc_equity_chart;
var btc_margin_chart;
var btc_profit_chart;

var ltc_spot_chart;
var btc_spot_chart;
var usd_spot_chart;

var btc_price_chart;
var ltc_price_chart;
var usd_total_chart;

var all_order_rate_chart;

$(function () {
    Highcharts.setOptions({
        global: {
            useUTC: true,
            timezoneOffset: 60
        },
        lang:{
            rangeSelectorZoom: ''
        }
    });

    //BTC
    $.getJSON('/account_info_rest/user_info/BTC', function (data) {
        btc_equity_chart = $('#btc_equity').highcharts('StockChart', {
            title: {text: 'BTC Equity'},
            chart: {animation: false, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            scrollbar: {enabled: false},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'BTC Equity',
                data: data.map(function(a){return [a[0], a[1]]}),
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                enabled: false,
                buttons: [
                    {type: 'hour', count: 1, text: ' '},
                    {type: 'day', count: 1, text: ' '},
                    {type: 'week', count: 1, text: ' '},
                    {type: 'all', count: 1, text: ' '}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();

        btc_margin_chart = $('#btc_margin').highcharts('StockChart', {
            title: {text: 'BTC Margin'},
            chart: {animation: false,  spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            scrollbar: {enabled: false},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'BTC Margin',
                data: data.map(function(a){return [a[0], a[2]]}),
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                enabled: false,
                buttons: [
                    {type: 'hour', count: 1, text: ' '},
                    {type: 'day', count: 1, text: ' '},
                    {type: 'week', count: 1, text: ' '},
                    {type: 'all', count: 1, text: ' '}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();

        btc_profit_chart = $('#btc_profit').highcharts('StockChart', {
            title: {text: 'BTC Profit'},
            chart: {animation: false,  spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            scrollbar: {enabled: false},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'BTC Profit',
                data: data.map(function(a){return [a[0], a[3]]}),
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                enabled: false,
                buttons: [
                    {type: 'hour', count: 1, text: ' '},
                    {type: 'day', count: 1, text: ' '},
                    {type: 'week', count: 1, text: ' '},
                    {type: 'all', count: 1, text: ' '}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();
    });

    //LTC

    $.getJSON('/account_info_rest/user_info/LTC', function (data) {
        ltc_equity_chart = $('#ltc_equity').highcharts('StockChart', {
            title: {text: 'LTC Equity'},
            chart:{animation: true, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{type : 'area',  threshold : null, name: 'LTC Equity',
                data: data.map(function(a){return [a[0], a[1]]}),
                fillColor : {
                    linearGradient : {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops : [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }}],
            rangeSelector : {
                enabled: false,
                buttons : [
                    {type : 'hour', count : 1, text : ' '},
                    {type : 'day', count : 1, text : ' '},
                    {type : 'week', count : 1, text : ' '},
                    {type : 'all', count : 1, text : ' '}
                ],
                //selected : 1,
                inputEnabled : false
            }
        }).highcharts();

        ltc_margin_chart = $('#ltc_margin').highcharts('StockChart', {
            title: {text: 'LTC Margin'},
            chart: {animation: false, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'LTC Margin',
                data: data.map(function(a){return [a[0], a[2]]}),
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                enabled: false,
                buttons: [
                    {type: 'hour', count: 1, text: ' '},
                    {type: 'day', count: 1, text: ' '},
                    {type: 'week', count: 1, text: ' '},
                    {type: 'all', count: 1, text: ' '}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();

        ltc_profit_chart = $('#ltc_profit').highcharts('StockChart', {
            title: {text: 'LTC Profit'},
            chart: {animation: false, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'LTC Profit',
                data: data.map(function(a){return [a[0], a[3]]}),
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                enabled: false,
                buttons: [
                    {type: 'hour', count: 1, text: ' '},
                    {type: 'day', count: 1, text: ' '},
                    {type: 'week', count: 1, text: ' '},
                    {type: 'all', count: 1, text: ' '}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();
    });
    //SPOT

    $.getJSON('/account_info_rest/spot/LTC_SPOT', function (data) {
        ltc_spot_chart = $('#ltc_spot').highcharts('StockChart', {
            title: {text: 'LTC Spot'},
            chart: {animation: false, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'LTC Spot',
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
                enabled: false,
                buttons: [
                    {type: 'hour', count: 1, text: ' '},
                    {type: 'day', count: 1, text: ' '},
                    {type: 'week', count: 1, text: ' '},
                    {type: 'all', count: 1, text: ' '}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();
    });

    $.getJSON('/account_info_rest/spot/BTC_SPOT', function (data) {
        btc_spot_chart = $('#btc_spot').highcharts('StockChart', {
            title: {text: 'BTC Spot'},
            chart: {animation: false, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'BTC Spot',
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
                enabled: false,
                buttons: [
                    {type: 'hour', count: 1, text: ' '},
                    {type: 'day', count: 1, text: ' '},
                    {type: 'week', count: 1, text: ' '},
                    {type: 'all', count: 1, text: ' '}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();
    });

    $.getJSON('/account_info_rest/spot/USD_SPOT', function (data) {
        usd_spot_chart = $('#usd_spot').highcharts('StockChart', {
            title: {text: 'USD Spot'},
            chart: {animation: false, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'USD Spot',
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
                enabled: false,
                buttons: [
                    {type: 'hour', count: 1, text: ' '},
                    {type: 'day', count: 1, text: ' '},
                    {type: 'week', count: 1, text: ' '},
                    {type: 'all', count: 1, text: ' '}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();
    });

    //USD TOTAL

    $.getJSON('/account_info_rest/user_info_total', function (data) {
        usd_total_chart = $('#usd_total').highcharts('StockChart', {
            title: {text: 'BTC Total'},
            chart:{animation: true, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{type : 'area',  threshold : null, name: 'BTC Total',
                data: data.map(function(a){return [a[0], a[1]/a[3]]}),
                fillColor : {
                    linearGradient : {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops : [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }}],
            rangeSelector : {
                enabled: false,
                buttons : [
                    {type : 'hour', count : 1, text : ' '},
                    {type : 'day', count : 1, text : ' '},
                    {type : 'week', count : 1, text : ' '},
                    {type : 'all', count : 1, text : ' '}
                ],
                //selected : 1,
                inputEnabled : false
            }
        }).highcharts();

        btc_price_chart = $('#btc_price').highcharts('StockChart', {
            title: {text: 'BTC Price'},
            chart: {animation: false, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'BTC Price',
                data: data.map(function(a){return [a[0], a[3]]}),
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                enabled: false,
                buttons: [
                    {type: 'hour', count: 1, text: ' '},
                    {type: 'day', count: 1, text: ' '},
                    {type: 'week', count: 1, text: ' '},
                    {type: 'all', count: 1, text: ' '}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();

        ltc_price_chart = $('#ltc_price').highcharts('StockChart', {
            title: {text: 'LTC Price'},
            chart: {animation: false, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'LTC Price',
                data: data.map(function(a){return [a[0], a[4]]}),
                fillColor: {
                    linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops: [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }
            }],
            rangeSelector: {
                enabled: false,
                buttons: [
                    {type: 'hour', count: 1, text: ' '},
                    {type: 'day', count: 1, text: ' '},
                    {type: 'week', count: 1, text: ' '},
                    {type: 'all', count: 1, text: ' '}
                ],
                //selected : 1,
                inputEnabled: false
            }
        }).highcharts();
    });

    //ORDER

    var offset = 0;

    all_order_rate_chart = $('#all_order_rate').highcharts({
        title: {text: 'Trade Volume', style:{"fontSize": "16px"}},
        chart:{animation: true, spacingBottom: 0,  alignTicks: false},
        tooltip: {valueDecimals: 3},
        credits: {enabled: false},
        scrollbar: {enabled: false},
        navigator: {enabled: false, xAxis:{labels:{enabled:false}}},
        rangeSelector: {enabled: false},
        legend: {enabled: false},
        xAxis: {type: 'datetime', minRange: 3600000, min: new Date().getTime() - 24*3600000 + offset, max: new Date().getTime() + 60000 + offset},
        yAxis: [
            {labels: {enabled: true}, title:{text: null}, opposite: true, endOnTick:false},
            {labels: {enabled: false}, title:{text: null}}
        ],
        series: [{
            type : 'column',
            name: 'Trade Volume',
            borderWidth:0,
            pointWidth: 16,
            stacking: 'normal',
            yAxis: 0,
            fillColor: {
                linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                stops: [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
            }
        },{
            type : 'spline',
            name: 'Order Time',
            lineWidth : 0,
            stacking: 'normal',
            marker : {
                enabled : false,
                radius : 4,
                symbol: 'circle',
                fillColor: Highcharts.getOptions().colors[0]
            },
            states:{hover:{enabled: false}},
            tooltip:{pointFormat: '', enabled: false},
            yAxis: 0
        }]

    }).highcharts();

    setInterval(function () {all_order_rate_chart.xAxis[0].setExtremes(new Date().getTime() - 24*3600000 + offset, new Date().getTime() + 60000 + offset);}, 60000);

    Wicket.Event.subscribe("/websocket/closed", function(){
        $('#charts').css('-webkit-filter', 'grayscale(1)');

        $('#reconnect').show();

        var i = 42;
        setInterval(function(){
            $('#reconnect').text(i--);

            if (i == 1){
                location.reload();
            }
        }, 1000);
    });
});

function hour(chart){
    chart.xAxis[0].setExtremes(new Date(new Date().getTime() - 60*60*1000), new Date());
}

function day(chart){
    chart.xAxis[0].setExtremes(new Date(new Date().getTime() - 24*60*60*1000), new Date());
}

function week(chart){
    chart.xAxis[0].setExtremes(new Date(new Date().getTime() - 7*24*60*60*1000), new Date());
}

function all(chart){
    chart.xAxis[0].setExtremes(null, new Date());
}


function hourCharts(){
    hour(ltc_equity_chart);
    hour(ltc_margin_chart);
    hour(ltc_profit_chart);
    hour(btc_equity_chart);
    hour(btc_margin_chart);
    hour(btc_profit_chart);
    hour(ltc_spot_chart);
    hour(btc_spot_chart);
    hour(usd_spot_chart);
    hour(btc_price_chart);
    hour(ltc_price_chart);
    hour(usd_total_chart);
}

function dayCharts(){
    day(ltc_equity_chart);
    day(ltc_margin_chart);
    day(ltc_profit_chart);
    day(btc_equity_chart);
    day(btc_margin_chart);
    day(btc_profit_chart);
    day(ltc_spot_chart);
    day(btc_spot_chart);
    day(usd_spot_chart);
    day(btc_price_chart);
    day(ltc_price_chart);
    day(usd_total_chart);
}

function weekCharts(){
    week(ltc_equity_chart);
    week(ltc_margin_chart);
    week(ltc_profit_chart);
    week(btc_equity_chart);
    week(btc_margin_chart);
    week(btc_profit_chart);
    week(ltc_spot_chart);
    week(btc_spot_chart);
    week(usd_spot_chart);
    week(btc_price_chart);
    week(ltc_price_chart);
    week(usd_total_chart);
}

function allCharts(){
    all(ltc_equity_chart);
    all(ltc_margin_chart);
    all(ltc_profit_chart);
    all(btc_equity_chart);
    all(btc_margin_chart);
    all(btc_profit_chart);
    all(ltc_spot_chart);
    all(btc_spot_chart);
    all(usd_spot_chart);
    all(btc_price_chart);
    all(ltc_price_chart);
    all(usd_total_chart);
}

