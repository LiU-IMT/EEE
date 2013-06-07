<#-- This is a freemarker template, that has access to at least the following variables:
  EEE_debug_callingClassName : a String containing the name of the class calling this template 
  contextAttributes: a Map<String, Object> containing the http request attributes
  requestAttributes: a Map<String, Object> containing the restlet context attributes
  httpQuery: convenience variable containing a Restlet Form object with the http query parameters
-->
<#assign EEE_title = "Info about bookmark ${requestAttributes.prefix}/${bookmarkID}"><#include "header.html.ftl">
<h1>${EEE_title}</h1>
<div align="left" id="core">
<a href="${bookmarkURI}qr/"><img style="padding-left:10px" align="right" src="${bookmarkURI}qr/"></a><br/>
<span class="bmTitle">Title: <b>${bookmark.title}</b></span><br/>
<span>Short name: <b>${requestAttributes.prefix}/${bookmarkID}</b></b></span><br/>
<span >Address: <b><a href="${bookmarkURI}">${bookmarkURI}</a></b></span><br/>
<!-- AddToAny BEGIN -->
<a class="a2a_dd" href="http://www.addtoany.com/share_save?linkurl=${bookmarkURI}&linkname=c"><img src="http://static.addtoany.com/buttons/share_save_171_16.png" width="171" height="16" border="0" alt="Share"/></a>
<script type="text/javascript">
var a2a_config = a2a_config || {};
a2a_config.linkname = "${bookmark.title}";
a2a_config.linkurl = "${bookmarkURI}";
</script>
<script type="text/javascript" src="http://static.addtoany.com/menu/page.js"></script>
<!-- AddToAny END --><BR/>
<hr/>
<FORM 
	id="bmUpdateForm"
	METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="${bookmarkURI}">
<p><b>Edit</b>
<!-- Target URI: <INPUT TYPE="text" name="uri" value="${bookmark.uri}" disabled="true"/><BR/> -->
Title: <INPUT TYPE="text" name="title" value="${bookmark.title}" size="40"/>
<INPUT TYPE="hidden" name="tags" value="${bookmark.tags}"/> <!-- TODO: possibly change to visible -->  
<!--INPUT TYPE=RESET VALUE="Reset"/<INPUT TYPE=SUBMIT VALUE="Update title"/>--></p>
</FORM>
<hr/>
<small>This bookmark was originally created by user "${bookmark.committer}" at ${bookmark.dt}<BR/>
Demo Target URI: <BR/>${bookmark.uri}<BR/>In clinical deployments the target URI would in most use-cases be hidden to end users. </small>
</div> <!-- div id="core" END -->
<button id="updateButton">Update bookmark title</button>
<span id="resultFromPost"></span>
<script type="text/javascript">
	$(document).ready(function(){  
		console.log('entering $(document).ready in BookmarkInfo');
		$('#updateButton').click(function(e) {
			var titleFormFields = $('#bmUpdateForm').serializeArray();
			console.log($('#bmUpdateForm'));
			// console.log($('#bmUpdateForm').attr('action'));
			var bmUri = $('#bmUpdateForm').attr('action');
			$('#resultFromPost').load(bmUri, titleFormFields, function(event){
				console.log("title update ok, event:"+event);
				$('#resultFromPost').append(" at "+new Date());
			});
		}); // end $('updateButton').click
	}); // end $(document).ready
</script>
<hr/>
We don't want you to deactivate our demo bookmarks so the deactivation button below is deactivated...)<small><BR/> 
<FORM METHOD=POST ENCTYPE="application/x-www-form-urlencoded" ACTION="../deactivate">
<INPUT TYPE="hidden" name="activate" value="false"/><BR/> 
<INPUT TYPE=SUBMIT VALUE=" Deactivate this bookmark" disabled="true"/>
</FORM>
<hr/>
<h2>Related Resources</h1>
<p>Debug incoming call via:  <a href="../trace">trace</a></p>
<#include "footer.html.ftl">