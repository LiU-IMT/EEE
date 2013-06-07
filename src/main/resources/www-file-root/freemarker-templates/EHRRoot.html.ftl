<#-- This is a freemarker template, that has access to at least the following variables:
  EEE_debug_callingClassName : a String containing the name of the class calling this template 
  contextAttributes: a Map<String, Object> containing the http request attributes
  requestAttributes: a Map<String, Object> containing the restlet context attributes
  httpQuery: convenience variable containing a Restlet Form object with the http query parameters
-->
<#assign EEE_title = "EHR Root Resource"><#include "header.html.ftl">
<h1>EHR Root Resource</h1>
<p>This page could contain user interfaces for listing or searching for EHRs. 
In a /multi-setting it could also contain links to multi-patient query interfaces.</p>

<h2>Accessing existing EHRs</h2>
<p>If the GTT-examples bundled with EEE have been loaded into the database (e.g. by running se.liu.imt.mi.eee.utils.LoadGTT.java) then some example EHRs are available:
<ul>
    <li><a href="../ehr:GTT_c10_Patient1/">GTT_c10_Patient1</a></li>
    <li><a href="../ehr:GTT_m5_Patient1_increasingCreatinine/">GTT_m5_Patient1_increasingCreatinine</a></li>
</ul></p>
<p>If the basic examples bundled with EEE have been loaded into the database (e.g. by running se.liu.imt.mi.eee.utils.LoadGTT.java) then some example EHRs are available:

<p>Other examples possibly available:
<ul>
    <li><a href="../ehr:example-ehr-id/">example-ehr-id</a></li>
    <li><a href="../ehr:example-ehr-id-2/">example-ehr-id-2</a></li>
</ul>
</p>

<h2>Creating a new EHR</h2>
<FORM 
	METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="">
<p>EHR ID suggestion: <INPUT TYPE="text" name="ehr_id" value="example-ehr-id"/> <INPUT TYPE=SUBMIT VALUE="Submit"/>
</p>
</FORM>
<h2>Related Resources</h1>
<p>Current time in the EHR database via: <a href="currentTime">currentTime</a><BR>
Debug incoming call via:  <a href="trace">trace</a></p>
<#include "footer.html.ftl">