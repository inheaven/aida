var chart_usd;
var chart_cny;

Highcharts.setOptions({
    global: {
        useUTC: true,
        timezoneOffset: -360
    },
    lang:{
        rangeSelectorZoom: ''
    }
});

function areaChart(id, title, data0, data1, data2){
    return $('#'+id).highcharts('StockChart', {
        title: {text: title},
        chart: {animation: false, spacingBottom: 0},
        tooltip: {valueDecimals: 3},
        credits: {enabled: false},
        navigator: {xAxis: {labels: {enabled: false}}},
        scrollbar: {enabled: true},
        xAxis: {type: 'datetime'},
        yAxis: [{labels: {
            formatter: function () {
                return this.value + '%';
            }
        }}, {}, {}],
        series: [{
            type: 'spline', threshold: null,
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
            type: 'spline', threshold: null,
            data: data1,
            name: 'SPOT/BTC',
            yAxis:0,
            fillColor: {
                linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                stops: [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
            }
        }, {
            type: 'spline', threshold: null,
            data: data2,
            name: 'BTC',
            yAxis:0,
            fillColor: {
                linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                stops: [
                    [0, Highcharts.getOptions().colors[0]],
                    [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                ]
            }
        }],
        rangeSelector: {
            enabled: true,
            buttons: [
                {type: 'hour', count: 1, text: 'Hour'},
                {type: 'day', count: 1, text: 'Day'},
                {type: 'week', count: 1, text: 'Week'},
                {type: 'all', count: 1, text: 'All'}
            ],
            selected : 1,
            inputEnabled: false
        }
    }).highcharts();
}

$.getJSON('/account_info_rest/user_info_total/7', function (data) {
    var last;
    var day = getParameterByName('d');

    if (day != null){
        var i = data.length - 1440*day;
        last = data[i > 0 ? i : data.length - 1440];
    }else{
        last = data[data.length - 1440];
    }

    chart_usd = areaChart('usd_profit', 'USD Profit',
        data.map(function(a){return [a[0], 100*(a[1] - last[1])/(last[1])]}),
        data.map(function(a){return [a[0], 100*((a[1]/a[3]) - (last[1]/last[3]))/(last[1]/last[3])]}),
        data.map(function(a){return [a[0], 100*(a[3] - last[3])/(last[3])]}));
});

$.getJSON('/account_info_rest/user_info_total/8', function (data) {
    var last;
    var day = getParameterByName('d');

    if (day != null){
        var i = data.length - 1440*day;
        last = data[i > 0 ? i : data.length - 1440];
    }else{
        last = data[data.length - 1440];
    }

    chart_cny = areaChart('cny_profit', 'CNY Profit',
        data.map(function(a){return [a[0], 100*(a[1] - last[1])/(last[1])]}),
        data.map(function(a){return [a[0], 100*((a[1]/a[3]) - (last[1]/last[3]))/(last[1]/last[3])]}),
        data.map(function(a){return [a[0], 100*(a[3] - last[3])/(last[3])]}));
});

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
        results = regex.exec(location.search);
    return results === null ? null : decodeURIComponent(results[1].replace(/\+/g, " "));
}
