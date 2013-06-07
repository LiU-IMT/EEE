// Workaround for IE (console for logging does not exist unless in Developer Tools for IE8) 
if (!window.console) console = {log: function() {}, debug: function(){}};

/** Creates simple JavaScript objects from XML leaf nodes. Does NOT work for entire XML branches */
function objectifyAQLXmlResult(xml) {
	console.log("ObjectifyAQLXmlResult, xml is xml: "+$.isXMLDoc(xml));
	var responseData = [ ];
	// jQuery XML selectors work differently in different browsers, see http://bugs.jquery.com/ticket/4208
	var resultSelector = "result";
	var bindingSelector = "binding";
	console.log("--1");
	if ($.browser.mozilla) { // Detects browser
		// Firefox (works if XML prefix set as xmlns:res="http://www.imt.liu.se/mi/ehr/2010/xml-result-v1#" )
		console.log("EEE-utils.js - ObjectifyAQLXmlResult: Firefox detected");
		resultSelector = "res\\:result";
		bindingSelector = "res\\:binding";
	} else if ($.browser.msie) {
		console.log("EEE-utils.js - ObjectifyAQLXmlResult: msie detected");
		// From comments to http://bugs.jquery.com/ticket/10720
		resultSelector = 'res\\:result, result';
		bindingSelector = 'res\\:binding, binding';
	}
	console.log("--2");
	// TODO: check possible performance boost at http://www.steveworkman.com/html5-2/javascript/2011/improving-javascript-xml-node-finding-performance-by-2000/

	$(xml).find(resultSelector).each(function(index, elem) {
		// First converts to JSON then to Javascript-object
		var JSONifiedResult = "{" 
		var myBindings = $(elem).find(bindingSelector);
		myBindings.each(function(index2, elem2) {
			JSONifiedResult = JSONifiedResult + ' "'+$(elem2).attr('name')+'": "';
			JSONifiedResult = JSONifiedResult + $.trim($(elem2).text())+'", ';
			console.log('  --- '+$(elem2).attr('name')+'='+$(elem2).text().trim());
		});
		JSONifiedResult = JSONifiedResult.substring(0, JSONifiedResult.length-2) + ' }'
		var myObject = jQuery.parseJSON(JSONifiedResult);
		responseData.push(myObject);
	});
	console.log("--3");
	return responseData;
} // end ObjectifyAQLXmlResult

function configurableAQLXmlResultHandler(xml) {
	console.log("configurableAQLXmlResultHandler, xml is xml: "+$.isXMLDoc(xml));
	var responseData = [ ];
	// jQuery XML selectors work differently in different browsers, see http://bugs.jquery.com/ticket/4208
	var resultSelector = "result";
	var bindingSelector = "binding";
	//console.log("-++-1");
	if ($.browser.mozilla) { // Detects browser
		// Firefox (works if XML prefix set as xmlns:res="http://www.imt.liu.se/mi/ehr/2010/xml-result-v1#" )
		console.log("EEE-utils.js - ObjectifyAQLXmlResult: Firefox detected");
		resultSelector = "res\\:result";
		bindingSelector = "res\\:binding";
	} else if ($.browser.msie) {
		console.log("EEE-utils.js - ObjectifyAQLXmlResult: msie detected");
		// From comments to http://bugs.jquery.com/ticket/10720
		resultSelector = 'res\\:result, result';
		bindingSelector = 'res\\:binding, binding';
	}
	//console.log("-++-2");
	// TODO: check possible performance boost at http://www.steveworkman.com/html5-2/javascript/2011/improving-javascript-xml-node-finding-performance-by-2000/

	$(xml).find(resultSelector).each(function(index, elem) {
		// First converts to JSON then to Javascript-object
		var JSONifiedResult = "{" 
		var myBindings = $(elem).find(bindingSelector);
		myBindings.each(function(index2, elem2) {
			var varName = $(elem2).attr('name');
			JSONifiedResult = JSONifiedResult + ' "'+varName+'": "';
			if (varName.indexOf("xml__") < 0) {
				JSONifiedResult = JSONifiedResult + $.trim($(elem2).text())+'", ';
				//console.log('  --- '+$(elem2).attr('name')+'='+$(elem2).text().trim());		
			} else {
				// The variable name starts with xml__ thus store raw XML snippet in JSON
				// console.log('  --###-- xml__'+varName.indexOf("xml__"));
				JSONifiedResult = JSONifiedResult + $.trim(elem2)+'", ';				
			}
		});
		JSONifiedResult = JSONifiedResult.substring(0, JSONifiedResult.length-2) + ' }'
		var myObject = jQuery.parseJSON(JSONifiedResult);
		responseData.push(myObject);
	});
	//console.log("-++-3");
	return responseData;
} // end ObjectifyAQLXmlResult

//TODO: possibly different behaviours for javascript string functions in different browsers?
function findSha(str){
	var start = str.indexOf("/q/AQL/") + 7;
	var sha = str.substring(start, start + 40); // sha:s are always 40 characters
	console.log("Found SHA="+sha+" \n...from the response headers below...\n"+str);
	return sha;
}

/**
 * Makes a POST request to the server for the AQL query q.aql
 *  
 * */
function tryPostAQLRequest(q, ehrId, callbackFunction){
	// Using freemarker variable ${ehrId} below
	var postRequest = $.ajax("/ehr:"+ehrId+"/q/AQL/", {// ajax options
		type: "POST",
		data: {query: q.aql, debug: "false"},
		dataType: "xml",
		success: function(xml){
			// Returning from a server redirect. No need to GET again.
			var sha1 = findSha(postRequest.getResponseHeader("Content-Location")); // using the (server-side injected) Content-Location HTTP header to get the redirect URL all the way to jQuery (it is invisibly handled by the browser)
			if (sha1 != q.sha){
				alert("The new SHA-1 value for this query (named '"+q+"') is: \n" + sha1 + "\n\n...if you have finished developing your AQL query, then use this new SHA-1 value in your call in order to reduce the number of POST requests requiring query parsing etc." +
						"\n\nThe old SHA-1 value was:\n" + q.sha);
			}
			callbackFunction(q, xml);
		}, // end function(xml)
		error: function(){
			var errmess = "POSTing " + "/ehr:"+ehrId+"/q/AQL/" + " failed.";
			console.log(errmess);
			alert(errmess);
			// FIXME: what to do here?
			callbackFunction(q, "");
		}

	}// end ajax options
	);
}

/** q is expected to be an object with the following 
		  Input:
		   q.query is a string with the AQL query
		   q.sha is the sha-1 string of the query combined with static variables as reported by query interface
		  Results:
		   will call the callbackFunction(q, xml) asynchronously later when the http response from the server is returned (where xml is the retruned xml)
 **/
function executeQuery(q, ehrId, callbackFunction) {
	if (q.sha == ""){ // if empty string we will not use stored query
		tryPostAQLRequest(q, ehrId, callbackFunction);
	}
	else {
		var shaUrl = "/ehr:"+ehrId+"/q/AQL/"+q.sha+"/";
		console.log("live-DB mode; Posting query: "+q+" to "+shaUrl);

		// First try get (Let's hope the queries are already parsed and stored) 
		var request = $.ajax(shaUrl,{
			success: function(){
				console.log("!!! (1) Succesful response for "+q+":"+request.responseXML)
				callbackFunction(q, request.responseXML);
			}, 
			error: function(){
				console.log("Page not found. Will try to POST request instead...");
				tryPostAQLRequest(q, ehrId, callbackFunction);
			}
		});
	}
}
//--- Utility function from http://stackoverflow.com/questions/1131630/javascript-jquery-param-inverse-function/1131651#1131651 ---
function queryStringToHash(query) {
	var query_string = {};
	var vars = query.split("&");
	for (var i=0;i<vars.length;i++) {
		var pair = vars[i].split("=");
		pair[0] = decodeURIComponent(pair[0]);
		pair[1] = decodeURIComponent(pair[1]);
		// If first entry with this name
		if (typeof query_string[pair[0]] === "undefined") {
			query_string[pair[0]] = pair[1];
			// If second entry with this name
		} else if (typeof query_string[pair[0]] === "string") {
			var arr = [ query_string[pair[0]], pair[1] ];
			query_string[pair[0]] = arr;
			// If third or later entry with this name
		} else {
			query_string[pair[0]].push(pair[1]);
		}
	} 
	return query_string;
}
