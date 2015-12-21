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
        navigator: {enabled: true, adaptToUpdatedData: true,  xAxis: {type:'linear', ordinal: false, labels:{format:'{value}'}}},
        rangeSelector: {enabled: true, selected : 0, inputEnabled: false,
            buttons: [{type: 'all', count: 1, text: '0'},
                {type: 'millisecond', count: 1000, text: '1000'},
                {type: 'millisecond', count: 2000, text: '2000'},
                {type: 'millisecond', count: 3000, text: '3000'},
                {type: 'millisecond', count: 5000, text: '5000'},
                {type: 'millisecond', count: 10000, text: '10000'}
            ]},
        legend: {enabled: false},
        xAxis: {type:'linear', ordinal: false, labels:{format:'{value}'}},
        yAxis: [
            {height: '100%', lineWidth: 0, offset: 50},
            {top: '85%', height: '0%', lineWidth: 0, offset:50}
        ],
        plotOptions: {scatter: {
            lineWidth: 0,
            marker: {enabled: true, radius: 1, lineWidth: 0, symbol: 'circle'},
            states: {hover: {lineWidthPlus: 0}},
            turboThreshold: 3000,
            dataGrouping: {enabled: true},
            
            groupPixelWidth: 1,
            enableMouseTracking: false
        }},
        series: [{
            type:'scatter',
            name: 'BID',
            color: '#00FF00'
,
            dashStyle: 'Dot'
        }, {
            type:'scatter',
            name: 'ASK',
            color: '#FF0000'
,
            dashStyle: 'Dot'
        },{
            type:'scatter',
            name: 'BUY',
            color: '#00FF00',
            marker: {enabled: true, radius: 5, lineWidth: 0, symbol: 'triangle'},
            //dataGrouping: {enabled: true, approximation : 'average'},
            lineWidth: 0
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
