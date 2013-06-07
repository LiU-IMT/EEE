<#ftl ns_prefixes =
  {"xsi":"http://www.w3.org/2001/XMLSchema-instance",
   "xsd":"http://www.w3.org/2001/XMLSchema",
   "oe":"http://schemas.openehr.org/v1" }>
<#assign EEE_title = "${requestAttributes.committer_id}'s active builds for EHR ${requestAttributes.ehr_id}"><#include "header.html.ftl">
<h1>${EEE_title}</h1>

<p>

<#if (keyToCBList?size > 0) >
   <ul>
    <#list keyToCBList as vers><#assign doc=vers.value><#assign auDet=doc["oe:AUDIT_DETAILS"]>    
    <li><a href="./${vers.key}/">${vers.key}</a> -- 
   	${auDet["oe:description"]["oe:value"]} --
   	${auDet["oe:time_committed"]["oe:value"]}
   	</li>
    </#list>
  </ul>
<#else>
  User ${requestAttributes.committer_id} does not have any  active builds for EHR ${requestAttributes.ehr_id}
</#if>

</p>
<hr/>
<p>
  Add new contribution build for the EHR with id ${requestAttributes.ehr_id}
  
   <form enctype="application/x-www-form-urlencoded" method="POST" action="/cb/${requestAttributes.committer_id}/${requestAttributes.ehr_id}/new-cb-id/" name="test">
   <input type="submit" name="__exclude__Submit" value="Generate a new contribution build"></input>
    described as <input type="text" name="description" value="My Example non-default contribution build" size="35"></input> for '${requestAttributes.ehr_id}' 
   </form> 

</p>


<h2>Related Resources</h1>
<p>One step up in the Resource hierarchy: <a href="../">../</a></p>
<#include "footer.html.ftl">

