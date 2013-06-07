<#ftl ns_prefixes =
{"soap":"http://schemas.xmlsoap.org/soap/envelope/",
		"xsi":"http://www.w3.org/2001/XMLSchema-instance",
		"xsd":"http://www.w3.org/2001/XMLSchema",
		"oe":"http://schemas.openehr.org/v1" ,
		"eee":"http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd" }><#assign EEE_title = "Bodymap experiment 1 for EHR "+ehrId>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
		<meta content="text/html; charset=utf-8" http-equiv="content-type" />
		<title>${EEE_title}</title>

		<script type="text/javascript" src="/js/lib/jquery-1.5.1.min.js" ></script>
		<script type="text/javascript" src="/static/js/d3/d3-2.0.0/d3.min.js"></script>
		<script type="text/javascript" src="/static/js/d3/d3-2.0.0/d3.time.min.js"></script>

		<!-- tooltip/balloon reseources, see http://plugins.jquery.com/project/bt or the local directory /js/bt-0.9.5-rc1/DEMO/-->
		<script type="text/javascript" src="/js/lib/jquery.bt-0.9.5-rc1min.js"></script>

		<style type="text/css">
		body {
			background: white;
		}
		</style>		
</head>
<body>
<div id="EEE_UI">		
		<script type="text/javascript">
		console.log("hello world, starting bodymap-1 script tag");

		$(document).ready(function() {
			console.log("hello world, starting timeline-2 $(document).ready");
		}); // end of $(document).ready

		</script>
<img src="/static/images/med_images.png" width="1754" height="1240" alt="body organ system maps"/>
</div><!-- end of div id="EEE_UI" -->
</body>
</html>
