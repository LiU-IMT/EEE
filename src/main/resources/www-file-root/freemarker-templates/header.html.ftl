<#setting url_escaping_charset='ISO-8859-1'>
<#-- the ?url freemarker command url-escapes the openEHR version id, it might be useful
     since they contain colons (::) can be interpreted as a protocol if it starts with 
     a letter. now we are preficing local URLs with ./ to avoid the problem -->
<#-- Create macros -->   
<#macro print_coded_text nnn>${nnn.value} [${nnn.defining_code.terminology_id.value}::${nnn.defining_code.code_string}]</#macro> 
<#macro print_PARTY_IDENTIFIED nnn>${nnn.name} [Type: ${nnn.external_ref.type}, Namespace: ${nnn.external_ref.namespace}, ID:${nnn.external_ref.id.value}]</#macro>
<?xml version="1.0" encoding="UTF-8"?>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
<head>
   <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
   <link rel="stylesheet" href="/static/js/jquery/jquery-ui-1.8.18.custom/css/smoothness/jquery-ui-1.8.18.custom.css" type="text/css"></link>   	
   <link rel="stylesheet" href="/static/css/style1.css" type="text/css"></link> 
   <script src="/static/js/jquery/jquery-1.7.2.min.js" type="application/x-javascript"></script>
   <title>${EEE_title!EEE}</title><#-- Text after the exclamation (!) is used if variable missing -->
</head>
<body>
<div id="pagetop" class="primary-2">LiU EEE</div>
<div class="wrapper">