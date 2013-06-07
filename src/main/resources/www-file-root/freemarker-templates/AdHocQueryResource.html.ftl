<#assign EEE_title = "Ad Hoc XQueries (old version) with embedded AQL for EHR "+requestAttributes.ehr_id><#include "header.html.ftl">
<h1>${EEE_title}</h1>

<!-- We would like version support...
SELECT  
 comp/context/start_time/value as Date, 
 comp/name/value as Label, 
 comp/context/setting/value as Setting,
 comp/composer/name as Clinician,
 v/uid as id
FROM 
 Ehr [uid=$current_ehr_uid] 
CONTAINS VERSION v
CONTAINS COMPOSITION comp[openEHR-EHR-COMPOSITION.encounter.v1]
-->

<p><FORM 
	METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="./">
  Query: <br/>
<textarea name="query" ROWS="15" COLS="100">
<h:html xmlns:h="http://www.w3.org/1999/xhtml" xmlns:eee="http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd" xmlns="http://schemas.openehr.org/v1">
<h:head><h:title>Composition list</h:title></h:head>
<h:body>
<h:table border="1" padding="5" width="90%">
  <h:tr><h:th>Date</h:th><h:th>Label</h:th><h:th>Setting</h:th><h:th>Clinician</h:th></h:tr>
{let $aqlResult := <eee:AQL>
SELECT  
 comp/context/start_time/value as Date, 
 comp/name/value as Label, 
 comp/context/setting/value as Setting,
 comp/composer/name as Clinician
FROM 
 Ehr [uid=$current_ehr_uid] 
CONTAINS COMPOSITION comp[openEHR-EHR-COMPOSITION.encounter.v1]
</eee:AQL>
}
<h:div> Now use the variable $aqlResult... </div>
{ 
for $result in $aqlResult/results
  return <h:tr>
    <h:td>{$result/binding[@Date]/text()}</h:td>
    <h:td><h:a href="/ehr/..ehrid-variable../...missing-info...">{$result/binding[@Label]/text()}</h:a></h:td>
    <h:td>{$result/binding[@Setting]/text()}</h:td>
    <h:td>{$result/binding[@Clinician]/text()}</h:td>
  </h:tr>
}
</h:table>
</h:body></h:html>
</textarea> <br/>
<input type="checkbox" name="debug" value="true" /> Use debug mode (query translation only)<INPUT TYPE=SUBMIT VALUE="Submit">
</FORM>
</p>
<p>The variable <em>$current_ehr_uid</em> is available to the query execution; when submitting the query to this URI it has the value <em>${requestAttributes.ehr_id}</em></p>
<h2>Related Resources</h1>
<p>One step up in the Resource hierarchy: <a href="../">../</a></p>
<#include "footer.html.ftl">