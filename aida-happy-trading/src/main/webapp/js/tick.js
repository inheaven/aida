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
        chart:{animation: true, spacingBottom: 0, alignTicks:true},
        credits: {enabled: false},
        //scrollbar: {enabled: true},
        navigator: {enabled: true, adaptToUpdatedData: true,  xAxis: {type:'linear', labels:{enabled:false}}, height:20,
        series:{lineWidth:0}},
        rangeSelector: {enabled: true, selected : 0, inputEnabled: false,
            buttons: [{type: 'all', count: 1, text: '0'},
                {type: 'minute', count: 1, text: '1'},
                {type: 'minute', count: 3, text: '3'},
                {type: 'minute', count: 5, text: '5'},
                                {type: 'minute', count: 10, text: '10'},
                        {type: 'minute', count: 15, text: '15'},
                {type: 'minute', count: 30, text: '30'}
            ]},
        legend: {enabled: false},
        xAxis: {type:'linear', ordinal:true},
        yAxis: [
            {top: '0%', height: '39%', lineWidth: 0, offset:50, opposite: true},
            {top: '0%', height: '39%', lineWidth: 0, offset:50, opposite: false},
            {top: '39%', height: '61%', lineWidth: 0, offset:50, opposite:false},
            {top: '39%', height: '61%', lineWidth: 0, offset:50, opposite: true}
        ],
        plotOptions: {scatter: {
            lineWidth: 0,
            marker: {enabled: true, radius: 2, lineWidth: 0, symbol: 'circle'},
            states: {hover: {lineWidthPlus: 0}},
            turboThreshold: 10000,
            dataGrouping: {enabled: false,  groupPixelWidth: 1},

            enableMouseTracking: false
        },
            spline: {
                turboThreshold: false,
                //dataGrouping: {enabled: false,  groupPixelWidth: 1},
                enableMouseTracking: false,
                lineWidth:1
            },
            areaspline: {
                turboThreshold: false,
                dataGrouping: {enabled: false,  groupPixelWidth: 1},
                enableMouseTracking: false,
                lineWidth:1,                
            }},
        series: [{
            type:'scatter',
            name: 'ORDER',
            color: '#00FF00',
            zIndex:2
        }, {
            type:'scatter',
            name: 'DELTA',
            marker:{radius:1},
            //color: '#FFFFFF'
            yAxis:1,
            zIndex:1,
            enableMouseTracking: false
        },{
            type:'areaspline',
            name: 'BALANCE',
            color: Highcharts.getOptions().colors[0],
            yAxis:2,
             fillColor: {
                                 linearGradient: {x1: 0, y1: 0, x2: 0, y2: 1},
                                                     stops: [
                                                                             [0, Highcharts.getOptions().colors[0]],
                                                                                                     [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
                                                                                                                         ]
                                                                                                                                         }
        },{
            type:'line',
            name: 'NET',
            color: Highcharts.getOptions().colors[0],
            yAxis:3
            
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
