<#ftl ns_prefixes =
{"soap":"http://schemas.xmlsoap.org/soap/envelope/",
 "xsi":"http://www.w3.org/2001/XMLSchema-instance",
 "xsd":"http://www.w3.org/2001/XMLSchema",
 "oe":"http://schemas.openehr.org/v1" ,
 "eee":"http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd" }><#assign EEE_title = "Timeline experiment-1 for EHR "+ehrId>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
 <meta content="text/html; charset=iso-8859-1" http-equiv="content-type" />
 <title>${EEE_title}</title>
 <link rel="stylesheet" href="/css/style1.css" type="text/css" />
 <link rel="stylesheet" type="text/css" href="http://ajax.googleapis.com/ajax/libs/jqueryui/1.7.2/themes/base/jquery-ui.css">
 <script src="http://cdn.jquerytools.org/1.2.5/full/jquery.tools.min.js"></script> 
 <!-- link rel="stylesheet" href="/js/timeglider/Timeglider.css" type="text/css" media="screen" title="no title" charset="utf-8" -->
 <script type="text/javascript" src="/js/lib/jquery-1.5.1.min.js" ></script>
 <script type="text/javascript" src="/js/lib/jquery-ui-1.8.12.custom.min.js" ></script>
 <script type="text/javascript" src="/js/lib/raphael-min.js"  charset="utf-8"></script> 
 <script type="text/javascript" src="/js/raphael.glow_shadow.js"  charset="utf-8"></script> 
 <script type="text/javascript" src="/js/line-script-1.js"  charset="utf-8"></script> 
 <#-- Moved style info here (overrides /static/css/style1.css instead of altering it)-->
 <STYLE type="text/css">
 	a:link {
	 COLOR: gray;
 	}
 	a:visited {
	 COLOR: white;
	}
	a:hover {
	 COLOR: white;
	}
	a:active {
	 COLOR: white;
	}
</STYLE>

	 
</head>
<body>
<h1>${EEE_title}</h1>
<p>Timeline page for the EHR with ID: ${ehrId}</p>


<!-- TIMEGLIDER PLACEMENT ... the intended location for the timeline -->
<div id='event-frame' class='frame-primary'>
<div id='eventband-placement' style="background-color:lightgray;" class='whiteBorder'></div>
</div>
<div id='time-frame' class='frame-primary'>
<div id='timeline-placement' class='whiteBorder'></div>
<div id='timeline2-placement' class='whiteBorder'></div>
<div id='timeline3-placement' class='whiteBorder'></div>
</div> <!-- End of 'time-frame' -->

<div id='date-tests' class='secondary-c-5'></div>
<br/><br/><br/>
<hr/>
<br/><br/>
<h2>Under the hood (Technical experiments)</h2>
<br/>
<table class='timeline-table' id="resultTable" focus_date="2007-11-07" title="Patient overview" initial_zoom="10" description="Patient overview test DESCRIPTION">
<!-- Timeglider comment: 
	The first row of the table is reserved for meta-data. Class values 
	below are *critical* for mapping out data from the <td> elements that follow 
	- though order is not important. The text in <td> elements is *not* critical: 
		just the class names. -->
	<tr>
	<th class="tg-startdate">start date</th>
	<th class="tg-enddate">end date</th>
	<th class="tg-title">title</th>
	<th class="tg-description">description</th>
	<th class="tg-icon">icon</th>
	<th class="tg-date_limit">date limit</th>
	<th class="tg-importance">importance</th>
	<th class="tg-link">link</th>
	</tr>
	<!--  Timeglider comment:
		EVENTS ARE IN THE 2nd+ ROWS OF THE TABLE. While appropriate for viewing 
		as a visual table, order of rows is not important. -->
	<!--  LiU EEE comment:
		We add the rows and their comment dynamically in the parseXml(xml) function -->
	</table>

	<script type="text/javascript">


	var initDone = false;
	var initCount = 0;
	
	var mintime = new Date();
	mintime.setISO8601('2006-01-01T22:07:03.000Z');
	
	var maxtime = new Date();
	maxtime.setISO8601('2009-01-01T22:07:03.000Z');
	
	var totalwidth = 2200;
	var preferredHeight = 25;
	
	$("#eventband-placement").width(totalwidth);
	var eventPaper = Raphael(document.getElementById('eventband-placement'), totalwidth, 20);
	eventPaper.timeband("Händelser", totalwidth, preferredHeight, 5, "rgba(10%, 0%, 100%, 0%)", "rgba(10%, 0%, 100%, 0%)");
		
	$("#timeline-placement").width(totalwidth);
	var firstPaper = Raphael(document.getElementById("timeline-placement"), totalwidth, 20)
	firstPaper.timeband("Hypertoni, I10.9", totalwidth, preferredHeight, 5, "rgba(0%, 100%, 0%, 50%)", "rgba(0%, 100%, 0%, 30%)");
	//firstPaper.timeband.inner_rect.attr({fill: "rgba(100%, 100%, 100%, 20%)"});
	
	$("#timeline2-placement").width(totalwidth);
	var secondPaper = Raphael(document.getElementById("timeline2-placement"), totalwidth, 20)
	secondPaper.timeband("Darrhänthet", totalwidth, preferredHeight, 5, "rgba(5%, 0%, 100%, 50%)", "rgba(5%, 0%, 100%, 30%)");
	
	$("#timeline3-placement").width(totalwidth);
	var thirdPaper = Raphael(document.getElementById("timeline3-placement"), totalwidth, 20)
	thirdPaper.timeband("Empty test", totalwidth, preferredHeight, 5, "rgba(10%, 0%, 10%, 50%)", "rgba(10%, 0%, 10%, 30%)");
	
	$(document).ready(function() {
		//console.log("hello world");
		var tld = "timeline";

		var predefinedQuery = "SELECT v/uid/value as composition_id,"+
		" obs/data/origin/value as measurement_time, " +
		" obs/data/events/data/items[at0004]/value/magnitude as systolic, "+
		" obs/data/events/data/items[at0005]/value/magnitude as diastolic, "+
		" c/name/value as composition_title, " +
		" c/context/end_time/value as comp_end_time,"+	
		" c/context/start_time/value as comp_start_time "+
		" FROM Ehr [uid=$current_ehr_uid] "+
		" CONTAINS VERSION v "+
		" CONTAINS COMPOSITION c[openEHR-EHR-COMPOSITION.encounter.v1] "+
		" CONTAINS OBSERVATION obs[openEHR-EHR-OBSERVATION.blood_pressure.v1]"+
		//" WHERE obs/data/events/data/items[at0004]/value/magnitude > 185 "+
		" ORDER BY c/context/start_time/value ";
		$("#loading").show();

		/*alert("Zzzztarting! "+predefinedQuery);*/
		
		// Using freemarker variable ${ehrId} below
		$.post("/ehr/${ehrId}/AQL",
				{ query: predefinedQuery, debug: "false" },
				parseXml
		);


		$("#toggle").click(function () { 
			if (tld == "timeline") {
				$("#timeline-placement").css({"display":"none"});
				$(".timeline-table").css({"display":"block"});
				tld = "table";
			} else {
				$("#timeline-placement").css({"display":"block"});
				$(".timeline-table").css({"display":"none"});

				tld = "timeline";
			}

		});
		
		//testLine(document.getElementById("timeline-placement"));
		
// 		var d = new Date();
// 		d.setISO8601('2010-01-01T22:07:03.000+08:00');		
//  		$("#date-tests").append('<p>'+d+' = '+toX(d, mintime, maxtime, totalwidth)+'</p>');
// 		d.setISO8601('2007-10-01');
//  		$("#date-tests").append('<p>'+d+' = '+toX(d, mintime, maxtime, totalwidth)+'</p>');

		//createMark('2007-10-01', false ,"Besök", "C07AB03", eventPaper, "2007-10-01 ATENOLOL tabl 50 mg, 98 x 1 st (Atenolol) Dosering: 1 TABLETT DAGLGIEN.")
		eventPaper.addMark('2007-10-01', 1 ,"VC Besök", "C07AB03", "2007-10-01 ATENOLOL tabl 50 mg, 98 x 1 st (Atenolol) Dosering: 1 TABLETT DAGLGIEN.")

 	 	firstPaper.addMark('2007-10-01', 98 ,"Atenolol 50mg/d", "C07AB03", "2007-10-01 ATENOLOL tabl 50 mg, 98 x 1 st (Atenolol) Dosering: 1 TABLETT DAGLGIEN.", "http://localhost:8182/ehr/example-ehr-id/cc67dfee-9caa-4b38-9bdc-c0c1fe8646eb::test2.eee.mi.imt.liu.se::1?media=text/html")
 		firstPaper.addMark('2007-10-01', 50 ,"Ramipril 10mg/d", "C09AA05", "2007-10-01 RAMIPRIL HEXAL tabl 5 mg, 100 x 1 st (Ramipril) Dosering: 1 TABLETT 2 GÅNGER DAGLIGEN.", "http://localhost:8182/ehr/example-ehr-id/cc67dfee-9caa-4b38-9bdc-c0c1fe8646eb::test2.eee.mi.imt.liu.se::1?media=text/html")
 		firstPaper.addMark('2008-01-14', 98 ,"Atenolol 50mg/d", "C07AB03", "2007-10-01 ATENOLOL tabl 50 mg, 98 x 1 st (Atenolol) Dosering: 1 TABLETT DAGLGIEN.", "http://localhost:8182/ehr/example-ehr-id/cc67dfee-9caa-4b38-9bdc-c0c1fe8646eb::test2.eee.mi.imt.liu.se::1?media=text/html")

//  		firstPaper.addMark('1975-04-01T22:07:03.000+08:00', false, "test3", "medication 2")
//  		firstPaper.addMark('1976-06-01T22:07:03.000+08:00', '1985-02-01T22:07:03.000+08:00',"test4", "medication 2") 		
 		
 		secondPaper.addMark('2008-01-14', 98 ,"Atenolol 50mg/d", "C07AB03", "2007-10-01 ATENOLOL tabl 50 mg, 98 x 1 st (Atenolol) Dosering: 1 TABLETT DAGLGIEN.", "http://localhost:8182/ehr/example-ehr-id/cc67dfee-9caa-4b38-9bdc-c0c1fe8646eb::test2.eee.mi.imt.liu.se::1?media=text/html")
 		thirdPaper.addMark('2007-01-01', "2007-12-31", "2007", "year")
 		thirdPaper.addMark('2008-01-01', "2008-12-31", "2008", "year")
 		
	}); // end of $(document).ready
	

	function parseXml(xml) {
		//console.log("Entering parseXml");
		//alert("entering parseXml with xml:\n"+$(xml).find("results"));
		$(xml).find("result").each(function() {
			// find each result
			// var myBindings = $(this).find('binding').attr('name');
			//$("p#testPara").append("<br/> :::: ");
			$("table#resultTable").append("<tr class='resultTableRow'>" +
					"<td class='comp_start_time-append'></td>" + //tg-startdate
					"<td class='comp_end_time-append'>&nbsp</td>" + //tg-enddate
					"<td class='composition_title-append'></td>" + //tg-title
					"<td><em class='composition_title-append'></em><br/>BP: <span class='systolic-replace'></span>/<span class='diastolic-replace'><span></td>" + //tg-description
					"<td>star_blue.png</td>" + //tg-icon
					"<td>&nbsp;</td>" + //tg-date_limit
					"<td>40</td>" + //tg-importance
					"<td><a href='#testlink' class='composition_id-replace-href'><span class='composition_id-replace'></span></a></td>" + //tg-link
			"</tr>");
			var myBindings = $(this).find('binding');
			myBindings.each(function() {
				//$("p#testPara").append($(this).attr('name')+'='+$(this).text()+' ');
				$(".resultTableRow:last").find("."+$(this).attr('name')+'-append').append($(this).text().trim());
				$(".resultTableRow:last").find("."+$(this).attr('name')+'-replace').replaceWith($(this).text().trim());
				$(".resultTableRow:last").find("."+$(this).attr('name')+'-replace-href').attr("href", "../"+($(this).text().trim()+"?media=text/html"));

			});
			//var myID = $(myBindings).children().find('name=composition_id') //.find('value').text();
			//console.log("myID = "+$(myID).text());
			//console.log(myID);
			$("#loading").hide();

			// Initiate timeline:
			initCount++;
			if (!initDone && initCount>2) {
				initDone = true;
				//console.log("init timeline");
				/* var tg1 = $("#timeline-placement").timeline({
					"min_zoom":18,  
					"max_zoom":55,  
					"data_source":"#resultTable" 
				});*/
			}

		});
		// alert("exiting parseXml");
	};
	</script>

	</body>
	</html>
