<#assign tempID = requestAttributes["temp-id"]>
<#assign cbID = requestAttributes["cb-id"]>
<#assign EEE_title = "Data of '${tempID}' in Contribution Build ${cbID}'"><#include "header.html.ftl">
<style type="text/css">
div.RM {
	margin: 1px; padding: 2px;
	background:white;
	border:1px solid lightblue;
}
div.COMPOSITION {
	margin: 5px; padding: 2;
	background:blue;
	color: #000;
}
div.OBSERVATION,div.EVALUATION,div.INSTRUCTION,div.ACTION{
	margin: 2px; padding: 3;
	background:green;
	color: #000;
}
.DV_CODED_TEXT{
	border:7px solid purple;
}
span {
	margin: 1px; padding: 1px;
	background:white;
	border:1px solid white;
}
span.oe_nodename {
	margin: 1px; padding: 1px;
    color: black;
	background: lightgray;
    font-size:70%;
}
span[oe_nodename=language] {
	background: pink;
	/* visibility: hidden; */
	display:none;
}
span.oe_leaf {
	margin: 1px; padding: 1px;
    color: blue;
	background: lightgray;
    font-size:100%;
}
.hilite {
	border-color:yellow;
    background:yellow
}
</style>
<p>This page handles the data contents of '${tempID}' in the Contribution Build  '${cbID}' for EHR '${requestAttributes.ehr_id}' by user '${requestAttributes.committer_id}'</p>

<p><FORM METHOD=POST 
	ENCTYPE="application/x-www-form-urlencoded" 
	ACTION="#">
<input type="button" id="submit-data" name="mybutton" value="Submit update!" >
  Data content that will get updated in the current object (named '${tempID}'): <br/>
<textarea name="${EEEConstants.DATA}" id="data" ROWS="15" COLS="100">
${data.data}
</textarea> <br/>
<!-- INPUT TYPE=SUBMIT VALUE="Submit update" -->
</FORM>

<p><div id="output-holder"></div></p>

<script type="text/javascript">
$(document).ready(function() {
	
	function sendingComplete() { 
	    alert("Update complete!");
	}

	function sendData() { 
		console.log("trying to send: "+document.getElementById('data').value);
	    $.ajax({
	        url: "#",
	        type: "POST",
	        dataType: "xml",
	        data: document.getElementById('data').value,
	        complete: sendingComplete,
	        contentType: "text/xml; charset=\"utf-8\""
	    });
	}
	
	$("#submit-data").click( function(e){
		console.log("#submit-data clicked");
		//alert('Before: '+document.getElementById('data').value);
		sendData();
	} ); 
	
	$("#data").change( function(e){
		console.log("#data changed...");
	} ); 
	
	$("#data").keyup( function(e){
		console.log("#data changed keyup");
		convertXMLtoHTML();
	} );
	
	function openEHRpathGenerator(node){
		if (!node) return "";
		if (node.nodeName == "#document") return "";
		$node = $(node);
		var out = "/";
		out += node.nodeName;
		var ani = $node.attr("archetype_node_id");
		if (ani) out += "["+ani+"]";

		return openEHRpathGenerator(node.parentNode) + out;
	}
	
	function recursiveDivGenerator(sourcenode_in, targetnode_in){
		var sourcenode = $(sourcenode_in);
		var targetnode = $(targetnode_in);
		var ani = sourcenode.attr("archetype_node_id");
		var xsi = sourcenode.attr("xsi:type");
		var name = sourcenode.find("name:first value:first").text();
		//console.log("NAME: "+name) 
		var nodeTypeOut = "span";
		if(ani){
			nodeTypeOut = "div";
		}
		
		var nodecontent = '<'+nodeTypeOut+' class="RM'		
		nodecontent +='" oe_nodename="'+sourcenode_in.nodeName+'">'+'<'+nodeTypeOut+'>'
		
		
		
		//var newTarget = targetnode.append(nodecontent);
		var newTarget = $(nodecontent).appendTo(targetnode);
		if(xsi){
			newTarget.addClass(sourcenode.attr("xsi:type"))
		}
		if(ani){
			newTarget.prepend(' ['+ani+']');
		}
		if(name){
			newTarget.prepend(name);
		}

		
		// Attach connection  to sourcnode
		$(newTarget).data("sourcenode", sourcenode_in);
		$(newTarget).hover(
				function(e){
					e.stopPropagation();
					$(this).addClass("hilite");
				},
				function(e){
					//e.stopPropagation();
					$(this).removeClass("hilite");
				}
		);
		
		$(newTarget).click(
				function(e){
					e.stopPropagation()
					var sn = $(this).data("sourcenode");
					var $sn = $(sn);
					console.log(openEHRpathGenerator(sn));
					alert("Path:\n"+ openEHRpathGenerator(sn)+
						  "\nContent:\n"+ sn);
				}
		);		
		//console.log("Same? "+newTarget == targetnode)	
		var childcount = sourcenode.children().length;
		
		
		if (childcount == 0) {
			newTarget.addClass('oe_leaf');
			console.log(sourcenode_in.nodeName + "//" +sourcenode_in.className+": text="+sourcenode.text());
			newTarget.text( sourcenode.text() );
		}
		else {
			newTarget.append('<span class="oe_nodename">'+sourcenode_in.nodeName+'</span> ' );
			console.log(sourcenode_in.nodeName + "/" +sourcenode_in.className+": Cc="+childcount +" archetype_node_id="+sourcenode.attr("archetype_node_id")+" type="+sourcenode.attr("xsi:type"));
			sourcenode.children(":not(:empty)").each(function(){ // TODO: Add condition to avoid recursing LOCATABLE "name"
				recursiveDivGenerator(this, newTarget);
			});
		}
//		$(targetnode).append('<div test="'+sourcenode.nodeName+'"><p> '+sourcenode.text+' </p><div>');
		    
		
	};
	
	function convertXMLtoHTML(){
		// Find old
		var target = $("#output-holder");
		
		// Remove it's content
		target.empty();

		var xmlstring = document.getElementById('data').value;
		var xmldocument = $.parseXML(xmlstring);
		var xml = $(xmldocument);
		//var comp = xml.find("composition");
		var comp = xml.contents();
		if (comp == null) comp = xml.find("data");
		var folder = xml.find("folder");
		console.log(comp);
		
		if(comp.length){ // Enters here if comp.length is not zero
			console.log("Composition name: " + comp.find("name:first > value").text()+" -- "+comp.length);			
			target.html(recursiveDivGenerator(comp, target));			
		} else {
			console.log("XML did not contain 'data' or 'composition' nodes: "+xml);
			console.log(xml);
		}
		
		if(folder.length){
			console.log("Folder name:" + folder.find("name:first > value").text()+" -- "+folder.length);
		}
		
//		xmldocument.find().each(function() {
//			console.log("Type: "+$(this));
//			console.log("Composition name: "+$(this).find("name:first > value").text())
//		  }
//		)
	}
	
    //alert("hello world, document ready");
	console.log("hello world, document ready");
	convertXMLtoHTML();
	
}); // End of $(document).ready
    
</script>

<h2>Related Resources!</h1>
<p>One step up in the Resource hierarchy: <a href="../">../</a></p><#include "footer.html.ftl">
