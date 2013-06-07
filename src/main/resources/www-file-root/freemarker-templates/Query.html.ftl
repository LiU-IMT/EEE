<#assign EEE_title = "Query possibilities for the EHR with id '${requestAttributes.ehr_id}'"><#include "header.html.ftl"><h1>${EEE_title}</h1>

<p>A query language must also be included in the URI, known examples on this server are:</p>
<ul>
<li><a href="./AQL/">AQL</a>: Archetype Query Language</li>
<li><a href="./XQuery/">XQuery</a>: An XQuery hybrid that also allows embedded AQL</li>
<!-- Temporarily disabled <li><a href="./AQL-XML-debug/">AQL-XML-debug</a>: A pseudo query language that only generates XML-based parse trees. Intended for tests and experiments</li> -->
</ul>
<p>Note that you are now using a URI for a the patient with ehr_id = ${requestAttributes.ehr_id} and only allowed to query this patient. <br/>
The 'multi' query mode allowing queries for multiple patients at the same time, e.g. for epidemiology work is at <a href="/multi/ehr/q/">/multi/ehr/q/</a>.</p>

<h2>Related Resources</h1>
<p>One step up in the Resource hierarchy: <a href="../">../</a></p>
<#include "footer.html.ftl">