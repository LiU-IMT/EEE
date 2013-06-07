<#-- This is a freemarker template, that has access to at least the following variables:
  all_version_ids : a List<String> containing ids of openEHR VERSIONs inside a VERSIONED_OBJECT
  EEE_debug_callingClassName : a String containing the name of the class calling this template 
  contextAttributes: a Map<String, Object> containing the http request attributes
  requestAttributes: a Map<String, Object> containing the restlet context attributes
  httpQuery: convenience variable containing a Restlet Form object with the http query parameters
-->
<#assign EEE_title = "Versioned object"><#include "header.html.ftl">
<script type="application/x-javascript" src="/static/js/jquery/jquery-ui-1.8.18.custom/js/jquery-ui-1.8.18.custom.min.js"></script>
<div id="versionWrap">
<script type="text/javascript">
	var doMark;

	$(document).ready(function(){  
		console.log('entering $(document).ready in VersionedObjectResource.html.ftl');
		
		 doMark = function(b){
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
			} // end doMark

		$('.embeddedShareButton').click(doMark);

	}); // end $(document).ready
</script>
${composition_div}
</div> <!-- end div id="versionWrap" -->
<hr/>
<p>Debug incoming call via:  <a href="../trace">trace</a></p>
<#include "footer.html.ftl">