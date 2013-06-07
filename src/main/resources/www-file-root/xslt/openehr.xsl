<?xml version="1.0" encoding="utf-8"?><!-- DWXMLSource="file:///C|/Users/Henning/Desktop/Liu/ProjektGrupp/XML-filer/composition-admission-080315.xml" -->
<!DOCTYPE xsl:stylesheet  [
	<!ENTITY nbsp   "&#160;">
	<!ENTITY copy   "&#169;">
	<!ENTITY reg    "&#174;">
	<!ENTITY trade  "&#8482;">
	<!ENTITY mdash  "&#8212;">
	<!ENTITY ldquo  "&#8220;">
	<!ENTITY rdquo  "&#8221;"> 
	<!ENTITY pound  "&#163;">
	<!ENTITY yen    "&#165;">
	<!ENTITY euro   "&#8364;">
]>
<xsl:stylesheet version="1.0" xmlns:eeeq="http://www.imt.liu.se/mi/ehr/2010/EEEQuery" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:oe="http://schemas.openehr.org/v1"> 
<xsl:output method="html" doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN" doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>
<xsl:template match="/">
<html xmlns="http://www.w3.org/1999/xhtml">

<body>
<h2>
<xsl:value-of select="oe:resultset/oe:data/oe:name"/>
</h2>
<p>Ansvarig läkare: <xsl:value-of select="oe:resultset/oe:data/oe:composer/oe:name"/> </p>
<p>Tidpunkt: <xsl:value-of select="oe:resultset/oe:data/oe:context/oe:start_time/oe:value"/></p>
<p>Plats: <xsl:value-of select="oe:resultset/oe:data/oe:context/oe:setting/oe:value"/></p>
<p>Sjukvårdsinrättning: <xsl:value-of select="oe:resultset/oe:data/oe:context/oe:health_care_facility/oe:name"/></p>

<h2>Innehåll</h2>
<p><xsl:value-of select="oe:resultset/oe:data/oe:content/oe:name/oe:value"/></p>
<p><xsl:value-of select="oe:resultset/oe:data/oe:content/oe:protocol/oe:items/oe:name/oe:value"/>: <xsl:value-of select="oe:resultset/oe:data/oe:content/oe:protocol/oe:items/oe:value/oe:value"/></p>
<p><xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:events/oe:data/oe:items/oe:name/oe:value"/>: <xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:events/oe:data/oe:items/oe:value/oe:magnitude"/> <xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:events/oe:data/oe:items/oe:value/oe:units"/></p>
<p><xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:events/oe:data/oe:items[2]/oe:name/oe:value"/>: <xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:events/oe:data/oe:items[2]/oe:value/oe:magnitude"/> <xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:events/oe:data/oe:items[2]/oe:value/oe:units"/></p>
<p><xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:items/oe:name/oe:value"/>: <xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:items/oe:value/oe:value"/></p>
<p><xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:items[2]/oe:name/oe:value"/>: 
<xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:items/oe:items/oe:value/oe:value"/></p>
<p><xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:items[3]/oe:name/oe:value"/>:  <xsl:value-of select="oe:resultset/oe:data/oe:content/oe:data/oe:items[3]/oe:value/oe:value"/></p>

</body>
</html>
</xsl:template>
</xsl:stylesheet>