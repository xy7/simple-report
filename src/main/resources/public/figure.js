function esencePic(){
	var time = new Date();
	$.get("/getEsenceData", {start: $("#start").val(), end: $("#end").val()}, function(json){
		$('#containerEsence').highcharts({
			chart: {zoomType: 'x'},
	        title: {
	            text: 'esence',
	            x: -20 //center
	        },
	        subtitle: {
	            text: 'from mindware',
	            x: -20
	        },
	        xAxis: {
	            categories: json.x,
	            labels: {
	            	formatter:function(){return this.value.substring(11, 19);}
	            },
	            tickInterval:2
	            
	        },
	        yAxis: {
	            title: {
	                text: 'level'
	            },
	            plotLines: [{
	                value: 0,
	                width: 1,
	                color: '#808080'
	            }]
	        },
	        tooltip: {
	            valueSuffix: ''
	        },
	        legend: {
	            layout: 'vertical',
	            align: 'right',
	            verticalAlign: 'middle',
	            borderWidth: 0
	        },
	        series: [{
	            name: 'attention',
	            data: json.attention
	        }, {
	            name: 'meditation',
	            data: json.meditation
	        }]
	    });
	}, "json");
};

function rawEegPic(){
	var time = new Date();
	$.get("/getRawEegData", {date: $("#date").val()}, function(json){
		//var json = JSON.parse(res);
		var x = json.x;
		var y = json.y;
		$("#myDiv").html(json.fresh_time);
		$('#containerRawEeg').highcharts({
			chart: {zoomType: 'x'},
	        title: {
	            text: 'raw eeg power',
	            x: -20 //center
	        },
	        subtitle: {
	            text: 'from: mindware',
	            x: -20
	        },
	        xAxis: {
	            categories: x,
	            labels: {
	            	formatter:function(){return this.value.substring(11, 19);}
	            },
	            tickInterval:500
	            
	        },
	        yAxis: {
	            title: {
	                text: 'level'
	            },
	            plotLines: [{
	                value: 0,
	                width: 1,
	                color: '#808080'
	            }]
	        },
	        tooltip: {
	            valueSuffix: ''
	        },
	        legend: {
	            layout: 'vertical',
	            align: 'right',
	            verticalAlign: 'middle',
	            borderWidth: 0
	        },
	        series: [{
	            name: 'raw_eeg',
	            data: y
	        }]
	    });
	}, "json");
};

var lastNewDate;
function getNewData(){
	$.get("/getData"
			, {date:"2016-05-13"}
			, function(json){
				
			}
			, "json")
}

function dymPic() {                                                                     
    $(document).ready(function() {                                                  
        Highcharts.setOptions({                                                     
            global: {                                                               
                useUTC: true                                                       
            }                                                                       
        });                                                                         
        
        var lastTime;
        var chart;                                                                  
        $('#container2').highcharts({                                                
            chart: {                                                                
                type: 'spline',                                                     
                animation: Highcharts.svg, // don't animate in old IE               
                marginRight: 10,                                                    
                events: {                                                           
                    load: function() {                                              
                                                                                    
                        // set up the updating of the chart each second             
                        var series = this.series[0];                                
                        setInterval(function() {  
                        	
                        	$.get("/getNewData"
                        			, {date: lastTime}
                        			, function(json){
                        				lastTime = json.fresh_time;
                        				var x = (new Date()).getTime(),       
                                        y = Math.random();
                        				series.addPoint([x, y], true, true);  
                        			}
                        			, "json")
                          
                        }, 1000);                                                   
                    }                                                               
                }                                                                   
            },                                                                      
            title: {                                                                
                text: 'Live random data'                                            
            },                                                                      
            xAxis: {                                                                
                type: 'datetime',                                                   
                tickPixelInterval: 150
            },                                                                      
            yAxis: {                                                                
                title: {                                                            
                    text: 'Value'                                                   
                },                                                                  
                plotLines: [{                                                       
                    value: 0,                                                       
                    width: 1,                                                       
                    color: '#808080'                                                
                }]                                                                
            },                                                                      
            tooltip: {                                                              
                formatter: function() {                                             
                        return '<b>'+ this.series.name +'</b><br/>'+                
                        Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x) +'<br/>'+
                        Highcharts.numberFormat(this.y, 2);                         
                }                                                                   
            },                                                                      
            legend: {                                                               
                enabled: false                                                      
            },                                                                      
            exporting: {                                                            
                enabled: false                                                      
            },                                                                      
            series: [{                                                              
                name: 'Random data',                                                
                data: (function() {                                                 
                    // generate an array of random data                             
                    var data = [],                                                  
                        time = (new Date()).getTime(),                              
                        i;                                                          
                                                                                    
                    for (i = -19; i <= 0; i++) {                                    
                        data.push({                                                 
                            x: time + i * 1000,                                     
                            y: Math.random()                                        
                        });                                                         
                    }                                                               
                    return data;                                                    
                })()                                                                
            }]                                                                      
        });                                                                         
    });                                                                             
                                                                                    
};