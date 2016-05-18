function esencePic(){
	var time = new Date();
	$.get("/getEsenceData", {start: $("#start").val(), end: $("#end").val()}, function(json){
		$('#containerEsence').highcharts({
			chart: {zoomType: 'x'},
			credits: {enabled: false},
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



function setConnected(connected) {
    document.getElementById('connect').disabled = connected;
    document.getElementById('disconnect').disabled = !connected;
    document.getElementById('conversationDiv').style.visibility = connected ? 'visible' : 'hidden';
    document.getElementById('response').innerHTML = '';
}

var stompClient = null;
var seriesGlobal = null;
function connect() {
	console.log("connect");
    var socket = new SockJS('/realDataEndPoint');
    
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        setConnected(true);
        console.log('Connected: ' + frame);
        stompClient.subscribe('/realDataResp/rawEeg', function(res){
        	var json = JSON.parse(res.body);
    		var x = json.longTime;
    		var y = json.raw;
    		seriesGlobal.addPoint([x, y], true, true);
    		console.log(seriesGlobal.data);
        });
    });
}

function rawEegDymPic(){
	console.log("rawEegDymPic");
	
	$(document).ready(function() {
		Highcharts.setOptions({                                                     
		        global: {                                                               
		            useUTC: false                                                       
		        }                                                                       
		    });
		var chart;
	    $('#containerRawEeg').highcharts({                                                
	        chart: {                                                                
	            type: 'spline',                                                     
	            animation: Highcharts.svg, // don't animate in old IE               
	            marginRight: 10,                                                    
	            events: {                                                           
	                load: function() { 
	                	seriesGlobal = this.series[0];                                                                                                           
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
	                var data = [];                                                  
	                var time = (new Date()).getTime();
	                var i;                                                          
	                                                                                
	                for (i = -19; i <= -1; i++) {                                    
	                    data.push({                                                 
	                        x: time + i * 1000,                                     
	                        y: 0                                        
	                    });                                                         
	                }                                                              
	                return data;                                                    
	            })()                                                                
	        }]                                                                      
	    });                                                                         
	});
}

function sleep(milliseconds) {
	  var start = new Date().getTime();
	  for (var i = 0; i < 1e10; i++) {
	    if ((new Date().getTime() - start) > milliseconds){
	      break;
	    }
	  }
	}

function disconnect() {
	console.log("disconnect");
    if (stompClient != null) {
        stompClient.disconnect();
    }
    setConnected(false);
    console.log("Disconnected");
}

function sendName() {
    var name = document.getElementById('name').value;
    stompClient.send("/realDataReq/rawEeg", {}, JSON.stringify({ 'name': name }));
}

function showGreeting(message) {
    var response = document.getElementById('response');
    var p = document.createElement('p');
    p.style.wordWrap = 'break-word';
    p.appendChild(document.createTextNode(message));
    response.appendChild(p);
}