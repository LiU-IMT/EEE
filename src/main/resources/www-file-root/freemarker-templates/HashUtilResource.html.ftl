<#assign EEE_title = "Hash functions and avatar generation/fetching"><#include "header.html.ftl"><h1>${EEE_title}</h1>

<FORM 
	METHOD=GET 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="./">
<p>This page allows you to encode strings as, using SHA1 or MD5 <a href="http://en.wikipedia.org/wiki/Cryptographic_hash_function">hash functions</a> that return a fixed length encoded response.</p>
<p>Data to be hashed:</p>
<p><textarea name="${EEEConstants.DATA}" ROWS="3" COLS="80">${data!"user1@test.com"}</textarea></p>
<p>Command: <select name="${EEEConstants.COMMAND}">
	 <option value="" selected="true">No command = Show examples on this page</option>
	 <option value="MD5">MD5</option>
	 <option value="SHA1">SHA1</option>
   </select>
  <INPUT TYPE="SUBMIT" VALUE="Submit"></p>
</FORM>
<hr/>
<#if data??>

<script type="text/javascript" src="/static/js/jquery/identicon5/jquery.identicon5.packed.js"></script>
<script type="text/javascript" src="/static/js/identicon_canvas_0_2.js"></script>

<script type="text/javascript">
        $(document).ready(function () {

            // Call Don parks original code
            render_identicon_canvases('identicon ')			

        	// Call idention5	
            $('.idco').identicon5({size:80});

         });        
</script>

<p>You provided the value '${data}' as data when calling this page. That data becomes  
<ul>
<li><a href="http://en.wikipedia.org/wiki/MD5">MD5</a>: ${MD5!"md5 should go here"}</li>
	<ul>
		<li> MD5 as decimal int: ${MD5_dec_int!"md5 as int should go here"}</li>
		<li> MD5 as decimal bigint: ${MD5_dec_bigint!"md5 as bigint should go here"}</li>
		<li> The decimal MD5 value depends on what conversion is selected in Java:<br/>
			<pre>${java_conversions}</pre>
		</li>
	</ul>
<li><a href="http://en.wikipedia.org/wiki/SHA-1">SHA1</a>: ${SHA1!"md5 should go here"}</li>
</ul></p>

<hr/>
 <p>MD5 hash values can also me used to generate patterns or avatars or to fetch registered <a href="http://en.gravatar.com/site/implement/images/">Gravatar images</a> as exemplified below. 
 In this demonstrator the data is first hashed on the local server and only the MD5 hash value is sent to external servers. A production EHR system may prefer to also generate images locally.<br/>
 <#assign avatUrl="http://www.gravatar.com/avatar/${MD5}?d=identicon&s=40">
 <#assign avatDesc="Gravatar with identicon fallback, size 40">
 <p><a href="${avatUrl}"><img src="${avatUrl}"></a><br/>${avatDesc}<br/><small><a href="${avatUrl}">${avatUrl}</a></small></p>
 
 <#assign avatUrl="http://www.gravatar.com/avatar/${MD5}?d=404">
 <#assign avatDesc="Gravatar without fallback (shows no image if unregistered)">
 <p><a href="${avatUrl}"><img src="${avatUrl}"></a><br/>${avatDesc}<br/><small><a href="${avatUrl}">${avatUrl}</a></small></p>

 <#assign avatUrl="http://www.gravatar.com/avatar/${MD5}?d=identicon&f=y">
 <#assign avatDesc="Gravatar with forced identicon">
 <p><a href="${avatUrl}"><img src="${avatUrl}"></a><br/>${avatDesc}<br/><small><a href="${avatUrl}">${avatUrl}</a></small></p>
 
 <p>The same avatar, but size 30 with opacity 0.5, is used as a repeated left aligned background in the section below.
<div style="background-image: url(http://www.gravatar.com/avatar/${MD5}?d=identicon&s=30&f=y); background-repeat: repeat-y; background-position: left; opacity:0.5; filter:alpha(opacity=50); /* For IE8 and earlier */">
<br/><br/><br/><br/><br/><br/><br/><br/><br/><br/><br/>
</div>
</p>


<p>The identicon implementation above, used by Gravatar is based on <a href="http://scott.sherrillmix.com/blog/blogger/wp_identicon/">Scott Sherrills PHP implementation</a> using 4x4 geometric blocks.

<hr/>
<p>Identicons can also be generated in client side canvas using Javascript. Below are some variations using 3x3 geometric blocks.</p>
 
<p><canvas title="identicon ${MD5_dec_int}" width="80" height="80"></canvas><br/>
Client side rendering (of the int ${MD5_dec_int}) using Don Park's <a href="https://github.com/donpark/identicon/tree/master/identicon-canvas">v0.2 client side code Identicons</a> –  (Javascript & HTML5 Canvas).
</p>

<p><span class="idco">${MD5}</span><br/>
Client side rendering of the first 17(?) (of 32) hexadecimal characters in the MD5 - Using <a href="http://francisshanahan.com/index.php/2010/html5-identicons-using-canvas-jquery-plugin/">Identicon5</a> – Identicons (Javascript, HTML5 Canvas & JQuery).
</p>

<p><span class="idco">${SHA1}</span><br/>
Client side rendering of the first 17(?) (of 40) hexadecimal characters in the SHA1 - Using <a href="http://francisshanahan.com/index.php/2010/html5-identicons-using-canvas-jquery-plugin/">Identicon5</a> – Identicons (Javascript, HTML5 Canvas & JQuery). </p>

<p>The client side identicon algorithms could/should probably be extended to cover the entire hash string to better avoid collisions</p>

<hr/>
<p>The avatars below this line are likely of limited use in the EHR context. A real photo or an identicon is likely easier to separate from other photos and identicons than the robots, artificial faces etc below.</p>

<#assign avatUrl="http://www.gravatar.com/avatar/${MD5}?d=wavatar&f=y">
<#assign avatDesc="Gravatar with forced wavatar">
<p><a href="${avatUrl}"><img src="${avatUrl}"></a><br/>${avatDesc}<br/><small><a href="${avatUrl}">${avatUrl}</a></small></p>

<#assign avatUrl="http://www.gravatar.com/avatar/${MD5}?d=retro&f=y">
<#assign avatDesc="Gravatar with forced retro">
<p><a href="${avatUrl}"><img src="${avatUrl}"></a><br/>${avatDesc}<br/><small><a href="${avatUrl}">${avatUrl}</a></small></p>

<#assign avatUrl="http://www.gravatar.com/avatar/${MD5}?d=monsterid&f=y">
<#assign avatDesc="Gravatar with forced monsterid">
<p><a href="${avatUrl}"><img src="${avatUrl}"></a><br/>${avatDesc}<br/><small><a href="${avatUrl}">${avatUrl}</a></small></p>

<#assign avatUrl="http://robohash.org/${MD5}?set=set1&size=80x80">
<#assign avatDesc="robohash.org using set1, size 80x80">
<p><a href="${avatUrl}"><img src="${avatUrl}"></a><br/>${avatDesc}<br/><small><a href="${avatUrl}">${avatUrl}</a></small></p>

<#assign avatUrl="http://robohash.org/${MD5}?set=set2&size=80x80">
<#assign avatDesc="robohash.org using set2, size 80x80">
<p><a href="${avatUrl}"><img src="${avatUrl}"></a><br/>${avatDesc}<br/><small><a href="${avatUrl}">${avatUrl}</a></small></p>

<#assign avatUrl="http://robohash.org/${MD5}?set=set3&size=80x80">
<#assign avatDesc="robohash.org using set3, size 80x80">
<p><a href="${avatUrl}"><img src="${avatUrl}"></a><br/>${avatDesc}<br/><small><a href="${avatUrl}">${avatUrl}</a></small></p>

<#else>
<p>If you add a parameter called data in the URL for this page (e.g. by using 'No command' in the form above) you will get it exemplified here...</p>
</#if>
<hr/>

<h2>Related Resources</h1>
<p>One step up in the Resource hierarchy: <a href="../">../</a></p>
<#include "footer.html.ftl">