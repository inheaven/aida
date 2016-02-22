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
        navigator: {enabled: true, adaptToUpdatedData: true,  xAxis: {type:'datetime', labels:{enabled:false}}, height:20,
        series:{lineWidth:0}},
        rangeSelector: {enabled: true, selected : 0, inputEnabled: false,
            buttons: [{type: 'all', count: 1, text: '0'},
                {type: 'minute', count: 1, text: '1'},
                {type: 'minute', count: 3, text: '3'},
                {type: 'minute', count: 5, text: '5'},
                {type: 'minute', count: 15, text: '15'},
                {type: 'minute', count: 60, text: '60'}
            ]},
        legend: {enabled: false},
        xAxis: {type:'datetime'},
        yAxis: [
            {top: '0%', height: '80%', lineWidth: 0, offset: 50, opposite: false},
            {top: '0%', height: '80%', lineWidth: 0, offset:50},
            {top: '80%', height: '20%', lineWidth: 0, offset:50}
        ],
        plotOptions: {scatter: {
            lineWidth: 0,
            marker: {enabled: true, radius: 2, lineWidth: 0, symbol: 'square'},
            states: {hover: {lineWidthPlus: 0}},
            turboThreshold: 10000,
            dataGrouping: {enabled: true,  groupPixelWidth: 1},

            enableMouseTracking: false
        },
            line: {
                turboThreshold: 10000,
                dataGrouping: {enabled: true,  groupPixelWidth: 1},
                enableMouseTracking: false,
                lineWidth:1
            },
            area: {
                turboThreshold: 10000,
                dataGrouping: {enabled: true,  groupPixelWidth: 1},
                enableMouseTracking: false,
                lineWidth:1
            }},
        series: [{
            type:'scatter',
            name: 'ORDER',
            color: '#00FF00'
        }, {
            type:'line',
            name: 'DELTA',
            color: '#FFFFFF'
,
            yAxis:1
        },{
            type:'area',
            name: 'BALANCE',
            color: '#FFFFFF',
            yAxis:2
        },{
            type:'scatter',
            name: 'SELL',
            color: '#FF0000',
            marker: {enabled: true, radius: 5, lineWidth: 0, symbol: 'triangle-down'},
            //dataGrouping: {enabled: true, approximation : 'average'},
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
