<#-- This is a freemarker template, that has access to at least the following variables:
  EEE_debug_callingClassName : a String containing the name of the class calling this template 
  contextAttributes: a Map<String, Object> containing the http request attributes
  requestAttributes: a Map<String, Object> containing the restlet context attributes
  httpQuery: convenience variable containing a Restlet Form object with the http query parameters
-->
<#assign EEE_title = "Contribution Builder Resource"><#include "header.html.ftl">
<h1>Contribution Builder Root Resource</h1>
<#-- ${requestAttributes.query} . -->
<h2>Adding EHR content using using a temporary Contribution Builder</h2>
<p>There are several ways to enter data into EEE. This page exemplifies 
how to use a "Contribution Builder" that acts like a temporary writing 
area that supports incremental content building and validation on demand.
<br/>See also: the documentation and the <a href="/index.html">main index page for EEE </a>
</p>
<h2>Listing active Contribution Builds</h2>
<p>A user can have several active contribution builds (CBs) for each patient and for several patients. 
It is <em>not</em> recommended to have CBs active/uncomitted longer than neccesary though. 
</p><p>To list all EHR IDs with active CBs use the URL pattern <code>/cb/{committer_id}/</code> - in your current case: 
<ul><li><a href="/cb/${currentUser}/">/cb/${currentUser}/</a></li></ul> 
<p>If there are no active CBs for user "${currentUser}", an error "404 - Not Found" will be returned.</p>

<h2>Starting a new Contribution Build</h2>
<p><em>If</em> there were no suitable existing CBs listed under <a href="/cb/${currentUser}/">/cb/${currentUser}/</a> 
as described above, then you'll need to create a new CB by sending a http POST to an URL using the pattern 
<code>/cb/{"+COMMITTER_ID+"}/{"+EHR_ID+"}/new-cb-id/</code> You are logged in as "${currentUser}", so if you wanted to add content for a patient that has EHR ID 1234567, then you'd use POST or PUT (not GET) to a URI on the format <a href="/cb/${currentUser}/1234567/new-cb-id/" onclick="alert('Clicking a link in an html page normally sends a http GET request instead of POST, and you will get a Method Not Allowed error in return. Use the POST-based ways described below instead.')">/cb/${currentUser}/1234567/new-cb-id/</a> 
</p>
<p>When POSTing to <code>new-cb-id</code> a new contribution build ID (cb-id) will get created and you will be redirected to it's URI (for example <code>/cb/${currentUser}/1234567/b734db36-30a7-43d6-b9ec-51cb389871b6/</code>) where you can add EHR content (and get information about how to do it).</p>
<p>Ways to test:  
	<ol>
	    <li>If you <a href="/ehr/">open an existing EHR</a> you may find links or buttons saying something like "generate a new contribution build" or "add content" for that specific EHR.</li>
	    <li><form enctype="application/x-www-form-urlencoded" method="POST" 
             action="/cb/${currentUser}/1234567/new-cb-id/" name="test">
             For EHR ID "1234567" 
             <input type="submit" name="__exclude__Submit" value="generate a new contribution build"></input>
             described as <input type="text" name="description" value="My Example contribution build"></input>
             by user "${currentUser}".</form></li> 
	    <li><form enctype="application/x-www-form-urlencoded" method="POST" 
             action="/cb/${currentUser}/1234567/new-cb-id/" name="test">
             For EHR ID <input disabled="true" type="text" name="id-form-field" value="example-ehr-id"></input> 
             <input disabled="true" type="submit" name="__exclude__Submit" value="generate a new contribution build"></input>
             by user "${currentUser}".</form></li> 
	</ol>

<!--
<li> Then content can be added to temporary contribution builder URIs. Using a proper REST client URIs like these could be used for PUT (if the temporary ID created was b734db36-30a7-43d6-b9ec-51cb389871b6): 
	<ul>
		<li><a href=""></a>Content can be added based on skeleton/freemarker-template files</li>	
		<li><a href=""></a>Content can be added based on previously existing content in the EHR</li>	
		<li><a href="cb/dr_who/1234567/b734db36-30a7-43d6-b9ec-51cb389871b6/56780007::ehr.us.lio.se::2;change_type=modification;lifecycle_state=incomplete">cb/dr_who/1234567/b734db36-30a7-43d6-b9ec-51cb389871b6/56780007::ehr.us.lio.se::2;change_type=modification;lifecycle_state=incomplete</a></li>
		<li><a href="cb/dr_who/1234567/b734db36-30a7-43d6-b9ec-51cb389871b6/temp-anything-new-unique-1::temp-will-be-replaced-by-current-sys-id::0;change_type=creation;lifecycle_state=incomplete">cb/dr_who/1234567/b734db36-30a7-43d6-b9ec-51cb389871b6/temp-anything-new-unique-1::temp-will-be-replaced-by-current-sys-id::0;change_type=creation;lifecycle_state=incomplete</a></li>
	</ul>
</li>
</ol>
Have a look at builds of <a href="./dr_who/">dr_who</a> meanwhile...
-->
</p>

<ul>
<li> To bootstrap test EHRs by importing data programiatically from files in local file directories, see documentation for the class se.liu.imt.mi.eee.utils.LoadPatientData</li>
</ul>
<small><PRE>
<strong>Debug info</strong>
Debug incoming call via:                 <a href="trace">trace</a>
<#include "footer.html.ftl">