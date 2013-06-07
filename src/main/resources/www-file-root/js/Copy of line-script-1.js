
// ISO-date-converter from http://webcloud.se/log/JavaScript-and-ISO-8601/
	Date.prototype.setISO8601 = function (string) {
		var regexp = "([0-9]{4})(-([0-9]{2})(-([0-9]{2})" +
		"(T([0-9]{2}):([0-9]{2})(:([0-9]{2})(\.([0-9]+))?)?" +
		"(Z|(([-+])([0-9]{2}):([0-9]{2})))?)?)?)?";
		var d = string.match(new RegExp(regexp));

		var offset = 0;
		var date = new Date(d[1], 0, 1);

		if (d[3]) { date.setMonth(d[3] - 1); }
		if (d[5]) { date.setDate(d[5]); }
		if (d[7]) { date.setHours(d[7]); }
		if (d[8]) { date.setMinutes(d[8]); }
		if (d[10]) { date.setSeconds(d[10]); }
		if (d[12]) { date.setMilliseconds(Number("0." + d[12]) * 1000); }
		if (d[14]) {
			offset = (Number(d[16]) * 60) + Number(d[17]);
			offset *= ((d[15] == '-') ? 1 : -1);
		}

		offset -= date.getTimezoneOffset();
		time = (Number(date) + (offset * 60 * 1000));
		this.setTime(Number(time));
	}

	// ISO-date-converter from https://developer.mozilla.org/en/JavaScript/Reference/Global_Objects/Date#Example.3a_ISO_8601_formatted_dates
	function ISODateString(d){
		 function pad(n){return n<10 ? '0'+n : n}
		 return d.getUTCFullYear()+'-'
		      + pad(d.getUTCMonth()+1)+'-'
		      + pad(d.getUTCDate())+'T'
		      + pad(d.getUTCHours())+':'
		      + pad(d.getUTCMinutes())+':'
		      + pad(d.getUTCSeconds())+'Z'
		    }
	
	function toX(datetime, mintime, maxtime, totalwidth) {
		// console.log("Hello toX: "+datetime);
		return ((datetime-mintime)/(maxtime-mintime)*totalwidth);
	}
	
 Raphael.fn.timeband =  Raphael.fn.timeband || function(band_title, totalPixelWidth, lineHeight, lineSpacing, strokez, fill) {

	//console.log("inner_rect stroke: "+strokez);

	// Init stuff
	this.timeband_paper = this;
	this.band_title = band_title;
	var totalPixelWidth = totalPixelWidth;
	var lineHeight = lineHeight;
	var lineSpacing = lineSpacing;
	//var stroke = stroke;
	//var fill = fill;
	var categoryArray = ['Undefined category'];

	var inner_rect = this.rect(0, 0, 20, 20, 5); // 5 px rounded corners 
	inner_rect.attr("stroke", strokez);
	inner_rect.attr("fill", fill);
	this.inner_rect = inner_rect;
	
	var min_x = Number.MAX_VALUE;
	var max_x = Number.MIN_VALUE;

	console.log("Raphael.fn.timeband starting:"+this.band_title+"; this="+this+"; inner_rect stroke="+strokez);
	
	// Print title
	var bandText = this.text(totalPixelWidth/2, lineHeight-3, this.band_title); // Todo: change to width of band
 
	// A hidden private function
	function categoryToNumber(category){
		var val = $.inArray(category, categoryArray)
		if (val == -1) {
			categoryArray.push(category);
			return categoryArray.length -1
		} else {
			return val;			
		}
	}
	
	// Make addMark function publicly accessible by adding it to 'this' 
    this.addMark = function(isoDate, isoDateEndOrDuratuionInDays, text, category, details, popupLink){
		var my_paper;
					
		my_paper = this //timeband_paper;
		console.log("Raphael.fn.timeband.addmark text:"+text+" paper="+my_paper);
		var dd = new Date();
		dd.setISO8601(isoDate);
		var x = toX(dd, mintime, maxtime, totalPixelWidth)
		var x_width = 0;
		
		if(isoDateEndOrDuratuionInDays){
			var dd_end = new Date();
			if ($.type(isoDateEndOrDuratuionInDays) === "string") {
				dd_end.setISO8601(isoDateEndOrDuratuionInDays);	
				//console.log("createMark STRING dd_end= "+dd_end+ " dd="+dd )
			} else {
				// int means width in days
				dd_end.setTime(dd.getTime()+isoDateEndOrDuratuionInDays*1000*60*60*24); // increase days
				//console.log("createMark NON-STRING dd_end= "+dd_end+ " dd="+dd +"isoDateEndOrDuratuionInDays="+isoDateEndOrDuratuionInDays)
			}
			// console.log("createMark - text:"+text+" dd_end= "+dd_end+ " dd="+dd )		
			var x_end = toX(dd_end, mintime, maxtime, totalPixelWidth)
			x_width = x_end - x
		} 

		// Make sure something always shows
		if (x_width < 0.1) x_width = 0.1;

		var cn = categoryToNumber(category)
		var y = cn * (lineHeight + lineSpacing)
		//console.log("createMark - "+category+" = "+cn+ " x="+x +" x_width="+x_width)		
		var rect = my_paper.rect(x, y, x_width, lineHeight, 0); 
		rect.attr("stroke", "rgba(100%, 100%, 100%, 100%)");
		rect.attr("fill", "rgba(100%, 100%, 100%, 60%)");
		
		x_right = x+x_width;
		
		var t = my_paper.text(x+x_width/2, y+lineHeight/2, text);
		
		if(x < min_x){
			min_x = x;
		}

		if((x_right) > max_x){
			max_x = x_right;
		}

		// TODO recalc inner_rect
		ix = min_x-3
		iy = lineHeight+lineSpacing-3;
		iwidth = max_x-min_x+6
		
		var surroundingBoxHeight = categoryArray.length*(lineHeight+lineSpacing)
		
		inner_rect.attr({x: ix, y: 0, width: iwidth, height: surroundingBoxHeight });

		// Move title
		bandText.attr({x: ix+iwidth/2})		

		// Reconfigure paper height after mark adding
		my_paper.setSize(totalPixelWidth, surroundingBoxHeight);
		
		// Find first start 
		//console.log("Reconfigure: ..."+my_paper.height);

				//inner_rect.x, inner_rect.y, inner_rect.width, inner_rect.height, 0);
		
		// glow(size, color, opacity)
		var glow = rect.glow(5, '#FFFFFF', 0.75);
		glow.hide();
		var combo = my_paper.set();
		combo.push(
		    rect,
		    t
		);
		combo.hover(	
			function (event) {
				//console.log("hover in: "+text);
				glow.show();
				//this.attr({fill: "white"});
			},
			function (event) {
				//console.log("hover out: "+text);
				glow.hide();
				//this.attr({fill: "red"});
			}
		)
		// CLICK-EVENT -> POPUP
//		var $dialog = $('<div></div>')
//		.html('This dialog will show every time!')
//		.dialog({
//			autoOpen: false,
//			title: 'Basic Dialog'
//		});
		//jQuery.post( url, [data,] [success(data, textStatus, jqXHR),] [dataType] )
		rect.node.onclick = function (){$.get(popupLink,
				function (responseData, textStatus, jqXHR) {
			var $dialog = $('<div></div>')
			.html(responseData)
			.dialog({
				autoOpen: false,
				title: text
			});
			$dialog.dialog('open')}
		);
		};
    };
	 // end of addmark

  	return this;
  	
 } // Raphael.fn.timeband



function testLine(target) {
	//console.log("Hello from line-script");
	// Creates canvas
	var paper = Raphael(target, 3200, 150);

	// Creates circle at x = 50, y = 40, with radius 10	
	var circle = paper.circle(150, 140, 10);
	// Sets the fill attribute of the circle 
	circle.attr("fill", "rgba(100%, 0%, 0%, 50%)");
	// Sets the stroke attribute of the circle
	circle.attr("stroke", "rgba(100%, 50%, 100%, 50%)");
	
	var rect = paper.rect(0, 0, 100, 15, 5);//circle.getBBox().width(), circle.getBBox().height());
	rect.attr("stroke", "rgba(100%, 0%, 0%, 70%)");
	rect.attr("fill", "rgba(100%, 0%, 0%, 50%)");

}

