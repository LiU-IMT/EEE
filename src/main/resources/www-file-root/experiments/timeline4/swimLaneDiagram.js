/**
 * Utilities to create swimlane diagrams using D3.js
 * Code heavily inspired by bunkat’s blocks at http://bl.ocks.org/2338034 and http://bl.ocks.org/3127459 and http://bl.ocks.org/1962173
 */

var y1, y2, x0, x1, now, brush, lanes, items, main, mini, x1DateAxis, x1MonthAxis, itemRects; // Global variables needed by several functions

function initSwimlane (inLanes, inItems, targetCanvasSelector, miniTargetCanvasSelector, itemClickFunction) {

	//////////////////////////////////////////////////
    ///                  SHARED                    ///
	//////////////////////////////////////////////////
	
	 lanes = inLanes;
	 items = inItems;
	 now = new Date(); // TODO: gör detta justerbart via init (sedan via URL)?
	
	 console.log("Lanes: ", lanes," Items: ",items)

	 var ext = d3.extent(lanes, function(d) { return d.id; });
	 
	
	//////////////////////////////////////////////////
    ///                 MINI	                   ///
	//////////////////////////////////////////////////
	
	var minichart = d3.select(miniTargetCanvasSelector);
	var minichartWidth = $(minichart.node()).width();
	var minichartHeight = $(minichart.node()).height();
	 
	console.log("minichart: ", minichart.node()," minichartWidth: ", minichartWidth, " minichartHeight: ", minichartHeight," lanes.length: ",lanes.length)

	var minimargin = {top: 20, right: 15, bottom: 15, left: 60} , 
		miniHeight = lanes.length * 5 + 30;

	x0 = d3.time.scale()
		.domain([d3.time.sunday(d3.min(items, function(d) { return d.start; })),
				 d3.max(items, function(d) { return d.end; })])
		.range([0, width]);
	
	y2 = d3.scale.linear().domain([ext[0], ext[1] + 1]).range([0, miniHeight]);

	console.log("mini :::: x0, y2= ", x0, y2)
	var minichartSvg = minichart.append('svg:svg') // TODO: Doublecheck this
	//.attr('width', width + margin.right + margin.left)
	.attr('width', minichartWidth)
	//.attr('height', height + margin.top + margin.bottom)
	.attr('height', miniHeight)
	.attr('class', 'minichart');

	mini = minichartSvg.append('g')
		.attr('transform', 'translate(' + minimargin.left + ',' + minimargin.top + ')')
		.attr('width', minichartWidth)
		.attr('height', miniHeight)
		.attr('class', 'mini');

	// draw the lanes for the mini chart
	mini.append('g').selectAll('.laneLines')
		.data(lanes)
		.enter().append('line')
		.attr('x1', 0)
		.attr('y1', function(d) { return d3.round(y2(d.id)) + 0.5; })
		.attr('x2', width)
		.attr('y2', function(d) { return d3.round(y2(d.id)) + 0.5; })
		.attr('stroke', function(d) { return d.label === '' ? 'white' : 'lightgray' });

	mini.append('g').selectAll('.laneText')
		.data(lanes)
		.enter().append('text')
		.text(function(d) { return d.label; })
		.attr('x', -10)
		.attr('y', function(d) { return y2(d.id + .5); })
		.attr('dy', '0.5ex')
		.attr('text-anchor', 'end')
		.attr('class', 'laneText');

	// draw the x axis
	
	// x and x0 is for MINI
		
	var xDateAxis = d3.svg.axis()
		.scale(x0)
		.orient('bottom')
		.ticks(d3.time.mondays, (x0.domain()[1] -x0.domain()[0]) > 15552e6 ? 2 : 1)
		.tickFormat(d3.time.format('%d'))
		.tickSize(6, 0, 0);
	
	console.log("mini :::: mid ");	
	
	var xMonthAxis = d3.svg.axis()
	.scale(x0)
	.orient('top')
	.ticks(d3.time.months, 1)
	.tickFormat(d3.time.format('%b %Y'))
	.tickSize(15, 0, 0);

	console.log("mini :::: mid2" );// xDateAxis:"+ xDateAxis);	
	
	mini.append('g')
	.attr('transform', 'translate(0,' + miniHeight + ')')
	.attr('class', 'axis date')
	.call(xDateAxis);

	console.log("mini :::: mid3 ");	
	
	mini.append('g')
	.attr('transform', 'translate(0,0.5)')
	.attr('class', 'axis month')
	.call(xMonthAxis)
	.selectAll('text')
		.attr('dx', 5)
		.attr('dy', 12);

	// draw a line representing today's date
	mini.append('line')
		.attr('x1',x0(now) + 0.5)
		.attr('y1', 0)
		.attr('x2',x0(now) + 0.5)
		.attr('y2', miniHeight)
		.attr('class', 'todayLine');

	// draw the items

	mini.append('g').selectAll('miniItems')
		.data(getPaths(items))
		.enter().append('path')
		.attr('class', function(d) { return 'miniItem ' + d.class; })
		.attr('d', function(d) { return d.path; })
		.on('click', function(d, i) { console.log(d, i, this) });

	// invisible hit area to move around the selection window
	mini.append('rect')
		.attr('pointer-events', 'painted')
		.attr('width', width)
		.attr('height', miniHeight)
		.attr('visibility', 'hidden')
		.on('mouseup', moveBrush); /* */

	// draw the selection area
	var brushLeft = d3.time.monday(now); // TODO: make week start/end configurable
	var brushRight = d3.time.sunday.ceil(now);
	
	brush = d3.svg.brush()
		.x(x0)
		.extent([brushLeft, brushRight])
		.on("brush", function(e){
			//console.log("brushevent: ",e);
			displaySwimlane()
			})

	mini.append('g')
		.attr('class', 'x brush')
		.call(brush)
		.selectAll('rect')
			.attr('y', 1)
			.attr('height', miniHeight - 1);

	mini.selectAll('rect.background').remove();

	console.log("mini :::: end ", mini)
	
	
    //////////////////////////////////////////////////
    ///                 DETAILED                   ///
	//////////////////////////////////////////////////
    
	var mainHeight = height; // - 50;

	var chart = d3.select(targetCanvasSelector);
	var chartWidth = $(chart.node()).width();
	var chartHeight = $(chart.node()).height();
	console.log("chart: ", chart.node()," chartWidth: ", chartWidth, "chartHeight", chartHeight)
	
	// Main
	var margin = {top: 10, right: 15, bottom: 15, left: 60}
	  , width = chartWidth - margin.left - margin.right
	  , height = chartHeight - margin.top - margin.bottom;

	// Main
	x1 = d3.time.scale().range([0, chartWidth]);	
	y1 = d3.scale.linear().domain([ext[0], ext[1] + 1]).range([0, mainHeight]);
	
	var chartSvg = chart.append('svg:svg')
		.attr('width', width + margin.right + margin.left)
		.attr('height', height + margin.top + margin.bottom)
		.attr('class', 'chart');

	chartSvg.append('defs').append('clipPath') // TODO: Check if this should be duplicated for mini too
	.attr('id', 'clip') //...but in that case with other ID here
	.append('rect')
		.attr('width', width)
		.attr('height', mainHeight);

	main = chartSvg.append('g')
	.attr('transform', 'translate(' + margin.left + ',' + (miniHeight + 60) + ')')
	.attr('width', width)
	.attr('height', mainHeight)
	.attr('class', 'main');
		
	// draw the lanes for the main chart
	main.append('g').selectAll('.laneLines')
		.data(lanes)
		.enter().append('line')
		.attr('x1', 0)
		.attr('y1', function(d) { return d3.round(y1(d.id)) + 0.5; })
		.attr('x2', width)
		.attr('y2', function(d) { return d3.round(y1(d.id)) + 0.5; })
		.attr('stroke', function(d) { return d.label === '' ? 'white' : 'lightgray' });

	main.append('g').selectAll('.laneText')
		.data(lanes)
		.enter().append('text')
		.text(function(d) { return d.label; })
		.attr('x', -10)
		.attr('y', function(d) { return y1(d.id + .5); })
		.attr('dy', '0.5ex')
		.attr('text-anchor', 'end')
		.attr('class', 'laneText');
	
	
	// x1 is for MAIN
	
	x1DateAxis = d3.svg.axis()
		.scale(x1)
		.orient('bottom')
		.ticks(d3.time.days, 1)
		.tickFormat(d3.time.format('%a %d'))
		.tickSize(6, 0, 0);

	x1MonthAxis = d3.svg.axis()
		.scale(x1)
		.orient('top')
		.ticks(d3.time.mondays, 1)
		.tickFormat(d3.time.format('%b - Week %W'))
		.tickSize(15, 0, 0);

	main.append('g')
		.attr('transform', 'translate(0,' + mainHeight + ')')
		.attr('class', 'main axis date')
		.call(x1DateAxis);

	main.append('g')
		.attr('transform', 'translate(0,0.5)')
		.attr('class', 'main axis month')
		.call(x1MonthAxis)
		.selectAll('text')
			.attr('dx', 5)
			.attr('dy', 12);

	// draw a line representing today's date
	main.append('line')
		.attr('y1', 0)
		.attr('y2', mainHeight)
		.attr('class', 'main todayLine')
		.attr('clip-path', 'url(#clip)');

	// draw the items
	itemRects = main.append('g')
		.attr('clip-path', 'url(#clip)');

	displaySwimlane();
}	


function displaySwimlane () {

	var rects, labels
	  , minExtent = d3.time.day(brush.extent()[0])
	  , maxExtent = d3.time.day(brush.extent()[1])
	  , visItems = items.filter(function (d) { return d.start < maxExtent && d.end > minExtent});

	mini.select('.brush').call(brush.extent([minExtent, maxExtent]));		

	x1.domain([minExtent, maxExtent]);

	if ((maxExtent - minExtent) > 1468800000) {
		x1DateAxis.ticks(d3.time.mondays, 1).tickFormat(d3.time.format('%a %d'))
		x1MonthAxis.ticks(d3.time.mondays, 1).tickFormat(d3.time.format('%b - Week %W'))		
	}
	else if ((maxExtent - minExtent) > 172800000) {
		x1DateAxis.ticks(d3.time.days, 1).tickFormat(d3.time.format('%a %d'))
		x1MonthAxis.ticks(d3.time.mondays, 1).tickFormat(d3.time.format('%b - Week %W'))
	}
	else {
		x1DateAxis.ticks(d3.time.hours, 4).tickFormat(d3.time.format('%I %p'))
		x1MonthAxis.ticks(d3.time.days, 1).tickFormat(d3.time.format('%b %e'))
	}

	//x1Offset.range([0, x1(d3.time.day.ceil(now) - x1(d3.time.day.floor(now)))]);

	// shift the today line
	main.select('.main.todayLine')
		.attr('x1', x1(now) + 0.5)
		.attr('x2', x1(now) + 0.5);

	// update the axis
	main.select('.main.axis.date').call(x1DateAxis);
	main.select('.main.axis.month').call(x1MonthAxis)
		.selectAll('text')
			.attr('dx', 5)
			.attr('dy', 12);

	// upate the item rects
	rects = itemRects.selectAll('rect')
		.data(visItems, function (d) { return d.id; })
		.attr('x', function(d) { return x1(d.start); })
		.attr('width', function(d) { return x1(d.end) - x1(d.start); });

	rects.enter().append('rect')
		.attr('x', function(d) { return x1(d.start); })
		.attr('y', function(d) { return y1(d.lane) + .1 * y1(1) + 0.5; })
		.attr('width', function(d) { return x1(d.end) - x1(d.start); })
		.attr('height', function(d) { return .8 * y1(1); })
		.attr('class', function(d) { return 'mainItem ' + d.class; })
		.on('click', function(d, i) { 
			var scrollTarget = $('#'+d.id+'-target');
			console.log(d, i, scrollTarget, this);
			if (scrollTarget) {
				$(document.body).animate({
				    'scrollTop':   scrollTarget.offset().top - 50 // TODO: Change 50 to variable for header hight
				}, 300);	
				// Then make it flash for attention, see http://stackoverflow.com/questions/275931/how-do-you-make-an-element-flash-in-jquery
				scrollTarget.fadeOut(200).fadeIn(200).fadeOut(100).fadeIn(100).fadeOut(200).fadeIn(200);
				//scrollTarget.animateHighlight("#ddffdd", 1500);
			}			
		  });

	rects.exit().remove();

	// update the item labels
	labels = itemRects.selectAll('text')
		.data(visItems, function (d) { return d.id; })
		.attr('x', function(d) { return x1(Math.max(d.start, minExtent)) + 2; });
				
	labels.enter().append('text')
		.text(function (d) { return 'Item Id: ' + d.id; })
		.attr('x', function(d) { return x1(Math.max(d.start, minExtent)) + 2; })
		.attr('y', function(d) { return y1(d.lane) + .4 * y1(1) + 0.5; })
		.attr('text-anchor', 'start')
		.attr('class', 'itemLabel');

	labels.exit().remove();
}

function moveBrush () {
	var origin = d3.mouse(this)
	  , point =x0.invert(origin[0])
	  , halfExtent = (brush.extent()[1].getTime() - brush.extent()[0].getTime()) / 2
	  , start = new Date(point.getTime() - halfExtent)
	  , end = new Date(point.getTime() + halfExtent);

	brush.extent([start,end]);
	displaySwimlane();
}

// generates a single path for each item class in the mini display
// ugly - but draws mini 2x faster than append lines or line generator
// is there a better way to do a bunch of lines as a single path with d3?
function getPaths(items) {
	console.log("getPaths(items)", items)
	var paths = {}, d, offset = .5 * y2(1) + 0.5, result = [];
	for (var i = 0; i < items.length; i++) {
		d = items[i];
		if (!paths[d.class]) paths[d.class] = '';	
		paths[d.class] += ['M',x0(d.start),(y2(d.lane) + offset),'H',x0(d.end)].join(' ');
	}

	for (var className in paths) {
		result.push({class: className, path: paths[className]});
	}

	return result;
}
