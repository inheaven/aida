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
        chart:{animation: true, spacingBottom: 0,  alignTicks: false},
        credits: {enabled: false},
        //scrollbar: {enabled: true},
        navigator: {enabled: true, adaptToUpdatedData: false},
        rangeSelector: {enabled: true, selected : 0,
            buttons: [{type: 'all', count: 1, text: '0'},
                {type: 'hour', count: 1, text: '1'},
                {type: 'hour', count: 2, text: '2'},
                {type: 'hour', count: 3, text: '3'},
                {type: 'hour', count: 4, text: '4'}
            ]},
        legend: {enabled: false},
        xAxis: {type: 'datetime'},
        yAxis: [
            {height: '80%', lineWidth: 2},
            {top: '85%', height: '15%', offset: 0,lineWidth: 2}
        ],
        plotOptions: {line: {
            lineWidth: 0,
            marker: {enabled: true, radius: 1, lineWidth: 0, symbol: 'circle'},
            states: {hover: {lineWidthPlus: 0}},
            turboThreshold: 0
        }},
        series: [{
            name: 'BID',
            color: '#00FF00'
        }, {
            name: 'ASK',
            color: '#FF0000'
        },{
            name: 'BUY',
            color: '#00FF00',
            marker: {enabled: true, radius: 7, lineWidth: 0, symbol: 'triangle'}
        },{
            name: 'SELL',
            color: '#FF0000',
            marker: {enabled: true, radius: 7, lineWidth: 0, symbol: 'triangle-down'}
        }, {
            type:'areaspline',
            name: 'VOLUME',
            yAxis:1,
            stacking: 'normal',
            borderWidth:0,
            dataGrouping: {approximation : 'sum'}
        }]
    }).highcharts();
});
