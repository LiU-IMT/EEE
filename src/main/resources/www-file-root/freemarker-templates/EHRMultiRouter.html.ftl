<#-- This is a freemarker template, that has access to at least the following variables:
  EEE_debug_callingClassName : a String containing the name of the class calling this template 
  contextAttributes: a Map<String, Object> containing the http request attributes
  requestAttributes: a Map<String, Object> containing the restlet context attributes
  httpQuery: convenience variable containing a Restlet Form object with the http query parameters
-->
<#assign EEE_title = "EHR Multi Resource"><#include "header.html.ftl">
<h1>EHR Multi Resource</h1>

<p>This is the root URI for accessing multiple EHRs simultaneously instead of using EHR-specific URI-roots.</p>
<ul>
    <li>Multi-EHR queries can be preformed <a href="./q/">using the query forms</a></li>
</ul>   

<h2>Accessing existing EHRs</h2>
<p>If the test examples bundled with EEE have been loaded into the database (e.g. by running se.liu.imt.mi.eee.utils.LoadExample.java) then some example EHRs are available:
<ul>
    <li><a href="../ehr:example-ehr-id/">ehr:example-ehr-id</a></li>
</ul>
</p>
<p>If the GTT-examples bundled with EEE have been loaded into the database (e.g. by running se.liu.imt.mi.eee.utils.LoadGTT.java) then some example EHRs are available:
<ul>
    <li><a href="../ehr:GTT_c10_Patient1/">GTT_c10_Patient1</a></li>
    <li><a href="../ehr:GTT_m5_Patient1_increasingCreatinine/">GTT_m5_Patient1_increasingCreatinine</a></li>
</ul>
</p>

<p>TODO: This page could also contain user interfaces for listing or searching for EHRs.</p>

<h2>Related Resources</h1>
<p>Current time in the EHR database via: <a href="./currentTime/">currentTime</a><BR>
Debug incoming call via:  <a href="trace">trace</a></p>
<#include "footer.html.ftl">