<#assign EEE_title = "Timeline experiment-2 for EHR "+ehrId><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
		<head>
		<meta content="text/html; charset=utf-8" http-equiv="content-type" />
		<title>${EEE_title}</title>

		<link rel="stylesheet" href="/static/js/jquery/jquery-ui-1.8.18.custom/css/smoothness/jquery-ui-1.8.18.custom.css" type="text/css">
		<link rel="stylesheet" href="/static/css/timeline.css" type="text/css"> 	
		<style type="text/css">
		body {
			margin: 0;  
			padding: 0; 
		    /* width: 1500px; */  /* width: 100%; */
			background: white;
		}		
		</style>

		<!-- This might compensate for Microsoft Internet Explorer's less competent engine
			<script type="application/x-javascript" src="/static/js/sizzle-1.5.1.js"></script> 
		-->
				
		<script type="application/x-javascript" src="/static/js/d3/d3-2.0.0/d3.min.js"></script>
		<script type="application/x-javascript" src="/static/js/d3/d3-2.0.0/d3.time.min.js"></script>		
		<!-- More up to date, but currently not working on iPad 
			<script type="application/x-javascript" src="/static/js/d3/d3-2.8.0/d3.v2.min.js"></script> 
		-->

		<script type="application/x-javascript" src="/static/js/jquery/jquery-1.5.1.min.js" ></script> 
		<!-- The bt tooltips do not work with the newest jQuery:
		 <script type="application/x-javascript" src="/static/js/jquery/jquery-1.7.2.min.js" ></script>
	    -->

		<script type="application/x-javascript" src="/js/utils/EEE-utils.js" ></script> 	    
	  
	    
		<!-- tooltip/balloon reseources, see http://plugins.jquery.com/project/bt or the local directory /js/bt-0.9.5-rc1/DEMO/-->
		<script type="application/x-javascript" src="/static/js/jquery/bt-0.9.5-rc1/jquery.bt.min.js"></script>

		<script type="application/x-javascript" src="/static/js/jquery/jquery-ui-1.8.18.custom/js/jquery-ui-1.8.18.custom.min.js"></script>
</head>
<body>
<div id="EEE_UI">		
		<script type="text/javascript">
		
		console.log("hello world, starting timeline-2 script tag");
		
	    // Note/warning: using freemarker variable ${ehrId} below
		var ehrId = "${ehrId}";
		
		var svg; // Reference to the root svg of the timeline graph
	
		// D3-related inspired by D3-bundled example axis-transition.html
		var m = [0, 0, 15, 0]; //margins
		var w = 2800 - m[1] - m[3];
		//h1 = 150;
		//h2 = 100;

		// Time-scale (used as x-axis shared by many y-scales)
		var x = d3.time.scale().range([0, w]);

//		var	settingScale = d3.scale.ordinal()
//		settingScale.rangeRoundBands([0, h/2], 0.5); // range([0, h/3]); // rangePoints([0, h], 0.5); // rangeRoundBands([0, h], 0.5);
//		settingScale.domain(["vårdcentral", "akutmottagning", "ortopedklinik", "telefonkontakt", "annan vård", "hemmet"]);//(["vårdcentral","2","3","4"]);//(["vårdcentral", "akutmottagning", "ortopedklinik"]);    	
//		var	settingScale_axis = d3.svg.axis().scale(settingScale);

		// Blood pressure linear scale 
		var	y_bp = d3.scale.linear().range([0, 100]); 
		y_bp.domain([250, 50]);
		var	yAxis_bp = d3.svg.axis().scale(y_bp);
		
		function doMark(b){
			// console.log("barr:"+$(b.currentTarget.parentElement).serializeArray());
			// console.log(b);
			var postdata = $(b.currentTarget.parentElement).serializeArray();
			// var postdata = $('#embeddedShareForm').serializeArray();
			console.log("postdata"+postdata);
			console.log(postdata);				
			var dia = $('<div id="qwerty">Generating bookmark, please wait...</div>');
			dia.dialog({ 
				modal: true,
				position: 'top',
				title: 'Share bookmark',
				minWidth: 300,
				maxWidth: 900,
				width: 600,
				buttons: {
					"Save changed title": function() {
						var titleFormFields = $('#bmUpdateForm').serializeArray();
						var bmUri = $('#bmUpdateForm').attr('action')
						dia.load(bmUri, titleFormFields, function(event){
							console.log("title update ok, event:"+event);
							dia.load(bmUri+'info/ #core');
						});
					},
					"Close": function() {
						$( this ).dialog( "close" );
					}
				}					
				})
			.load('/bm/test/ #core', postdata);				
			return false; // TODO: check return semantics of jquery .click(...)
		} // end doMark
		
		function settingToYvalue(settingcode){
			switch(settingcode.trim()){
			case '227':
				/* EMERGENCY: <concept rubric="emergency care" id="227"/> */
				return 5;
				break;
			case '232':
			case '233':
			case '234':		
				/* SECONDARY: <concept rubric="secondary medical " id="232"/> <concept rubric="secondary nursing " id="233"/> <concept rubric="secondary allied " id="234"/> */
				return 20;
				break;
			case '228':
			case '229':
			case '230':
				/* PRIMARY: rubric="primary medical " id="228"; rubric="primary nursing " id="229; rubric="primary allied " id="230" */
				return 40;
				break;
			case '225':
				/* HOME : <concept rubric="home" id="225"/> */
				return 60;
				break;
			default:
//				console.log("settingToYvalue defaulting");
			  return 75;
			}
		}

		// Dynamic BeautyType based popups
		function addBT(node, data){
			var $myNode = $(node);
			$myNode.bt( {
				trigger: 'click', 
				positions: 'bottom',
				ajaxPath: '../'+data.composition_id+'?media=text/html #versionWrap',
				ajaxLoading: '<p>'+data.composition_title+'<br/>loading...</p>',
				ajaxError: "<strong>Strange?!</strong> There was a problem getting this content at URI "+'../'+data.composition_id+
				" Here's what we know: <em>%error</em>.",
				postShow: function(box){$('.embeddedShareButton').click(doMark);},
				padding: 10,
				margin: 5,
				width: 500,
				spikeLength: 25,
				spikeGirth: 15,
				cornerRadius: 20,
				fill: 'rgba(255, 255, 255, .95)',
				strokeWidth: .5,
				strokeStyle: '#222', 
				shadow: true,
				shadowColor: 'rgba(0,0,0,.4)',
			    shadowOffsetX: 5,
			    shadowOffsetY: 5,
				cssStyles: {color: '#FFF'}
					}
			);
		};

		// *************** AQL Queries and callback functions *******************
		
		var queries = new Array();
		
		queries[0] = new String("All compositions");
		queries[0].aql = "SELECT " + "\n" +
		" v/uid/value as composition_id,"+ "\n" +
		" c/name/value as composition_title, " + "\n" +
		" c/context/start_time/value as comp_start_time, "+ "\n" +
		" c/context/end_time/value as comp_end_time, "+	 "\n" +
		" c/context/health_care_facility/name as comp_facility, "+ "\n" +		
		" c/context/setting/defining_code/code_string as comp_setting "+ "\n" +		
		" FROM Ehr e[ehr_id/value=$current_ehr_uid] "+ "\n" +
		" CONTAINS VERSION v "+ "\n" +
		" CONTAINS COMPOSITION c "+ "\n" +
		" ORDER BY c/context/start_time/value ";
		queries[0].sha="288c91583ca9bdca41d1962cdb2ee1207002e92f";
		queries[0].done = false;
		queries[0].responseArray = [ ]
		queries[0].callback = function(q, queryResponse) {
			console.log("Entering timeline-2 queries[0].callback");
//			console.log(q);
			q.responseArray = objectifyAQLXmlResult(queryResponse);
			// Parse dates and put into standardized variables + do other query specific processing
			q.responseArray.forEach(function(d) {
				// console.log("d.comp_start_time:"+d.comp_start_time+"Z")	  	
				d.date = d3.time.format.iso.parse(d.comp_start_time+"Z");
				if(d.comp_end_time) {
					d.enddate = d3.time.format.iso.parse(d.comp_end_time+"Z");
				}
				// console.log("d.date:"+d.date)
				d.category = d.composition_title;
				// console.log("d.category:"+d.category)
				d.yValue = settingToYvalue(d.comp_setting);
			});
			q.minDate = d3.min(q.responseArray, function(d) { return d.date; })
			q.maxDate = d3.max(q.responseArray, function(d) { return d.date; }) // TODO: check enddate (if available) not startdate!
			q.done = true;
			drawIfAllQueriesDone();
		} 
		queries[0].height = 150;
		queries[0].draw = function(q){
			var xAxis = d3.svg.axis().scale(x).tickSize(-q.height).tickSubdivide(true); // tickSize is the height of the vertical scale lines
			// Add the x-axis ticks & bottom labels.
			svg.append("svg:g")
			.attr("class", "x axis")
			.attr("transform", "translate(0," + q.height + ")") // Use h - something if margins are scrapped
			.call(xAxis);
			
			// Create and position a place (svg g group) to put symbols and text in
			var comps = svg.selectAll("g.composition")
			.data(q.responseArray)
			.enter().append("svg:g")
			.attr("class", function(d) {return ('openEHR-setting-'+d.comp_setting)})
			//.attr("class", "composition")
			.classed("composition", true)			
			//.attr("id", f)
			.attr("transform", function(d, i) {return "translate(" + x(d.date) + "," + d.yValue + ")";} ); //+ (80-(i%3)*30) + ")"; }) // Using modulus (%3) to spread dots into three vertical levels
//			.on("mouseover", function(){d3.select(this).style("fill", "blue");})
//			.on("mouseout", function(){d3.select(this).style("fill");})
//			.on("click", function(d){})

			var theDiv = d3.select("#timeline");
			
			var pAnchors = theDiv.selectAll("span.popAnchor")
			.data(q.responseArray)
			.enter().append("span")
			.style("left", function(d){ return (x(d.date)-10)+"px";}) // -10 to center the 20x20-box
			.style("top", function(d,i){ return (d.yValue-10)+"px";}) //(80-(i%3)*30-10)+"px";})
			.attr("class", "popAnchor")
			.each(function(d){ addBT(this, d)})
						
			comps.append("svg:path")
			.attr("class", "dot")
			//.attr("stroke", function(d, i) { return d3.scale.category10(i); })
			//.attr("transform", function(d) { return "translate(" + x(d.date) + "," + y(d.category) + ")"; })
			.attr("d", d3.svg.symbol().type(function(d, i) { return symbol(3);} )) // { return symbol(i); })); 

			var text_labels = comps.append("svg:g")
			.attr("class", "text_label_foundation")
			.attr("transform", function(d) {return "rotate(25)";})
			.on("click", function(d){console.log("d.composition_title:"+d.composition_title)})
			
			text_labels.append("svg:text")
			.attr("x", 13)
			.attr("y", 0) // settingScale.rangeBand() / 2)
			.attr("dx", -6)
			.attr("dy", ".35em")
			.attr("text-anchor", "start")
			.text(function(d, i) { return d.composition_title })

			text_labels.append("svg:text")
			.attr("x", 13)
			.attr("y", 8)
			.attr("dx", -6)
			.attr("dy", ".35em")
			.attr("text-anchor", "start")
			.attr("font-size", "7px")
			.text(function(d, i) { return d.comp_facility });
		}
			
					
		//bpQuery
		queries[1] = new String("All blood pressures in encounters");
		queries[1].aql = "SELECT " + "\n" +
		" v/uid/value as composition_id,"+ "\n" +
		" obs/data/origin/value as measurement_time, " + "\n" +
		" obs/data/events/data/items[at0004]/value/magnitude as systolic, " + "\n" +
		" obs/data/events/data/items[at0005]/value/magnitude as diastolic, " + "\n" +
		" c/context/setting/value as comp_setting, " + "\n" +	
		" c/name/value as composition_title, " + "\n" +
		// " c/context/end_time/value as comp_end_time,"+ "\n" +	
		" c/context/start_time/value as comp_start_time "+ "\n" +
		" FROM Ehr e[ehr_id/value=$current_ehr_uid] "+ "\n" +
		" CONTAINS VERSION v "+ "\n" +
		" CONTAINS COMPOSITION c[openEHR-EHR-COMPOSITION.encounter.v1] "+ "\n" +
		" CONTAINS OBSERVATION obs[openEHR-EHR-OBSERVATION.blood_pressure.v1]"+ "\n" +
		//" WHERE obs/data/events/data/items[at0004]/value/magnitude > 185 "+ "\n" +
		" ORDER BY c/context/start_time/value ";
		queries[1].sha="0e231430ceb511f981ba2996fb930429d7be27be";
		queries[1].done = false;
		queries[1].responseArray = [ ];
		queries[1].callback = function(q, queryResponse) {
			console.log("Entering timeline-2 queries[1].callback");
			q.responseArray = objectifyAQLXmlResult(queryResponse);
			// console.log(q);
			// Parse dates and put into standardized variables + do other query specific processing
			q.responseArray.forEach(function(d) {
				// console.log("d.comp_start_time:"+d.comp_start_time+"Z")	  	
				d.date = d3.time.format.iso.parse(d.comp_start_time+"Z");
				if(d.comp_end_time) {
					d.enddate = d3.time.format.iso.parse(d.comp_end_time+"Z");
				}
				// console.log("d.date:"+d.date)
				d.category = d.composition_title;
				//console.log("y_bp:" + y_bp(d.systolic));
				d.yValue = settingToYvalue(d.comp_setting);
			});
			q.minDate = d3.min(q.responseArray, function(d) { return d.date; })
			q.maxDate = d3.max(q.responseArray, function(d) { return d.date; }) // TODO: check enddate (if available) not startdate!
			q.done = true;
			drawIfAllQueriesDone();
		} 
		queries[1].height = 150;
		queries[1].draw = function(q){
			console.log("Should draw: "+q)

			var xAxis = d3.svg.axis().scale(x).tickSize(-q.height).tickSubdivide(true); // tickSize is the height of the vertical scale lines
			// Add the x-axis ticks & bottom labels.
			svg.append("svg:g")
			.attr("class", "x axis")
			.attr("transform", "translate(0," + (queries[0].height+q.height) + ")") // Use h - something if margins are scrapped
			.call(xAxis);
			
			// Create and position a place (svg g group) to put symbols and text in
			var bpbase = svg.selectAll("g.bloodPressureMarker")
			.data(q.responseArray)
			.enter().append("svg:g")
			.attr("class", "bloodPressureMarker")
			//.attr("id", f)
			.attr("transform", function(d, i) {return "translate(" + x(d.date) + "," + q.height+ ")";} );
//			.on("click", function(d){})
						
//			bpbase.append("svg:path")
			bpbase.append("svg:text")
			.attr("class", "systolic")
			.attr("text-anchor", "middle")
			.text(function(d, i) { return d.systolic })
			//.attr("d", d3.svg.symbol().type(function(d, i) { return symbol(7);} )) // { return symbol(i); })); 
			.attr("transform", function(d, i) {return "translate(0," +y_bp(d.systolic) + ")";} ); 

			//bpbase.append("svg:path")
			bpbase.append("svg:text")
			.attr("class", "diastolic")
			.attr("text-anchor", "middle")
			.text(function(d, i) { return d.diastolic })
			//.attr("d", d3.svg.symbol().type(function(d, i) { return symbol(7);} )) // { return symbol(i); })); 
			.attr("transform", function(d, i) {return "translate(0," +y_bp(d.diastolic) + ")";} );
		}
		
		//Medication Query
		queries[2] = new String("All medication instructions in encounters");
		queries[2].aql = "SELECT " + "\n" +
		"v/uid/value as composition_id," + "\n" +
		"c/name/value as composition_title, " + "\n" +
		"c/context/start_time/value as composition_time, " + "\n" +
		"it/items[at0007]/value/magnitude as duration_magnitude, " + "\n" +
		"it/items[at0007]/value/units as duration_unit, " + "\n" +
		"it/items[at0018]/items[at0019]/value/value as date_of_administration " + "\n" +
		"FROM Ehr e[ehr_id/value=$current_ehr_uid] " + "\n" +
		"CONTAINS VERSION v " + "\n" +
		"CONTAINS COMPOSITION c[openEHR-EHR-COMPOSITION.encounter.v1] " + "\n" +
		"CONTAINS INSTRUCTION i[openEHR-EHR-INSTRUCTION.medication.v1] " + "\n" +
		"CONTAINS ITEM_TREE it[openEHR-EHR-ITEM_TREE.medication.v1] " + "\n" +
		"ORDER BY  c/context/start_time/value";
		queries[2].sha="cc7b4b24ec9a2080da00cee0c53d3b57acb1e7d4";
		queries[2].done = false;
		queries[2].responseArray = [ ];
		queries[2].callback = function(q, queryResponse) {
			console.log("Entering timeline-2 queries[3].callback");
			q.responseArray = objectifyAQLXmlResult(queryResponse);
			// console.log(q);
			// Parse dates and put into standardized variables + do other query specific processing
			q.responseArray.forEach(function(d) {
				// console.log("d.date_of_administration: "+d.date_of_administration)	
				var myformat = d3.time.format("%Y-%m-%d");
				d.date = myformat.parse(d.date_of_administration); // returns a Date
				//d.date = d3.time.format.parse(d.date_of_administration);
				
				// TODO: Calculate enddate based on duration
				if(d.comp_end_time) {
					d.enddate = d3.time.format.iso.parse(d.comp_end_time+"Z");
				}
				// console.log("d.date:"+d.date)
				d.category = d.composition_title;
				// console.log("d.category:"+d.category)
			});
			q.minDate = d3.min(q.responseArray, function(d) { return d.date; })
			q.maxDate = d3.max(q.responseArray, function(d) { return d.date; }) // TODO: check enddate (if available) not startdate!
			q.done = true;
			drawIfAllQueriesDone();
		}
		queries[2].height = 10;
		queries[2].draw = function(q){
			console.log("Should draw: "+q)
		}		
		
			
//		$(node).bt( '<div class="closeX"><a href="javascript:void($('+"'#bookmarkButton'"+').btOff());"><img src="/images/close.gif" alt="close" width="12" height="12" class="gmap-close" /></a></div>'+
//		'<p class="popupHeading">'+data.composition_title+'</p>'+
//		'<div class="popupInnerBox" id="bmPopupBox">'+					
//		'Loading details...</div>'+
//		'<p class="popupHeading">Debug info</p>'+
//		'<div class="popupInnerBox" id="bmPopupBox">'+
//		'<FORM id="bmPopup" METHOD="POST" ENCTYPE="application/x-www-form-urlencoded" ACTION="/bm/test/">'+
//		'Tags: <INPUT TYPE="text" name="tags" value=""/><BR/>'+
//		'<INPUT TYPE=BUTTON OnClick="console.log('+data.composition_id+');" VALUE="Open in side pane" /><INPUT TYPE=BUTTON VALUE="Bookmark/share" /></FORM>'+
//		'</div>',			

		function drawIfAllQueriesDone() {
			// Iterate over whole array of queries to check if done
			var allDone = true;
			queries.forEach(function(q) {
				// console.log("drawIfAllQueriesDone(): "+q+" done = "+q.done);
				if (!q.done) allDone = false;
			});

			console.log('allDone = '+allDone); 
			// return (quit function) if not all done
			if (!allDone) return;
			
			// if done continue...
			
			// Compute the minimum and maximum date
			// Iterate over whole array of query responses and calculate max of max and min of min
			var minArray = [ ];
			var maxArray = [ ];
			
			queries.forEach(function(q) {
				minArray.push(q.minDate);
				console.log(q+": min="+q.minDate);
				maxArray.push(q.maxDate);
				console.log(q+": max="+q.maxDate);
			});
			
			// x.domain([compositionQueryResponseData[0].date, compositionQueryResponseData[compositionQueryResponseData.length - 1].date]);
			var minDate = d3.min(minArray, function(d) { return d; })
			var maxDate = d3.max(maxArray, function(d) { return d; })
			console.log("Min:"+minDate+"  Max:"+maxDate);

			// Add 10% on each side
			var dateLength = maxDate - minDate;
			var timeMargin = Math.round(dateLength / 10) // 10%
			console.log("timeMargin: "+timeMargin)			
			var outerMin = new Date();
			var outerMax = new Date();
			outerMin.setTime(minDate-timeMargin);
			outerMax.setTime(maxDate.valueOf()+timeMargin);
			console.log("outerMin: "+outerMin);
			console.log("outerMax: "+outerMax);
			x.domain([outerMin, outerMax]); // sets domain for the x scale, see further up...
			
			// TODO: Add shaded areas for unfetched margin-zones
						
			// then draw base
			drawDiagramBase();
			
			// Iterate over whole array of query responses and draw
			queries.forEach(function(q) {
				q.draw(q);
			});
			
		};

		
		function drawDiagramBase() {
			console.log("Entering timeline-2 drawDiagramBase");
			//alert("entering parseXml with xml:\n"+$(xml).find("results"));
			
			// TODO: loop over queries instead
			h = queries[0].height + queries[1].height + queries[2].height - m[0] - m[2];
			
			// Add an SVG element with the desired dimensions and margin.
			svg = d3.select("#timeline").append("svg:svg")
			.attr("class", "svgChartRoot")
			.attr("width", w + m[1] + m[3])
			.attr("height", h + m[0] + m[2])
//			.append("svg:rect")
//			.attr("width", w + m[1] + m[3])
//			.attr("height", h + m[0] + m[2])
			.append("svg:g")
			.attr("transform", "translate(" + m[3] + "," + m[0] + ")")

			
//			Add the clip path.
		/*	svg.append("svg:clipPath")
 			.attr("id", "clip")
 			.append("svg:rect")
   			.attr("width", w)
   			.attr("height", h);
			*/ 



//			svg.append("svg:g")
//			.attr("class", "x axis")
//			.attr("transform", "translate(0," + Math.round(h/2-20) + ")") // Use h - something if margins are scrapped
//			.call(xAxis);

			//console.log("hello --- TRACE PRINT ---: ");	

			/*  Add the y_bp-axis.
			svg.append("svg:g")
   			.attr("class", "y axis")
   			.attr("transform", "translate(" + w + ",0)")
   			.call(yAxis_bp);	  	
	   		console.log("hello --- TRACE PRINT 2 ---: ");	
			 */

			symbol = d3.scale.ordinal().range(d3.svg.symbolTypes);
	
		};// end drawDiagramBase
		
		
		$(document).ready(function() {
			console.log("hello world, starting timeline-2 $(document).ready");
			
				// Iterate over queries and post them one by one
			    console.log(queries)

			    queries.forEach(function(q){
			    	console.log("will call executeQuery("+q+", "+ehrId+", ...)");
					executeQuery(q, ehrId, q.callback);
				}); // end queries.forEach(function(q))
				
			console.log("hello world ending timeline-2 $(document).ready");

		}); // end of $(document).ready


		</script>
		<div id="timeline"> </div>
		</div><!-- end of div id="EEE_UI" -->
	</body>
</html>
