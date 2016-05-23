//历史数据绘图
function esensePic() {
	var time = new Date();
	$.get("/getEsenceData", {
		start : $("#start").val(),
		end : $("#end").val()
	}, function(json) {
		$('#containerEsense').highcharts({
			chart : {
				zoomType : 'x'
			},
			credits : {
				enabled : false
			},
			title : {
				text : 'esence',
				x : -20
			// center
			},
			subtitle : {
				text : 'from mindware',
				x : -20
			},
			xAxis : {
				categories : json.x,
				labels : {
					formatter : function() {
						return this.value.substring(11, 19);
					}
				},
				tickInterval : 2
			},
			yAxis : {
				title : {
					text : 'level'
				},
				plotLines : [ {
					value : 0,
					width : 1,
					color : '#808080'
				} ]
			},
			tooltip : {
				valueSuffix : ''
			},
			legend : {
				layout : 'vertical',
				align : 'right',
				verticalAlign : 'middle',
				borderWidth : 0
			},
			series : [ {
				name : 'attention',
				data : json.attention
			}, {
				name : 'meditation',
				data : json.meditation
			} ]
		});
	}, "json");
};

function rawEegPic() {
	var time = new Date();
	$.get("/getRawEegData", {
		date : $("#date").val()
	}, function(json) {
		// var json = JSON.parse(res);
		var x = json.x;
		var y = json.y;
		$("#myDiv").html(json.fresh_time);
		$('#containerRawEeg').highcharts({
			chart : {
				zoomType : 'x'
			},
			credits : {
				enabled : false
			},
			title : {
				text : 'raw eeg power',
				x : -20
			// center
			},
			subtitle : {
				text : 'from: mindware',
				x : -20
			},
			xAxis : {
				categories : x,
				labels : {
					formatter : function() {
						return this.value.substring(11, 19);
					}
				},
				tickInterval : 500

			},
			yAxis : {
				title : {
					text : 'level'
				},
				plotLines : [ {
					value : 0,
					width : 1,
					color : '#808080'
				} ]
			},
			tooltip : {
				valueSuffix : ''
			},
			legend : {
				layout : 'vertical',
				align : 'right',
				verticalAlign : 'middle',
				borderWidth : 0
			},
			series : [ {
				name : 'raw_eeg',
				data : y
			} ]
		});
	}, "json");
};

//实时绘图
function setConnected(connected) {
	document.getElementById('connect').disabled = connected;
	document.getElementById('disconnect').disabled = !connected;
	document.getElementById('reqData').disabled = !connected;
}

var stompClient = null;
var seriesRawEeg = null;
var seriesEsenseAtt = null;
var seriesEsenseMed = null;
function connect() {
	console.log("connect");
	var socket = new SockJS('/realDataEndPoint');

	stompClient = Stomp.over(socket);
	stompClient.connect({}, function(frame) {
		setConnected(true);
		console.log('Connected: ' + frame);
		
		stompClient.subscribe('/realDataResp/rawEeg', function(res) {
			var json = JSON.parse(res.body);
			var x = json.longTime;
			var y = json.raw;
			seriesRawEeg.addPoint([ x, y ], true, true);
			console.log(json);
		});
		reqData("rawEeg");
		
		stompClient.subscribe('/realDataResp/esense', function(res) {
			var json = JSON.parse(res.body);
			var x = json.longTime;
			var att = json.attention;
			var med = json.meditation;
			seriesEsenseAtt.addPoint([ x, att ], true, true);
			seriesEsenseMed.addPoint([ x, med ], true, true);
			console.log(json);
		});
		reqData("esense");
	});
}

function disconnect() {
	console.log("disconnect");
	if (stompClient != null) {
		stompClient.disconnect();
	}
	setConnected(false);
	console.log("Disconnected");
}

function reqData(type) {
	stompClient.send("/realDataReq/type", {}, type);
}

function rawEegDymPic() {
	console.log("rawEegDymPic");

	Highcharts.setOptions({
		global : {
			useUTC : false
		}
	});
	var chart;
	$('#containerRawEeg').highcharts({
		chart : {
			type : 'spline',
			animation : Highcharts.svg, // don't animate in old IE
			marginRight : 10,
			events : {
				load : function() {
					seriesRawEeg = this.series[0];
				}
			}
		},
		credits : {
			enabled : false
		},
		title : {
			text : 'raw eeg'
		},
		xAxis : {
			type : 'datetime',
			tickPixelInterval : 150
		},
		yAxis : {
			title : {
				text : 'Value'
			},
			plotLines : [ {
				value : 0,
				width : 1,
				color : '#808080'
			} ]
		},
		tooltip : {
			formatter : function() {
				return '<b>'
						+ this.series.name
						+ '</b><br/>'
						+ Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x)
						+ '<br/>'
						+ Highcharts.numberFormat(this.y, 2);
			}
		},
		legend : {
			enabled : false
		},
		exporting : {
			enabled : false
		},
		series : [ {
			name : 'rawEeg',
			data : zeroData()
		} ]
	});
}

function esenseDymPic() {
	console.log("esenseDymPic");

	Highcharts.setOptions({
		global : {
			useUTC : false
		}
	});
	var chart;
	$('#containerEsense').highcharts({
		chart : {
			type : 'spline',
			animation : Highcharts.svg, // don't animate in old IE
			marginRight : 10,
			events : {
				load : function() {
					seriesEsenseAtt = this.series[0];
					seriesEsenseMed = this.series[1];
				}
			}
		},
		credits : {
			enabled : false
		},
		title : {
			text : 'esense'
		},
		xAxis : {
			type : 'datetime',
			tickPixelInterval : 150
		},
		yAxis : {
			title : {
				text : 'Value'
			},
			plotLines : [ {
				value : 0,
				width : 1,
				color : '#808080'
			} ]
		},
		tooltip : {
			formatter : function() {
				return '<b>'
						+ this.series.name
						+ '</b><br/>'
						+ Highcharts.dateFormat('%Y-%m-%d %H:%M:%S', this.x)
						+ '<br/>'
						+ Highcharts.numberFormat(this.y, 2);
			}
		},
		legend : {
			enabled : false
		},
		exporting : {
			enabled : false
		},
		series : [ {
			name : 'attention',
			data : zeroData()
		}, {
			name : 'meditation',
			data : zeroData()
		} ]
	});
}

function zeroData() {
	// generate an array of
	// random data
	var data = [];
	var time = (new Date()).getTime();
	var i;

	for (i = -110; i <= -10; i++) {
		data.push({
					x : time + i * 1000,
					y : 0
				});
	}
	return data;
}