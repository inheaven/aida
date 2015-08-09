var btc_spread_chart;
var ltc_spread_chart;

var btc_spot_this_delta_chart;
var ltc_spot_this_delta_chart;

var btc_this_next_delta_chart;
var ltc_this_next_delta_chart;

$(function(){
    Highcharts.setOptions({
        global: {
            useUTC: true,
            timezoneOffset: 60
        },
        lang:{
            rangeSelectorZoom: ''
        }
    });

    $.getJSON('/arbitrage_rest/spreads/BTC', function (data) {
        btc_spread_chart = $('#btc_spread').highcharts('StockChart', {
            title: {text: 'BTC Spread'},
            chart: {animation: false, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            scrollbar: {enabled: false},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'line', threshold: null, name: 'BTC Spot Bid',
                data: data.filter(function(a){return a[3] == -1}).map(function(a){return [a[0], a[1]]})

            },{
                type: 'line', threshold: null, name: 'BTC Spot Ask',
                data: data.filter(function(a){return a[3] == -1}).map(function(a){return [a[0], a[2]]})

            },{
                type: 'line', threshold: null, name: 'BTC This Week Bid',
                data: data.filter(function(a){return a[3] == 0}).map(function(a){return [a[0], a[1]]})

            },{
                type: 'line', threshold: null, name: 'BTC This Week Ask',
                data: data.filter(function(a){return a[3] == 0}).map(function(a){return [a[0], a[2]]})

            },{
                type: 'line', threshold: null, name: 'BTC Next Week Bid',
                data: data.filter(function(a){return a[3] == 1}).map(function(a){return [a[0], a[1]]})

            },{
                type: 'line', threshold: null, name: 'BTC Next Week Ask',
                data: data.filter(function(a){return a[3] == 1}).map(function(a){return [a[0], a[2]]})

            },{
                type: 'line', threshold: null, name: 'BTC Quarter Bid',
                data: data.filter(function(a){return a[3] == 2}).map(function(a){return [a[0], a[1]]})

            },{
                type: 'line', threshold: null, name: 'BTC Quarter Ask',
                data: data.filter(function(a){return a[3] == 2}).map(function(a){return [a[0], a[2]]})
            }],
            rangeSelector: {
                enabled: true,
                buttons: [
                    {type: 'hour', count: 1, text: 'H'},
                    {type: 'day', count: 1, text: 'D'},
                    {type: 'week', count: 1, text: 'W'},
                    {type: 'all', count: 1, text: 'A'}
                ],
                inputEnabled: false
            }
        }).highcharts();
    });

    $.getJSON('/arbitrage_rest/spreads/LTC', function (data) {
        ltc_spread_chart = $('#ltc_spread').highcharts('StockChart', {
            title: {text: 'LTC Spread'},
            chart: {animation: false, spacingBottom: 0},
            tooltip: {valueDecimals: 3},
            credits: {enabled: false},
            navigator: {height: 0, xAxis:{labels:{enabled:false}}},
            scrollbar: {enabled: false},
            xAxis: {type: 'datetime'},
            series: [{
                type: 'line', threshold: null, name: 'LTC Spot Bid',
                data: data.filter(function(a){return a[3] == -1}).map(function(a){return [a[0], a[1]]})

            },{
                type: 'line', threshold: null, name: 'LTC Spot Ask',
                data: data.filter(function(a){return a[3] == -1}).map(function(a){return [a[0], a[2]]})

            },{
                type: 'line', threshold: null, name: 'LTC This Week Bid',
                data: data.filter(function(a){return a[3] == 0}).map(function(a){return [a[0], a[1]]})

            },{
                type: 'line', threshold: null, name: 'LTC This Week Ask',
                data: data.filter(function(a){return a[3] == 0}).map(function(a){return [a[0], a[2]]})

            },{
                type: 'line', threshold: null, name: 'LTC Next Week Bid',
                data: data.filter(function(a){return a[3] == 1}).map(function(a){return [a[0], a[1]]})

            },{
                type: 'line', threshold: null, name: 'LTC Next Week Ask',
                data: data.filter(function(a){return a[3] == 1}).map(function(a){return [a[0], a[2]]})

            },{
                type: 'line', threshold: null, name: 'LTC Quarter Bid',
                data: data.filter(function(a){return a[3] == 2}).map(function(a){return [a[0], a[1]]})

            },{
                type: 'line', threshold: null, name: 'LTC Quarter Ask',
                data: data.filter(function(a){return a[3] == 2}).map(function(a){return [a[0], a[2]]})
            }],
            rangeSelector: {
                enabled: true,
                buttons: [
                    {type: 'hour', count: 1, text: 'H'},
                    {type: 'day', count: 1, text: 'D'},
                    {type: 'week', count: 1, text: 'W'},
                    {type: 'all', count: 1, text: 'A'}
                ],
                inputEnabled: false
            }
        }).highcharts();
    });

});