<#-- This is a freemarker template, that has access to at least the following variables:
  all_version_ids : a List<String> containing ids of openEHR VERSIONs inside a VERSIONED_OBJECT
  EEE_debug_callingClassName : a String containing the name of the class calling this template 
  contextAttributes: a Map<String, Object> containing the http request attributes
  requestAttributes: a Map<String, Object> containing the restlet context attributes
  httpQuery: convenience variable containing a Restlet Form object with the http query parameters
-->
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <meta content="text/html; charset=UTF-8" http-equiv="content-type">
  <title>Listing all version IDs of object ${requestAttributes.object_id}</title>
</head>
<body>
<h1>Listing all version IDs of object ${requestAttributes.object_id}</h1>
<ul>
<#list all_version_ids as id>
    <li><a href="../${id}">${id}</a></li>
</#list>
</ul>

<small><PRE>
<strong>Debug info</strong>
Class calling this freemarker template:  ${EEE_debug_callingClassName} 
Freemarker template filename:            ${EEE_debug_freemarkerTemplateFilename} 
ehr_id in requestAttributes:             ${requestAttributes.ehr_id}
object_id in requestAttributes:          ${requestAttributes.object_id}
<#-- ${requestAttributes.query} . -->
</PRE>
<#-- list requestAttributes.keys() as attribute>
    <li>${attribute}</li>
</#list -->
</small>
</body>
</html>