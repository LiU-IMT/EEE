<#assign EEE_title = "AQL Queries (multi-patient queries)"><#include "header.html.ftl"><h1>${EEE_title}</h1>

<FORM 
	METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="./?dynamicTestParameter=moo">
<p>Query:</p>
<p><textarea name="query" ROWS="15" COLS="100">
SELECT  
 v/uid/value as composition_id,
 obs/data/origin/value as measurement_time,
 obs/data/events/data/items[at0004]/value as systolic,
 e/ehr_id/value as ehr_id
FROM 
 Ehr e
CONTAINS VERSION v
CONTAINS COMPOSITION c[openEHR-EHR-COMPOSITION.encounter.v1]
CONTAINS OBSERVATION obs[openEHR-EHR-OBSERVATION.blood_pressure.v1]
WHERE obs/data/events/data/items[at0004]/value/magnitude > $limit
ORDER BY obs/data/origin/value
</textarea> </p>
<p><input type="checkbox" name="debug" value="true" /> Use debug mode (query translation only). <INPUT TYPE=SUBMIT VALUE="Submit"></p>
<p>Limit: <input type="input" name="_limit" value="185" /> A variable named "_limit" in this html form. "Dynamic" variables like this one should be prefixed with _ (underscore) in HTML POST forms. In the redirected url it will be added changed to "limit", watch the end of the redirected URL after the question mark (when not using the debug checkbox above). In the AQL (and XQuery) code it should be referred to as $limit.</p> 
<p>Foo: <input type="input" name="foo" value="foo-value" /> A variable named "foo" in this html form. "Static" (stored) variables like this one are provided as XQuery variables in the translated and stored query (use the debug checkbox above to see how). Static variabes are unecessary in this html-form since the query itself can be edited to include the value instead, but may be useful in other setups where the query is not visible/editable.</p>
<p>
  Note that the variable <em>$current_ehr_uid</em> (often used in single mode) is <strong>not</strong> set now when running "multi" mode.
</p>
</FORM>
<hr/>
<#assign example_sha = "d61258845665fafe288bd885a32f8fc0a16ae13d">
<p>Static variable changes (including query changes) will result in new parsing and storing using a new SHA-1 hash. Dynamic variables reuse already parsed and stored queries and are in many use cases preferable from a performance perspective. Dynamic URL parameters can also be given (without underscore) directly in the query URL, look at the source code of this HTML page to see how "dynamicTestParameter" is added in the form's ACTION URL. This makes it easy to include dynamic parameters in links pointing directly to already compiled/stored questions without posting a form.</p>
<p>If the query above has been submitted once already (unmodified), then it should be stored under the SHA-1 hash value ${example_sha}. Thus we can make a link to e.g. 
 <a href="/multi/ehr/q/AQL/${example_sha}/?limit=195">/multi/ehr/q/AQL/${example_sha}/?limit=195</a> 
  (note the possibility to change the dynamic "limit" parameter). 

<script type="text/javascript">
// $(document).ready(function(){
//	 $("form").append('<INPUT TYPE=BUTTON disabled="true" OnClick="console.log("hi!");" VALUE="Show response below" />');
// }); // End of $(document).ready
</script>

<h2>Related Resources</h1>
<p>One step up in the Resource hierarchy: <a href="../">../</a></p>
<#include "footer.html.ftl">