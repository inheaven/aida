var tick_chart;

$(function(){
    Highcharts.setOptions({
        global: {
            useUTC: true,
            timezoneOffset: -360
        },
        lang:{
            rangeSelectorZoom: ''
        }
    });

    tick_chart = $('#tick').highcharts('StockChart', {
        title: {text: 'Tick Chart', style:{"fontSize": "16px"}},
        chart:{animation: false, spacingBottom: 0, alignTicks:false},
        credits: {enabled: false},
        //scrollbar: {enabled: true},
        navigator: {enabled: true, adaptToUpdatedData: true},
        rangeSelector: {enabled: true, selected : 0,
            buttons: [{type: 'all', count: 1, text: '0'},
                {type: 'minute', count: 1, text: '1m'},
                {type: 'minute', count: 2, text: '2m'},
                {type: 'minute', count: 3, text: '3m'},
                {type: 'minute', count: 5, text: '5m'},
                {type: 'minute', count: 10, text: '10m'}
            ]},
        legend: {enabled: false},
        xAxis: {type:'datetime', minRange: 0, minTickInterval:0, startOnTick: false, endOnTick: false, ordinal: false, pointRange: 1},
        yAxis: [
            {height: '80%', lineWidth: 0, offset: 50},
            {top: '85%', height: '15%', lineWidth: 0, offset:50}
        ],
        plotOptions: {scatter: {
            lineWidth: 0,
            marker: {enabled: true, radius: 1, lineWidth: 0, symbol: 'circle'},
            states: {hover: {lineWidthPlus: 0}},
            turboThreshold: 1000,
            dataGrouping: {enabled: true, smoothed: true},
            enableMouseTracking: false
        }},
        series: [{
            type:'scatter',
            name: 'BID',
            color: '#00FF00'
        }, {
            type:'scatter',
            name: 'ASK',
            color: '#FF0000'
        },{
            type:'scatter',
            name: 'BUY',
            color: '#00FF00',
            marker: {enabled: true, radius: 7, lineWidth: 0, symbol: 'triangle'},
            dataGrouping: {enabled: true, approximation : 'average'},
            lineWidth: 0
        },{
            type:'scatter',
            name: 'SELL',
            color: '#FF0000',
            marker: {enabled: true, radius: 7, lineWidth: 0, symbol: 'triangle-down'},
            dataGrouping: {enabled: true, approximation : 'average'},
            lineWidth: 0
        }, {
            type:'scatter',
            name: 'VOLUME BID',
            color: '#00FF00',
            yAxis:1,
            stacking: 'normal',
            pointWidth : 2,
            borderWidth:0,
            dataGrouping: {enabled: true, approximation : 'sum'}
        }, {
            type:'scatter',
            name: 'VOLUME ASK',
            color: '#FF0000',
            yAxis:1,
            stacking: 'normal',
            pointWidth : 2,
            borderWidth:0,
            dataGrouping: {enabled: true, approximation : 'sum'}
        }]
    }).highcharts();
});
