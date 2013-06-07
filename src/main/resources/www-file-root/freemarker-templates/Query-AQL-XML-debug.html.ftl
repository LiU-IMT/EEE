<#assign EEE_title = "AQL Query parse tree generator"><#include "header.html.ftl"><h1>${EEE_title}</h1>
<p>Please note that this is a pseoudo-query-language for debugging ans AQL experimentation purposes. The query will only generate a parse-tree in XML. No EHR data will be fetched. <a href="../">Step up one directory level</a> if you want to list query languages.</p>
<p><FORM 
	METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="./?testparameter=moo">
  Query: <br/>
<textarea name="query" ROWS="15" COLS="100">
SELECT  
 v/uid/value as composition_id,
 c/name/value as composition_title, 
 c/context/start_time/value as composition_time, 
 obs/data/origin/value as measurement_time,
 obs/data/events/data/items[at0004]/value as systolic,
 obs/data/events/data/items[at0005]/value as diastolic
FROM 
 Ehr [uid=$current_ehr_uid] 
CONTAINS VERSION v
CONTAINS COMPOSITION c[openEHR-EHR-COMPOSITION.encounter.v1]
CONTAINS OBSERVATION obs[openEHR-EHR-OBSERVATION.blood_pressure.v1]
WHERE obs/data/events/data/items[at0004]/value/magnitude > 185
ORDER BY  c/context/start_time/value
</textarea> <br/>
<input type="checkbox" name="debug" value="true" /> Use debug mode (query translation only) <br/>
<br/>
<INPUT TYPE=SUBMIT VALUE="Submit">
</FORM>
</p>
<script type="text/javascript">
$(document).ready(function(){
	$("form").append('<INPUT TYPE=BUTTON disabled="true" OnClick="console.log("hi!");" VALUE="Show response below" />');
}); // End of $(document).ready
</script>

<p>The variable <em>$current_ehr_uid</em> is available to the query execution; when submitting the query to this URI it has the value <em>${requestAttributes.ehr_id}</em></p>
<h2>Related Resources</h1>
<p>One step up in the Resource hierarchy: <a href="../">../</a></p>
<#include "footer.html.ftl">