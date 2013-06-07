<#-- This is a freemarker template, that has access to at least the following variables:
  EEE_debug_callingClassName : a String containing the name of the class calling this template 
  contextAttributes: a Map<String, Object> containing the http request attributes
  requestAttributes: a Map<String, Object> containing the restlet context attributes
  httpQuery: convenience variable containing a Restlet Form object with the http query parameters
-->
<#assign EEE_title = "User Root Resource"><#include "header.html.ftl">
<h1>User Root Resource</h1>
<p>You are logged in as ${currentUser}.<br/>
<a href="/user/${currentUser}/">Go to the personal start page for user '${currentUser}'</a></p> 
<hr/>
<h2>Creating new users</h2>
<p>Later this is where you would create new users, currently that is instead done in the Java code of the startup class.</p>
<FORM 
	METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="./">
<p>
User ID: <INPUT TYPE="text" name="uri" value="not in use yet" disabled="true"/><BR/> 
Full Name: <INPUT TYPE="text" name="title" value="not in use yet" disabled="true"/><BR/> 
Tags: <INPUT TYPE="text" name="tags" value="not in use yet" disabled="true"/><BR/> 
<INPUT TYPE=SUBMIT VALUE="Submit" disabled="true"/></p>
</FORM>
<p>User accounts are created by POSTing a form. Later support for POSTing JSON or XML might be added.</p>
<#include "footer.html.ftl">