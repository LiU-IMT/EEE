<#ftl ns_prefixes =
  {"soap":"http://schemas.xmlsoap.org/soap/envelope/",
   "xsd":"http://www.w3.org/2001/XMLSchema",
   "oe":"http://schemas.openehr.org/v1" ,
   "eee":"http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd",
   "xsi":"http://www.w3.org/2001/XMLSchema-instance"
    }><#assign EEE_title = "Root of EHR "+ehrId><#include "header.html.ftl">
<h1>EHR ${ehrId}</h1>
<p>Root page for the EHR with ID: ${ehrId}</p>
<table>
<tr><td>ehr_id*:</td><td>${ehrNode["eee:ehr_id"]["oe:value"]} 
<br><small>(The above is the ID of the EHR, not the patient. 
Crossmappings between EHR ID and patient ID should be fetched from a demographics service if needed.)</small></td></tr>
<tr><td>system_id*:</td><td>${ehrNode["eee:system_id"]["oe:value"]}</td></tr>
<tr><td>time_created*:</td><td>${ehrNode["eee:time_created"]["oe:value"]}</td></tr>
<tr>
 <td>Add content:</td>
 <td><ul>
  <li>List ongoing Contribuition Builds (cb) by me (${currentUser}) for this patient ID (${ehrId}): <br/><a href="/cb/${currentUser}/${ehrId}/">/cb/${currentUser}/${ehrId}/</a> if already available</li>
  <li><form enctype="application/x-www-form-urlencoded" method="POST" action="/cb/${currentUser}/${ehrId}/new-cb-id/" name="test">
   ...or <input type="submit" name="__exclude__Submit" value="Generate a new contribution build"></input>
    described as <input type="text" name="description" value="My Example non-default contribution build"></input> for '${ehrId}' 
   </form></li> 
 </ul></td>
</tr>

<tr><td>Querying:</td><td><a href="q/">Link to list of available query languages/interfaces</a></td></tr>
<tr><td>Experiments:</td><td><a href="./timeline-2/">timeline-2</a>, <a href="/experiments/timeline3.html#ehr=${ehrId}&foo=test">timeline3</a>
<br/>
        <a href="./frame-1/">frame-1</a> (try it on a tablet computer or surfpad)</td></tr>
<tr><td>Contributions:</td><td><a href="contributions/">list contributions</a></td></tr>
<tr><td>Versioned objects:</td><td>
	
<#list ehrNode["eee:versioned_objects"] as vobj>
  <#assign versionedObjectType=vobj["./@xsi:type"]?substring(14)>
  <p>The versioned <em>${versionedObjectType}</em> with id '${vobj["eee:uid"]["oe:value"]}' was created ${vobj["eee:time_created"]}<br/>
  Command URI examples:  [<a href="./${vobj["eee:uid"]["oe:value"]}/all_version_ids">List version IDs</a>] 
  Versions contained within the versioned object: 
  <ul>
    <#list vobj["eee:versions"] as vers>
    <li><b>${vers["oe:data"]["oe:name"]["oe:value"]}</b>  [<a href="./${vers["oe:uid"]["oe:value"]}?media=text/html">html</a>] [<a href="./${vers["oe:uid"]["oe:value"]}?media=text/xml">xml</a>] <br/> 
    <small>ID: <a href="./${vers["oe:uid"]["oe:value"]}">${vers["oe:uid"]["oe:value"]}</a> <#-- Comitted: ${vers["oe:commit_audit"]["oe:time_committed"]["oe:value"]} Root archetype: ${vers["oe:data"]["oe:archetype_details"]["oe:archetype_id"]["oe:value"].text} --></small>
   <form METHOD=POST ENCTYPE="application/x-www-form-urlencoded" action="/cb/${currentUser}/${ehrId}/default/new/update-version/">
   <input type="submit" value="Create"> new version of this object, with change_type
   <input type="hidden"	name="${EEEConstants.PRECEDING_VERSION_ID}" value="${vers["oe:uid"]["oe:value"]}">
   <input type="hidden"	name="${EEEConstants.TEMP_ID}" value="update-of-${vers["oe:uid"]["oe:value"]}">   
   <input type="hidden"	name="${EEEConstants.OBJECT_TYPE}" value="${versionedObjectType}">
    	<select name="${EEEConstants.CHANGE_TYPE}">
			<#list EEEConstants.AuditChangeType as type><option value="${type}" <#if type = EEEConstants.AuditChangeType_HT.modification>selected="true"</#if>>${type}</option></#list>
		</select>
		in the contribution build named "default"
	</form>
    </li>
    </#list>
  </ul>
  </p>  
</#list>  

</td></tr>
</table>
*) Marked values that have been fetched from database...
<#include "footer.html.ftl">