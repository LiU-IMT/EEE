<#-- This is a freemarker template, that has access to at least the following variables:
  EEE_debug_callingClassName : a String containing the name of the class calling this template 
  contextAttributes: a Map<String, Object> containing the http request attributes
  requestAttributes: a Map<String, Object> containing the restlet context attributes
  httpQuery: convenience variable containing a Restlet Form object with the http query parameters
-->
<#assign EEE_title = "Bookmark Root Resource"><#include "header.html.ftl">
<h1>Bookmark Root Resource</h1>
<p>You are logged in as ${currentUser} <a href="/bm/u/${currentUser}/">List bookmarks for ${currentUser}</a></p> 
<p>Currently bookmarks are handled by prefixed pages, currently available prefixes: 
<ul>
<li><a href="./test/">/bm/test/</a></li>
<li>The prefix /u/ is used together with user name for user-specific features, for example <a href="/bm/u/${currentUser}/">/bm/u/${currentUser}</a></li>
</ul>
<p>Possibly coming prefixes: 
<ul>
<li>/p/</li> 
<li>...</li>
</ul>
<h2>Related Resources</h2>
<p>Debug incoming call via:  <a href="../trace">trace</a></p>
<#include "footer.html.ftl">