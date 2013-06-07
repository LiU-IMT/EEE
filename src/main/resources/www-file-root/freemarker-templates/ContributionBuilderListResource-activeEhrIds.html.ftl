<#assign EEE_title = "CB List of EHRs that have builds by user ${requestAttributes.committer_id}"><#include "header.html.ftl">
<h1>${EEE_title}</h1>
<p>The EHRs listed below (if any) have active (not yet committed) contribution builds made by the user "${requestAttributes.committer_id}":</p>
  <ul>
    <#list keyToCBList as vers>
    <li><a href="./${vers}/">${vers}</a></li>
    </#list>
  </ul>
<p>Click an EHR ID in the list above to see the related contribution builds.</p>

<h2>Related Resources</h1>
<p>One step up in the Resource hierarchy: <a href="../">../</a></p>
<#include "footer.html.ftl">