var chart_3d;
var chart_3d_cn;

var chart_7_ltc_equity;
var chart_7_btc_equity;
var chart_7_btc_price;
var chart_7_ltc_price;

var chart_7_ltc_spot;
var chart_7_btc_spot;
var chart_7_usd_spot;
var chart_7_total;

var chart_8_ltc_spot;
var chart_8_btc_spot;
var chart_8_cny_spot;
var chart_8_total;

var all_order_rate_chart;

function areaChart(id, title, data){    
        return $('#'+id).highcharts('StockChart', {
            title: {text: title},
            chart: {animation: false, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            navigator: {height: 0, xAxis: {labels: {enabled: false}}},
            scrollbar: {enabled: false},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'area', threshold: null, name: title,
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
}

$(function () {
    Highcharts.setOptions({
        global: {
            useUTC: true,
            timezoneOffset: -360
        },
        lang:{
            rangeSelectorZoom: ''
        }
    });
    
    //FUTURES
    
    //$.getJSON('/account_info_rest/user_info/7/LTC', function (data) {
    //    chart_7_ltc_equity = areaChart('ltc_equity', 'LTC Equity', data);
    //});
    //
    //$.getJSON('/account_info_rest/user_info/7/BTC', function (data) {
    //    chart_7_btc_equity = areaChart('btc_equity', 'BTC Equity', data);
    //});



    //3D

    $.getJSON('/account_info_rest/3d/7', function (data) {
        chart_3d =  $('#3d').highcharts( {
            credits: {enabled: false},
            title: {text: 'VOLUME', style:{"fontSize": "16px"}},
            chart: {
                type: 'scatter',
                margin: 70,
                options3d: {
                    enabled: true,
                    alpha: 10,
                    beta: 30,
                    depth: 500,
                    viewDistance: 0,

                    frame: {
                        bottom: { size: 1, color: 'rgba(0,0,0,0.02)' },
                        back: { size: 1, color: 'rgba(0,0,0,0.04)' },
                        side: { size: 1, color: 'rgba(0,0,0,0.06)' }
                    }
                }
            },
            plotOptions: {
                scatter: {
                    width: 100,
                    height: 100,
                    depth: 100
                }
            },
            yAxis: {
              // min: 0,
              // max: 3000,
               title: null
            },
            xAxis: {
                type: 'datetime',
                gridLineWidth: 1
            },
            zAxis: {
              // min: 0,
             //  max: 3000,
               showFirstLabel: false
            },
            legend: {
                enabled: false
            },
            series: [{
                name: 'LTC',
                data: data.map(function(d){return [d[0], d[2], d[1]]}),
                marker:{radius:3}
            }
                , {
                name: 'BTC',
                data: data.map(function(d){return [d[0], d[3], d[1]]})
            }
            ]
        }).highcharts();

        // Add mouse events for rotation
        $(chart_3d.container).bind('mousedown.hc touchstart.hc', function (e) {
            e = chart_3d.pointer.normalize(e);

            var posX = e.pageX,
                posY = e.pageY,
                alpha = chart_3d.options.chart.options3d.alpha,
                beta = chart_3d.options.chart.options3d.beta,
                newAlpha,
                newBeta,
                sensitivity = 5; // lower is more sensitive

            $(document).bind({
                'mousemove.hc touchdrag.hc': function (e) {
                    // Run beta
                    newBeta = beta + (posX - e.pageX) / sensitivity;
                    chart_3d.options.chart.options3d.beta = newBeta;

                    // Run alpha
                    newAlpha = alpha + (e.pageY - posY) / sensitivity;
                    chart_3d.options.chart.options3d.alpha = newAlpha;

                    chart_3d.redraw(false);
                },
                'mouseup touchend': function () {
                    $(document).unbind('.hc');
                }
            });
        });
    });

    $.getJSON('/account_info_rest/3d/8', function (data) {
        chart_3d_cn =  $('#3d_cn').highcharts( {
            credits: {enabled: false},
            title: {text: 'VOLUME CN', style:{"fontSize": "16px"}},
            chart: {
                type: 'scatter',
                margin: 70,
                options3d: {
                    enabled: true,
                    alpha: 10,
                    beta: 30,
                    depth: 500,
                    viewDistance: 0,

                    frame: {
                        bottom: { size: 1, color: 'rgba(0,0,0,0.02)' },
                        back: { size: 1, color: 'rgba(0,0,0,0.04)' },
                        side: { size: 1, color: 'rgba(0,0,0,0.06)' }
                    }
                }
            },
            plotOptions: {
                scatter: {
                    width: 100,
                    height: 100,
                    depth: 100
                }
            },
            yAxis: {
                //min: 0,
                //max: 10000,
                title: null
            },
            xAxis: {
                type: 'datetime',
                gridLineWidth: 1
            },
            zAxis: {
               // min: 0,
               // max: 10000,
                showFirstLabel: false
            },
            legend: {
                enabled: false
            },
            series: [{
                name: 'LTC',
                data: data.map(function(d){return [d[0], d[2], d[1]]}),
                marker:{radius:3}
            }
                , {
                    name: 'BTC',
                    data: data.map(function(d){return [d[0], d[3], d[1]]})
                }
            ]
        }).highcharts();

        // Add mouse events for rotation
        $(chart_3d_cn.container).bind('mousedown.hc touchstart.hc', function (e) {
            e = chart_3d_cn.pointer.normalize(e);

            var posX = e.pageX,
                posY = e.pageY,
                alpha = chart_3d_cn.options.chart.options3d.alpha,
                beta = chart_3d_cn.options.chart.options3d.beta,
                newAlpha,
                newBeta,
                sensitivity = 5; // lower is more sensitive

            $(document).bind({
                'mousemove.hc touchdrag.hc': function (e) {
                    // Run beta
                    newBeta = beta + (posX - e.pageX) / sensitivity;
                    chart_3d_cn.options.chart.options3d.beta = newBeta;

                    // Run alpha
                    newAlpha = alpha + (e.pageY - posY) / sensitivity;
                    chart_3d_cn.options.chart.options3d.alpha = newAlpha;

                    chart_3d_cn.redraw(false);
                },
                'mouseup touchend': function () {
                    $(document).unbind('.hc');
                }
            });
        });
    });




    
    //USD

    $.getJSON('/account_info_rest/user_info/7/LTC_SPOT', function (data) {
        chart_7_ltc_spot = areaChart('ltc_spot', 'LTC Spot', data);


    });

    $.getJSON('/account_info_rest/user_info/7/BTC_SPOT', function (data) {
        chart_7_btc_spot = areaChart('btc_spot', 'BTC Spot', data);
    });

    $.getJSON('/account_info_rest/user_info/7/USD_SPOT', function (data) {
        chart_7_usd_spot = areaChart('usd_spot', 'USD Spot', data);
    });
    
    //CNY

    $.getJSON('/account_info_rest/user_info/8/LTC_SPOT', function (data) {
        chart_8_ltc_spot = areaChart('ltc_spot_cn', 'LTC Spot CN', data);
    });

    $.getJSON('/account_info_rest/user_info/8/BTC_SPOT', function (data) {
        chart_8_btc_spot = areaChart('btc_spot_cn', 'BTC Spot CN', data);
    });

    $.getJSON('/account_info_rest/user_info/8/CNY_SPOT', function (data) {
        chart_8_cny_spot = areaChart('cny_spot', 'CNY Spot', data);
    });       

    //TOTAL

    $.getJSON('/account_info_rest/user_info_total/7', function (data) {
        chart_7_total = areaChart('total', 'BTC Total', data.map(function(a){return [a[0], a[1]]}));
        
        chart_7_ltc_price = areaChart('ltc_price', 'LTC Price', data.map(function(a){return [a[0], a[4]]}));
        chart_7_btc_price = areaChart('btc_price', 'BTC Price', data.map(function(a){return [a[0], a[3]]}));
    });

    $.getJSON('/account_info_rest/user_info_total/8', function (data) {
        chart_8_total = areaChart('total_cn', 'BTC Total CN', data.map(function(a){return [a[0], a[1]]}));
    });
   

    //ORDER

    var offset = 0;

    all_order_rate_chart = $('#all_order_rate').highcharts({
        title: {text: 'Trade Volume', style:{"fontSize": "16px"}},
        chart:{animation: true, spacingBottom: 0,  alignTicks: false},
        tooltip: {valueDecimals: 3, positioner: function (w, h, p) {
            return { x: p.plotX - w/2, y: p.plotY < all_order_rate_chart.plotTop ? all_order_rate_chart.plotTop-h/2 : p.plotY};
        }},
        credits: {enabled: false},
        scrollbar: {enabled: false},
        navigator: {enabled: false, xAxis:{labels:{enabled:false}}},
        rangeSelector: {enabled: false},
        legend: {enabled: false},
        xAxis: {type: 'datetime', minRange: 3600000, min: new Date().getTime() - 24*3600000 + offset, max: new Date().getTime() + 60000 + offset},
        yAxis: [
            {labels: {enabled: true}, title:{text: null}, opposite: true, endOnTick:false, max: 500},
            {labels: {enabled: true}, title:{text: null}, opposite: false, endOnTick:false, max: 5000}
        ],
        series: [{
            type : 'column',
            name: 'USD Trade Volume',
            borderWidth:0,
            //stacking: 'normal',
            color: {
                linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                stops: [
                    [0, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0.2).get('rgba')],
                    [1, Highcharts.getOptions().colors[0]]
                ]
            }
        }, {
            type : 'column',
            name: 'CNY Trade Volume',
            borderWidth:0,
            //stacking: 'normal',
            yAxis:1,
            color: {
                linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                stops: [
                    [0, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0.2).get('rgba')],
                    [1, Highcharts.getOptions().colors[0]]
                ]
            }
        }]

    }).highcharts();

    setInterval(function () {all_order_rate_chart.xAxis[0].setExtremes(new Date().getTime() - 24*3600000 + offset, new Date().getTime() + 60000 + offset);}, 60000);

    Wicket.Event.subscribe("/websocket/closed", function(){
        $('#charts').css('-webkit-filter', 'grayscale(1)');

        $('#reconnect').show();

        var i = 42;
        setInterval(function(){
            $('#reconnect').text(Math.abs(i--));

            if (i == 1){
                location.reload();
            }
        }, 100);
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
    hour(chart_7_ltc_equity);
    hour(chart_7_btc_equity);
    hour(chart_7_btc_price);
    hour(chart_7_ltc_price);
    hour(chart_7_ltc_spot);
    hour(chart_7_btc_spot);
    hour(chart_7_usd_spot);
    hour(chart_7_total);
    hour(chart_8_ltc_spot);
    hour(chart_8_btc_spot);
    hour(chart_8_cny_spot);
    hour(chart_8_total);
}

function dayCharts(){
    day(chart_7_ltc_equity);
    day(chart_7_btc_equity);
    day(chart_7_btc_price);
    day(chart_7_ltc_price);
    day(chart_7_ltc_spot);
    day(chart_7_btc_spot);
    day(chart_7_usd_spot);
    day(chart_7_total);
    day(chart_8_ltc_spot);
    day(chart_8_btc_spot);
    day(chart_8_cny_spot);
    day(chart_8_total);
}

function weekCharts(){
    week(chart_7_ltc_equity);
    week(chart_7_btc_equity);
    week(chart_7_btc_price);
    week(chart_7_ltc_price);
    week(chart_7_ltc_spot);
    week(chart_7_btc_spot);
    week(chart_7_usd_spot);
    week(chart_7_total);
    week(chart_8_ltc_spot);
    week(chart_8_btc_spot);
    week(chart_8_cny_spot);
    week(chart_8_total);
}

function allCharts(){
    all(chart_7_ltc_equity);
    all(chart_7_btc_equity);
    all(chart_7_btc_price);
    all(chart_7_ltc_price);
    all(chart_7_ltc_spot);
    all(chart_7_btc_spot);
    all(chart_7_usd_spot);
    all(chart_7_total);
    all(chart_8_ltc_spot);
    all(chart_8_btc_spot);
    all(chart_8_cny_spot);
    all(chart_8_total);
}

