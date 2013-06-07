<#assign tempID = requestAttributes["temp-id"]>
<#assign cbID = requestAttributes["cb-id"]>
<#assign EEE_title = "Object '${cbItem.tempID}' in Contribution Build '${cbID}'">
<#include "header.html.ftl">
<h1>${EEE_title}</h1>
<p>This page shows ${EEE_title}</p>
<hr/>
<p><FORM 
	METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="../${cbItem.tempID}/"
	class="indent-x"> 
<select name="${EEEConstants.OBJECT_TYPE}">
<#list EEEConstants.VersionableObjectType as type>
  <option value="${type}" <#if type = cbItem.versionableObjectType>selected="true"</#if>>${type}</option>
</#list>
</select>, with change type 
<select name="${EEEConstants.CHANGE_TYPE}">
<#list EEEConstants.AuditChangeType as type>
  <option value="${type}" <#if type = cbItem.auditChangeType>selected="true"</#if>>${type}</option>
</#list>
</select>
and lifecycle state
<select name="${EEEConstants.LIFECYCLE_STATE}">
<#list EEEConstants.VersionLifecycleState as type>
  <option value="${type}" <#if type = cbItem.versionLifecycleState>selected="true"</#if>>${type}</option>
</#list>
</select>
...<br>
<#if cbItem.preceding_version_uid??>...based on the previous version <input size="80" name="${EEEConstants.PRECEDING_VERSION_ID}" value="${cbItem.preceding_version_uid}"></input><br/></#if>
<#if cbItem.otherInputVersionUids??>...and based on the other version <input size="120" name="${EEEConstants.OTHER_INPUT_VERSION_UIDS}" value="<#list cbItem.otherInputVersionUids as oinp>${oinp} </#list>"></input> (space separated id's)<br/></#if>
...containing the data...<br/>
<textarea name="${EEEConstants.DATA}" id="dataField" ROWS="15" COLS="100">
${cbItem.data!}
</textarea> <br/>
<input type="submit" value="Update"> (Only updates this object in the contribution build. Committing it to the EHR database then requires one more step)
</FORM>

<h2>Related Resources</h1>
<p>One step up in the Resource hierarchy: <a href="../">../</a></p>

<#include "footer.html.ftl">