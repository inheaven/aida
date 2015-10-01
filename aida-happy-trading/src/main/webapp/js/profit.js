function areaChart(id, title, data0, data1, data2){
    return $('#'+id).highcharts('StockChart', {
        title: {text: title},
        chart: {animation: false, spacingBottom: 0},
        tooltip: {valueDecimals: 3},
        credits: {enabled: false},
        navigator: {xAxis: {labels: {enabled: false}}},
        scrollbar: {enabled: true},
        xAxis: {type: 'datetime'},
        yAxis: [{}, {}, {}],
        series: [{
            type: 'line', threshold: null,
            data: data0,
            name: 'SPOT',
            fillColor: {
                linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                stops: [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
            }
        }, {
            type: 'line', threshold: null,
            data: data1,
            name: 'SPOT/BTC',
            yAxis:1,
            fillColor: {
                linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                stops: [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
            }
        }, {
            type: 'line', threshold: null,
            data: data2,
            name: 'BTC',
            yAxis:2,
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

$.getJSON('/account_info_rest/user_info_total/7', function (data) {
    areaChart('usd_profit', 'USD Profit',
        data.map(function(a){return [a[0], (a[1])]}),
        data.map(function(a){return [a[0], (a[1]/a[3])]}),
        data.map(function(a){return [a[0], (a[3])]}));
});

$.getJSON('/account_info_rest/user_info_total/8', function (data) {
    areaChart('cny_profit', 'CNY Profit',
        data.map(function(a){return [a[0], (a[1])]}),
        data.map(function(a){return [a[0], (a[1]/a[3])]}),
        data.map(function(a){return [a[0], (a[3])]}));
});
