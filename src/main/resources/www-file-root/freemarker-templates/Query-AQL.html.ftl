<#assign EEE_title = "AQL Queries"><#include "header.html.ftl"><h1>${EEE_title}</h1>

<FORM 
	METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="./?dynamicTestParameter=moo">
<p>  Query: </p>
<p><textarea name="query" ROWS="15" COLS="100">
SELECT  
 v/uid/value as composition_id,
 c/name/value as composition_title, 
 c/context/start_time/value as composition_time, 
 obs/data/origin/value as measurement_time,
 obs/data/events/data/items[at0004]/value as systolic,
 obs/data/events/data/items[at0005]/value as diastolic
FROM 
 Ehr e[ehr_id/value=$current_ehr_uid] 
CONTAINS VERSION v
CONTAINS COMPOSITION c[openEHR-EHR-COMPOSITION.encounter.v1]
CONTAINS OBSERVATION obs[openEHR-EHR-OBSERVATION.blood_pressure.v1]
WHERE obs/data/events/data/items[at0004]/value/magnitude > $limit
ORDER BY c/context/start_time/value
</textarea> </p>
<p><input type="checkbox" name="debug" value="true" /> Use debug mode (query translation only). <INPUT TYPE=SUBMIT VALUE="Submit"></p>
<p>Limit: <input type="input" name="_limit" value="185" /> A variable named "_limit" in this html form. "Dynamic" variables like this one should be prefixed with _ (underscore) in HTML POST forms. In the redirected url it will be added changed to "limit", watch the end of the redirected URL after the question mark (when not using the debug checkbox above). In the AQL (and XQuery) code it should be referred to as $limit.</p> 
<p>Foo: <input type="input" name="foo" value="foo-value" /> A variable named "foo" in this html form. "Static" (stored) variables like this one are provided as XQuery variables in the translated and stored query (use the debug checkbox above to see how). Static variabes are unecessary in this html-form since the query itself can be edited to include the value instead, but may be useful in other setups where the query is not visible/editable.</p>
<p>The variable <em>$current_ehr_uid</em> is always available to the query execution when running in "single" mode. When submitting the query to this (patient specific) URI it has the value: <em>${requestAttributes.ehr_id}</em></p>
<p>Experimental return format options <select name="_media">
<option value="text/xml" selected="selected">XML</option>
<option value="text/html" >HTML (via not yet fully functional XSLT)</option></select> (HTML responses are only available if the annotation '//@Get("html")' is uncommented in the class se.liu.imt.mi.eee.ehr.res.StoredQueryAQL - otherwise XML will be returned in all cases.)
</FORM>
<hr/>
<#assign example_sha = "986fd9641b608f61d4041c058bafcfb369469c9d">
<p>Static variable changes (including query changes) will result in new parsing and storing using a new SHA-1 hash. Dynamic variables reuse already parsed and stored queries and are in many use cases preferable from a performance perspective. Dynamic URL parameters can also be given (without underscore) directly in the query URL, look at the source code of this HTML page to see how "dynamicTestParameter" is added in the form's ACTION URL. This makes it easy to include dynamic parameters in links pointing directly to already compiled/stored questions without posting a form.</p>
<p>If the query above has been submitted once already (unmodified), then it should be stored under the SHA-1 hash value ${example_sha}. Thus we can make a link to e.g. 
 <a href="/ehr:${requestAttributes.ehr_id}/q/AQL/${example_sha}/?limit=170">/ehr:${requestAttributes.ehr_id}/q/AQL/${example_sha}/?limit=170</a>
  (note the possibility to change the dynamic "limit" parameter). 

<script type="text/javascript">
// $(document).ready(function(){
//	 $("form").append('<INPUT TYPE=BUTTON disabled="true" OnClick="console.log("hi!");" VALUE="Show response below" />');
// }); // End of $(document).ready
</script>
<hr/>
<h2>Known bugs!</h1>
<p>The condition statements after the where clause currently needs to be balanced in (possibly nested) pairs by using parenthesis levels (otherwise only the first and last conditions are kept and the intermediate conditions are discarded. <br/>Example that does <b>NOT</b> work: 
<p><pre>WHERE obs/data/events/data/items[at0004]/value/magnitude > 100 AND obs/data/events/data/items[at0004]/value/magnitude > 110 AND
(obs/data/events/data/items[at0004]/value/magnitude > 120 OR obs/data/events/data/items[at0004]/value/magnitude > 130)</pre></p>
<p>...the above should instead be rewritten as...</p>
<p><pre>WHERE <b>(</b> obs/data/events/data/items[at0004]/value/magnitude > 100 AND obs/data/events/data/items[at0004]/value/magnitude > 110 <b>)</b> AND
(obs/data/events/data/items[at0004]/value/magnitude > 120 OR obs/data/events/data/items[at0004]/value/magnitude > 130)</pre></p>
<h2>A "clean" form without extra variables</h1>
<FORM 
	METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="./">
<p><textarea name="query" ROWS="15" COLS="100">
SELECT  
 v/uid/value as composition_id,
 c/name/value as composition_title, 
 c/context/start_time/value as composition_time, 
 obs/data/origin/value as measurement_time,
 obs/data/events/data/items[at0004]/value as systolic,
 obs/data/events/data/items[at0005]/value as diastolic
FROM 
 Ehr e[ehr_id/value=$current_ehr_uid] 
CONTAINS VERSION v
CONTAINS COMPOSITION c[openEHR-EHR-COMPOSITION.encounter.v1]
CONTAINS OBSERVATION obs[openEHR-EHR-OBSERVATION.blood_pressure.v1]
WHERE obs/data/events/data/items[at0004]/value/magnitude > 120
ORDER BY c/context/start_time/value
</textarea> </p>
<p><input type="checkbox" name="debug" value="true" checked="true"/> Use debug mode (query translation only). <INPUT TYPE=SUBMIT VALUE="Submit"></p>
</p>
</FORM>


<h2>Related Resources</h1>
<p>One step up in the Resource hierarchy: <a href="../">../</a></p>
<#include "footer.html.ftl">