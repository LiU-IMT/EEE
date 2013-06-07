<#-- This is a freemarker template, that has access to at least the following variables:
  EEE_debug_callingClassName : a String containing the name of the class calling this template 
  contextAttributes: a Map<String, Object> containing the http request attributes
  requestAttributes: a Map<String, Object> containing the restlet context attributes
  httpQuery: convenience variable containing a Restlet Form object with the http query parameters
-->
<#assign EEE_title = "Instance Builder Root Resource"><#include "header.html.ftl">
<h1>${EEE_title}</h1>
<p>Currently you need to run the command-line tool InstanceBuilder to generate example XML instance "skeletons" based on templates and archetypes.</p>
<p>Make sure that the EEE-classes are on the classpath and then run the java class se.liu.imt.mi.eee.utils.InstanceBuilder. Command line arguments are listed below:</p>
<p><pre>   usage: InstanceBuilder
    -a,--archetypes <arg>     archetype repository
    -g,--gen-strategy <arg>   generation strategy (default value MAXIMUM
                              other permitted values: MINIMUM, MAXIMUM_EMPTY)
    -h,--help                 print help statement
    -o,--output <arg>         output file (default output.xml)
    -t,--templates <arg>      template repository
</pre></p>
<hr/>
<h2>Creating instance skeletons  using a form</h2>
<p>Later we plan to add the ability to generate instance examples online using a form and local or remote repositories (selected by system administrators and possible to pre-cache)<p>
<FORM 
	METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="./">
<p>
Upload template file to process: <input type="file" name="somename" size="chars" disabled="true"> <BR/> 
<BR/> 
Archetype repositories to use:<BR/> 
<input type="checkbox" name="a_local" value="true" disabled="true"/>Default shared local archetype directory<BR/> 
<input type="checkbox" name="a_personal" value="true" disabled="true"/>Personal archetype development directory for user ${currentUser}<BR/> 
<input type="checkbox" name="a_ckm" value="false" disabled="false"/>openEHR Clinical knowledge manager (CKM)<BR/>
<input type="checkbox" name="a_add" value="false" disabled="false"/>...add more later<BR/>  
<BR/> 
Template repositories to use:<BR/> 
<input type="checkbox" name="t_local" value="true" disabled="true"/>Default shared local template directory<BR/> 
<input type="checkbox" name="a_personal" value="true" disabled="true"/>Personal template development directory for user ${currentUser}<BR/> 
<input type="checkbox" name="t_ckm" value="false" disabled="false"/>openEHR Clinical knowledge manager (CKM)<BR/> 
<input type="checkbox" name="t_add" value="false" disabled="false"/>...add more later<BR/>  
<INPUT TYPE=SUBMIT VALUE="Submit" disabled="true"/>
</p>
</FORM>
<#include "footer.html.ftl">