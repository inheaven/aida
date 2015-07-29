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
            useUTC: false
        },
        lang:{
            rangeSelectorZoom: ''
        }
    });

    //BTC
    $.getJSON('/account_info_rest/user_info/BTC', function (data) {
        btc_equity_chart = $('#btc_equity').highcharts('StockChart', {
            title: {text: 'BTC Equity'},
            chart: {animation: false, marginTop: 5},
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
            chart: {animation: false, marginTop: 5},
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
            title: {text: 'BTC Realized Profit'},
            chart: {animation: false, marginTop: 5},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            scrollbar: {enabled: false},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'BTC Realized Profit',
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
            chart:{animation: true, marginTop: 5},
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
            chart: {animation: false, marginTop: 5},
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
            title: {text: 'LTC Realized Profit'},
            chart: {animation: false, marginTop: 5},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: 'LTC Realized Profit',
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
            chart: {animation: false, marginTop: 5},
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
            chart: {animation: false, marginTop: 5},
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
            chart: {animation: false, marginTop: 5},
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
            title: {text: 'USD Total'},
            chart:{animation: true, marginTop: 5},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            scrollbar: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            xAxis: {type: 'datetime'},
            series: [{type : 'area',  threshold : null, name: 'USD Total',
                data: data.map(function(a){return [a[0], a[1]]}),
                fillColor : {
                    linearGradient : {x1: 0, y1: 0, x2: 0, y2: 1},
                    stops : [
                        [0, Highcharts.getOptions().colors[0]],
                        [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                    ]
                }}],
            rangeSelector : {
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
            chart: {animation: false, marginTop: 5},
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
            chart: {animation: false, marginTop: 5},
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

    var offset = -new Date().getTimezoneOffset()*60000 + 60000*60;

    all_order_rate_chart = $('#all_order_rate').highcharts({
        title: {text: 'Order Time', style:{"fontSize": "16px"}},
        chart:{animation: true},
        tooltip: {valueDecimals: 3},
        credits: {enabled: false},
        scrollbar: {enabled: false},
        navigator: {enabled: false, xAxis:{labels:{enabled:false}}},
        rangeSelector: {enabled: false},
        legend: {enabled: false},
        xAxis: {type: 'datetime', minRange: 3600000, min: new Date().getTime() - 3600000 + offset, max: new Date().getTime() + 60000 + offset},
        yAxis: {labels: {enabled: false}, tickWidth: 0, title:{text: null}, min:0},
        series: [{type : 'spline', name: 'Order Time',
            lineWidth : 0,
            marker : {
                enabled : true,
                radius : 4
            },
            states:{hover:{enabled: false}},
            tooltip:{pointFormat: ''}
        }]
    }).highcharts();

    setInterval(function () {all_order_rate_chart.xAxis[0].setExtremes(new Date().getTime() - 3600000 + offset, new Date().getTime() + 60000 + offset);}, 60000);

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

