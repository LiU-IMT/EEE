<#-- This is a freemarker template, that has access to at least the following variables:
  EEE_debug_callingClassName : a String containing the name of the class calling this template 
  contextAttributes: a Map<String, Object> containing the http request attributes
  requestAttributes: a Map<String, Object> containing the restlet context attributes
  httpQuery: convenience variable containing a Restlet Form object with the http query parameters
-->
<#assign EEE_title = "Bookmark Prefixed Root Resource"><#include "header.html.ftl">
<h1>Bookmark Root Resource</h1>
<p>You are logged in as ${currentUser} <a href="/bm/u/${currentUser}/">List bookmarks for ${currentUser}</a></p> 
<h2>Creating a bookmark using a form</h2>
<FORM 
	METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="./">
<p>
URI: <INPUT TYPE="text" name="uri" value="http://"/><BR/> 
Title: <INPUT TYPE="text" name="title" value="Example title"/><BR/> 
Tags: <INPUT TYPE="text" name="tags" value=""/><BR/> 
<INPUT TYPE=SUBMIT VALUE="Submit"/></p>
</FORM>
<p>Currently bookmarks are created by POSTing a form. Later support for POSTing JSON or XML might be added.</p>
<h2>Related Resources</h1>
<p>Debug incoming call via:  <a href="../trace">trace</a></p>
<#include "footer.html.ftl">