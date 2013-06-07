<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=0, minimum-scale=1.0, maximum-scale=1.0">
<meta name="apple-mobile-web-app-capable" content="yes">
<meta name="apple-mobile-web-app-status-bar-style" content="black">
<title>Patientöversikt (LiO AIV - LiU EEE)</title>

<link rel="stylesheet" href="/static/js/jquery/jquery-ui-1.8.18.custom/css/smoothness/jquery-ui-1.8.18.custom.css" type="text/css">
<!-- link rel="stylesheet" href="/js/jquery-ui-1.8.12.custom/css/south-street/jquery-ui-1.8.12.custom.css" type="text/css" -->
<link rel="stylesheet" href="/static/css/timeline.css" type="text/css"> 	

<style type="text/css" media="all">
body,ul,li,p {
	padding:0;
	margin:0;
	border:0;
}

body {
	font-size:12px;
	-webkit-user-select:none;
	-webkit-text-size-adjust:none;
	font-family:helvetica;
}

#alertsymbol {
	/* border:1px solid red; */
	padding:1px;
}

#header {
	position:absolute;
	top:0; left:0;
	width:100%;
	height:48px;
	/* line-height:45px; */
	background-color: #555; /* For explorer etc */
	background-image:-webkit-gradient(linear, 0 0, 0 100%, color-stop(0, #999), color-stop(0.02, #666), color-stop(1, #222));
	background-image:-moz-linear-gradient(top, #999, #666 2%, #222);
	background-image:-o-linear-gradient(top, #999, #666 2%, #222);
	/* padding:0px; */
	color:#eee;
	font-size:16px;
	text-align:center;
}

.ctext {
	text-align:center;
}

.rtext {
	text-align:right;
}

.topleft {
	font-size: 8px;
	left: 46px;
	top: 1px;
	position: absolute;
}

.topright {
	font-size: 8px;
	right: 87px;
	top: 1px;
	position: absolute;
}

#header	a.framing{
	color:#f3f3f3;
	text-decoration:none;
	font-weight:bold;
	text-shadow:0 -1px 0 rgba(0,0,0,0.5);
}

.framing{
	color:#f3f3f3;
	text-decoration:none;
	font-weight:bold;
	text-shadow:0 -1px 0 rgba(0,0,0,0.5);
	/* border:1px solid #ddd; */
}

#footer {
	position:absolute;
	bottom:0; left:0;
	width:100%;
	height:48px;
	background-color: #555; /* For explorer etc */
	background-image:-webkit-gradient(linear, 0 0, 0 100%, color-stop(0, #999), color-stop(0.02, #666), color-stop(1, #222));
	background-image:-moz-linear-gradient(top, #999, #666 2%, #222);
	background-image:-o-linear-gradient(top, #999, #666 2%, #222);
	padding:0;
	border-top:1px solid #444;
	color: #eee;
	font-size:16px;
	text-align:center;
}

.domain {
	background-color: #999;
}

#wrapper {
	position:absolute; z-index:1;
	top:45px; bottom:48px; left:0;
	width:100%;
	background:#fff;
	overflow:auto;
}

#scroller {
	/* position:relative; */
	/* -webkit-touch-callout:none;*/
	/* -webkit-tap-highlight-color:rgba(0,0,0,0); */
	width:2800px;
	padding:0;
	background:#fff;
}

frame {
	display:block;
	width:100%;
	margin-bottom:1em;
	padding:8px;
	font-size:14px;
}

p img {
	margin:4px 8px;
	-webkit-transform:translate3d(0,0,0);
}

/* Styles below for bookmarking popup*/

.popupInnerBox {
	padding:5px;
	color: #000;
	background-color:#eee;
	border:1px solid #ddd;
}

.popupHeading {
	color: #fff;
	padding: 5px;
    font-size: 16px;
    font-weight: bold;
	//margin-bottom:0.5em;
}

.closeX {
	position: absolute;
	right: 7px;
	top: 7px;
	border: none;
}

/* Force-layout nodes & links */
	circle.node {
	  stroke: #fff;
	  stroke-width: 1.5px;
	}

	line.link {
	  stroke: #999;
	  stroke-opacity: .6;
	}

</style>

<script type="application/x-javascript" src="/static/js/iscroll-4.1.9-ie.js"></script>

		<script type="application/x-javascript" src="/static/js/d3/d3-2.0.0/d3.min.js"></script>
		<script type="application/x-javascript" src="/static/js/d3/d3-2.0.0/d3.time.min.js"></script>		
		<!-- More up to date, but currently not working on iPad 
			<script type="application/x-javascript" src="/static/js/d3/d3-2.8.0/d3.v2.min.js"></script> 
		-->


<script type="application/x-javascript" src="/static/js/jquery/jquery-1.5.1.min.js" ></script>
<!-- The bt tooltips do not work with the newest jQuery:
	 <script type="application/x-javascript" src="/static/js/jquery/jquery-1.7.2.min.js" ></script>
-->

<!-- script type="application/x-javascript" src="/js/lib/jquery-ui-1.8.12.custom.min.js"></script -->
<script type="application/x-javascript" src="/static/js/jquery/jquery-ui-1.8.18.custom/js/jquery-ui-1.8.18.custom.min.js"></script>

<!-- tooltip/balloon reseources, see http://plugins.jquery.com/project/bt or the local directory /js/bt-0.9.5-rc1/DEMO/-->
<script type="application/x-javascript" src="/static/js/jquery/bt-0.9.5-rc1/jquery.bt.min.js"></script>

<script type="application/x-javascript" src="/js/utils/EEE-utils.js" ></script> 	    


<script type="text/javascript">

	var incomingFrag = window.location.hash;
	console.log("incoming window.location.hash: "+incomingFrag);	
	
	var bookmarkUriFunction = function() {return window.location.toString()}; // Initial function (later replaced)
	var bookmarkTitleFunction = function() {return "Timeline for ${ehrId}"};




	var myScroll;

	
	function bmSubmission(){
		$('#bmPopupBox').load('/bm/test/ #core', $("#bmPopup").serializeArray()); // { uri: bookmarkTitleFunction() }		
		return false;
	}
	

	function scrollLoadComplete(responseText, textStatus, XMLHttpRequest) {
		console.log("frame-1 scrollLoadComplete textStatus: "+textStatus);
		if(parsedFragmentData.y) {
			myScroll.scrollTo(0+parsedFragmentData.x, 0+parsedFragmentData.y, 200, true);		  		  
		}	
		
		if(parsedFragmentData.gz) {
		    myScroll.zoom(parsedFragmentData.x, parsedFragmentData.y, parsedFragmentData.gz, 300);		  
	    }
		myScroll.refresh();
		bookmarkUriFunction();
	}

	//	--- Utility function to create a string for the uri #fragment (gz means geometric zoom) ---
	var updateUriFragment = function(e) {window.location.hash = 'x='+Math.round(this.x) + '&y=' + Math.round(this.y) + '&gz=' +  Math.round(this.scale*100)/100 ; };

	function iScrollLoader(urlToLoad){
		// Clear <div id="viewPane"> and set up a new one configured for iScroll
		$('#viewPane').replaceWith( 
				'<div id="viewPane">'+
				  '<div id="wrapper"><div id="scroller"><p>Loading view...</p></div></div>'+
		        '</div>');
		
		// ...then load it with content		
		console.log("frame-1 iScrollLoader() before load, scroller:"+$('#scroller'));
		$('#scroller').load(urlToLoad, scrollLoadComplete);

		console.log("frame-1 iScrollLoader() after load ");
		
		myScroll = new iScroll('wrapper', { 
			zoom:true,
			zoomMin:0.5,
			zoomMax:3,
			x: parsedFragmentData.x,
			y: parsedFragmentData.y,			
			wheelAction: 'zoom',
			onScrollEnd: function(e){console.log("onScrollEnd this.x --> "+this.x);bookmarkUriFunction();},
			onZoomEnd: function(e){console.log("onScrollEnd this.x --> "+this.x);bookmarkUriFunction();}		
		}); // , lockDirection:true, desktopCompatibility:true});
		
		bookmarkUriFunction = function() {
			var result = 'v='+'iscroll'+'&x='+Math.round(myScroll.x) + '&y=' + Math.round(myScroll.y) + '&gz=' +  Math.round(myScroll.scale*100)/100 ;
			window.location.hash = result;
			console.log("bookmarkUriFunction returning: "+window.location.toString());
			return window.location.toString();
		};			
	}
	
	function plainLoader(urlToLoad){
		// Clear <div id="viewPane"> and set up a new one
		$('#viewPane').replaceWith( 
				'<div id="viewPane">'+
				  '<div id="plainTarget"><p>Before load</p></div>'+
		        '</div>');
		
		// ...then load it with content		
		console.log("frame-1 plainLoader() before loaing "+urlToLoad+' #guts');
		$('#plainTarget').load(urlToLoad, function(responseText, textStatus, XMLHttpRequest){console.log("Loader completed: responseText="+responseText+"  textStatus="+textStatus);});
		console.log("frame-1 plainLoader() after load ");
		
		bookmarkUriFunction = function(e) {
			console.log("bookmarkUriFunction e: "+e);
			var result = 'v='+'plain';
			window.location.hash = result;
			return window.location.toString()
		};
	}
	
	function changeView(){
		console.log("changeView(): viewSelector changed");
		$.globalEval($('#viewSelector').val());
	}
	
	$(document).ready(function(){  
		parsedFragmentData = queryStringToHash(incomingFrag);	
		console.log("parsedFragmentData.y: "+parsedFragmentData.y);
		console.log("parsedFragmentData.x: "+parsedFragmentData.x);

		document.addEventListener('touchmove', function (e) { e.preventDefault(); }, false);

		if(parsedFragmentData.v) {
			console.log("parsedFragmentData.v: "+parsedFragmentData.v);
			// TODO: Set the parsed value as default selected if available
			
		}
		
		// At first run, execute the code in the selected viewSelector value
		changeView(); 
		
		// Also register listener for changes of the viewSelector value;
		$('#viewSelector').change(changeView);


//		Add popup balloon
//		function bookmarkButtonClicked() {
//			$('#bookmarkButton').bt('<div class="closeX"><a href="javascript:void($('+"'#bookmarkButton'"+').btOff());"><img src="/static/images/close.gif" alt="close" width="12" height="12" class="gmap-close" /></a></div>'+
//					'<p class="popupHeading"> Bookmark this view</p>'+
//					'<div class="popupInnerBox" id="bmPopupBox">'+
//					'<FORM id="bmPopup" METHOD="POST" ENCTYPE="application/x-www-form-urlencoded" ACTION="/bm/test/">'+
//					'<INPUT TYPE="hidden" name="uri" value="'+bookmarkUriFunction()+'" />'+
//					'Title: <INPUT TYPE="text" name="title" value="'+bookmarkTitleFunction()+'"/><BR/>'+
//					'Tags: <INPUT TYPE="text" name="tags" value=""/><BR/>'+
//					'<INPUT TYPE=RESET VALUE="Reset"/> | <INPUT TYPE=BUTTON OnClick="bmSubmission();" VALUE="Create bookmark" /><INPUT TYPE=BUTTON VALUE="Send to..." /></FORM>'+
//					'</div>',//+
//					{ 
//				trigger: 'none', //'click', 
//				positions: 'bottom',
//				padding: 4,
//				width: 300,
//				spikeLength: 15,
//				spikeGirth: 30,
//				// cornerRadius: 20,
//				fill: 'rgba(0, 0, 0, .65)',
//				strokeWidth: .5,
//				strokeStyle: '#CCC', 
//				cssStyles: {color: '#FFF'}
//					}
//			);
//			
//		} // end bookmarkButtonClicked

		// Attach function to button in a somewhat wierd way since we want to get fresh data for the tooltip
		//$('#bookmarkButton').click(function(b){bookmarkButtonClicked();$('#bookmarkButton').btOn()});// .btOn() might not be needed if using 'now' trigger 	  

		$('#bookmarkButton').click(
			function(b){
				var postdata = { 
						uri: bookmarkUriFunction(), 
						title: bookmarkTitleFunction(),
						tags: ""
					};
//				console.log("postdata"+postdata);
//				console.log(postdata);				
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
//							console.log($('#bmUpdateForm'));
//							console.log($('#bmUpdateForm').attr('action'));
							var bmUri = $('#bmUpdateForm').attr('action')
							dia.load(bmUri, titleFormFields, function(event){
								console.log("title update ok, event:"+event);
								dia.load(bmUri+'info/ #core');
							});
//							$( this ).dialog( "close" );
						},
						"Close": function() {
							$( this ).dialog( "close" );
						}
					}					
					})
				.load('/bm/test/ #core', postdata);
				
				return false; // TODO: check return semantics of jquery .click(...)
			}	
		);
		
//		'<INPUT TYPE="hidden" name="uri" value="'+bookmarkUriFunction()+'" />'+
//		'Title: <INPUT TYPE="text" name="title" value="'+bookmarkTitleFunction()+'"/><BR/>'+
//		'Tags: <INPUT TYPE="text" name="tags" value=""/><BR/>'+

		
		// Attach function to button in the normal BeutyTips way
		$('#bookmarkListButton').bt( {
				trigger: 'click', 
				positions: 'bottom',
				ajaxPath: '/bm/u/${currentUser}/?media=text/html #EEE_UI',
				ajaxLoading: '<p>Loading bookmarks, please wait...</p>',
				padding: 10,
				margin: 5,
				width: 500,
				spikeLength: 15,
				spikeGirth: 30,
				//cornerRadius: 20,
				fill: 'rgba(0, 0, 0, .65)',  
				//fill: 'rgba(255, 255, 255, .95)',
				strokeWidth: .5,
				//strokeStyle: '#222', 
				strokeStyle: '#CCC',
				shadow: true,
				shadowColor: 'rgba(0,0,0,.4)',
			    shadowOffsetX: 5,
			    shadowOffsetY: 5,
				cssStyles: {color: '#FFF'}
				}
			);	
		
	}); // End of $(document).ready

</script>


</head>
<body>
	<div id="header">
	<table width="100%" height="100%" style="padding:0px;"><tr>
	<td width="39px" style="background:transparent;color:white;width:45"><a ref="/user/${currentUser}/"><img src="/static/images/avatars/pitr_LEGO_Town_--_doctor.png" height="35" border="0" valign="middle"/></a></td>
	<td width="25%">
 	    <p class="topleft">Logged in as:</p>	
		<div align="left"><a id="LoggedInUser" class="framing" href="/user/${currentUser}/"> ${currentUser}</a>&nbsp;&nbsp;&nbsp;&nbsp;
		<img src="/static/images/bookmark-5-24_2.png" height="35" width="35" alt="list bookmarks" id="bookmarkListButton" valign="middle">&nbsp;
		<img src="/static/images/Share.png" height="35" width="35" alt="bookmark/send" id="bookmarkButton" valign="middle">&nbsp;
	</td>
	<td><div class="ctext">Patientöversikt (Prototyp-utveckling pågår...)</div>
</div></td>
	<td width="25%"><!-- <div class="topright rtext">Patient EHR-ID: ${ehrId}</div>--> <div class="rtext" style="font-size:14px;">Anna Exempelsson<BR/>19310909<span>-</span>9000</div></td>
	<td width="80px" style="background:transparent;color:white"><img src="/static/images/avatars/anna-avatar.jpg" height="35" width="35" border="1" valign="middle"/>&nbsp;<img id="alertsymbol" src="/static/images/alertsymbol/signal.2.gif" height="32" width="32" valign="middle"/></td>
	</tr>
	</table>
	</div><!-- end header-->
	<div id="viewPane">
		<p>Loading view, please wait...</p>
	</div>
	<div id="footer">
	<table width="100%" height="100%" style="padding:0px;"><tr>
	<td width="250" style="background:transparent;">
	<a href="http://www.lio.se/Verksamheter/DC/Medicinsk-teknik-i-Ostergotland/" target="_blank"> 
	<img src="/static/images/loggor/lio_logga.png" height="35"  align="left" valign="center"/>
	</a>&nbsp;&nbsp;
	<a href="http://www.advancedinfovis.org/" target="_blank"> 
	<img src="/static/images/loggor/aiv_logga.png" height="35"  align="middle" valign="center"/>
	</a>
	</td>
	<td>
	<FORM><div class="ctext"><select id="viewSelector" name="change_type">
	<option id="timeline-m" value="iScrollLoader('../timeline-2/');" selected="true">Tidsaxel: kontakt, bltr, medicin</option>
	<option id="bodymap" value="iScrollLoader('../bodymap-1/');">Bodymap</option>
	<option id="force" value="plainLoader('/experiments/force.html');">Force layout demo</option>
	<option id="test" value="plainLoader('/experiments/');">Experiments list</option>
	</select></div></FORM>
	</td>
	<td width="250px" style="background:transparent;">
	<a href="http://www.imt.liu.se/mi/ehr/" target="_blank"> 
	<img src="/static/images/loggor/liu_logga.png" height="35" align="middle" valign="center"/>
	</a>
	<a href="http://www.novamedtech.se/" target="_blank"> 
	<img src="/static/images/loggor/nmt_logga.png" height="35" align="right" valign="center"/>
	</a>
	</td>
	</tr>
	</table>
	</div>
	</td>
	
	</div>
</body>
</html>