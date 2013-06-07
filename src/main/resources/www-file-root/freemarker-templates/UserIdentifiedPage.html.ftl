<#ftl ns_prefixes =
  {"xsi":"http://www.w3.org/2001/XMLSchema-instance",
   "xsd":"http://www.w3.org/2001/XMLSchema",
   "oe":"http://schemas.openehr.org/v1" }>
<#assign USER_ID = requestAttributes[EEEConstants.USER_ID]>
<#assign EEE_title = "User page for ${USER_ID}"><#include "header.html.ftl">
<h1>${EEE_title}</h1>
<p>You are logged in as ${currentUser} and viewing the user page of ${USER_ID} *</p>
<ul>
	<li><a href="/bm/u/${USER_ID}/">List bookmarks for '${USER_ID}'</a></li>
	<li><a href="/cb/${USER_ID}/">List patients with currently open (unfinished) contrubution builds by '${USER_ID}'</a></li>
	<li>Show <a href="/demographic/person/${USER_ID}.json">JSON file describing user '${USER_ID}'</a></li>
</ul>
<script type="text/javascript">

console.log("Entering Javascript");

$(document).ready(function(){

	$.getJSON('/demographic/person/${USER_ID}.json', function(data) {
	  console.log("data:" + data);
	  var items = [];
	
	  $.each(data, function(key, val) {
	    items.push('<li id="' + key + '">' + key + ' : ' + val + '</li>');
	  });
	
	  $('<ul/>', {
	    'class': 'my-new-list',
	    html: items.join('')
	  }).appendTo('body');
	});
	
	console.log("Exiting $(document).ready");
});
</script>

<p>TODO: Later the user page could also contain user specific:</p>
<ul>
	<li>Alert/DSS inbox</li>
	<li>Recent bookmarks</li>
	<li>Recent patients</li>
	<li>Change preferences, password etc</li>
	<li>Scheduling, coming patients, procedures etc </li>
	<li>Stored queries (created by this user)</li>
</ul>
<p>*) In this demo you are allowed to view other users user pages</p>
<#include "footer.html.ftl">