<#ftl ns_prefixes =
  {"xsi":"http://www.w3.org/2001/XMLSchema-instance",
   "xsd":"http://www.w3.org/2001/XMLSchema",
   "oe":"http://schemas.openehr.org/v1" }>
<#assign EEE_title = "Bookmarks for ${currentUser}"><#include "header.html.ftl">
<div id="EEE_UI" style="color:black;">
<h1 class="popupHeading">${EEE_title}</h1>
<!-- TODO: move styles to external stylesheet -->
<div class="popupInnerBox">
<#list bookmarkList as bm>
<p style="font-size:10px; font-family:helvetica; border:1px solid black; padding: 2px">       
   <span style="font-size:12px; font-weight:bold;">${bm.title}</span><br/>
   By: ${bm.committer} at ${bm.dt} <br/>
   ID: <a href="/bm/test/${bm.id}/">test/${bm.id}</a> <#--if bm.tags != null-->[Tags: ${bm.tags}]<#-- >/#if --> <a href="/bm/test/${bm.id}/info/"><button>Share/QR/Edit</button></a> <a href="/bm/test/${bm.id}/"><button>Go to target</button></a> 
</p>
</#list>
</div>
</div><!-- end of div id="EEE_UI" -->
<h2>Related Resources</h1>
<p>One step up in the Resource hierarchy: <a href="../">../</a></p>
<#include "footer.html.ftl">