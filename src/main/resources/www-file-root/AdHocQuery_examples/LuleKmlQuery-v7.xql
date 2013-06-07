<kml:kml xmlns:kml="http://www.opengis.net/kml/2.2" 
	xmlns:gx="http://www.google.com/kml/ext/2.2" 
	xmlns:atom="http://www.w3.org/2005/Atom" 
	xmlns:eee="http://www.imt.liu.se/mi/ehr/2010/EEE-v1.xsd" 
	xmlns:v1="http://schemas.openehr.org/v1" 
	xmlns="http://schemas.openehr.org/v1">
<kml:Document>
	<kml:name>SoS LuleDemo</kml:name>

	<kml:Style xmlns="http://www.opengis.net/kml/2.2"  id="sn_ylw-pushpin">
		<IconStyle>
			<scale>1.1</scale>
			<Icon>
				<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>
			</Icon>
			<hotSpot x="20" y="2" xunits="pixels" yunits="pixels"/>
		</IconStyle>
		<LabelStyle>
			<color>ffcccccc</color>
			<scale>0.9</scale>
		</LabelStyle>
	</kml:Style>
	<kml:Style xmlns="http://www.opengis.net/kml/2.2"  id="sh_ylw-pushpin">
		<IconStyle>
			<scale>1.3</scale>
			<Icon>
				<href>http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png</href>
			</Icon>
			<hotSpot x="20" y="2" xunits="pixels" yunits="pixels"/>
		</IconStyle>
		<LabelStyle>
			<color>ffcccccc</color>
			<scale>0.9</scale>
		</LabelStyle>
	</kml:Style>
	<kml:StyleMap xmlns="http://www.opengis.net/kml/2.2" id="msn_ylw-pushpin">
		<Pair>
			<key>normal</key>
			<styleUrl>#sn_ylw-pushpin</styleUrl>
		</Pair>
		<Pair>
			<key>highlight</key>
			<styleUrl>#sh_ylw-pushpin</styleUrl>
		</Pair>
	</kml:StyleMap>
	<kml:StyleMap xmlns="http://www.opengis.net/kml/2.2" id="msn_B">
		<Pair>
			<key>normal</key>
			<styleUrl>#sn_B</styleUrl>
		</Pair>
		<Pair>
			<key>highlight</key>
			<styleUrl>#sh_B</styleUrl>
		</Pair>
	</kml:StyleMap>
	<kml:Style xmlns="http://www.opengis.net/kml/2.2" id="sn_B">
		<IconStyle>
			<color>b3ffffff</color>
			<scale>0.8</scale>
			<Icon>
				<href>http://maps.google.com/mapfiles/kml/paddle/B.png</href>
			</Icon>
			<hotSpot x="32" y="1" xunits="pixels" yunits="pixels"/>
		</IconStyle>
		<LabelStyle>
			<color>ffcccccc</color>
			<scale>0.9</scale>
		</LabelStyle>
		<ListStyle>
			<ItemIcon>
				<href>http://maps.google.com/mapfiles/kml/paddle/B-lv.png</href>
			</ItemIcon>
		</ListStyle>
	</kml:Style>
	<kml:Style xmlns="http://www.opengis.net/kml/2.2" id="sh_B">
		<IconStyle>
			<color>b3ffffff</color>
			<scale>0.945455</scale>
			<Icon>
				<href>http://maps.google.com/mapfiles/kml/paddle/B.png</href>
			</Icon>
			<hotSpot x="32" y="1" xunits="pixels" yunits="pixels"/>
		</IconStyle>
		<LabelStyle>
			<color>ffcccccc</color>
			<scale>0.9</scale>
		</LabelStyle>
		<ListStyle>
			<ItemIcon>
				<href>http://maps.google.com/mapfiles/kml/paddle/B-lv.png</href>
			</ItemIcon>
		</ListStyle>
	</kml:Style>

	<kml:Folder>
	<kml:Name>Anna</kml:Name>

	<kml:Folder>
		<kml:Name>Anteckningar</kml:Name>

{ 
let $aqlFiltered := //*[ends-with(string(./attribute::xsi:type),"COMPOSITION")]  
for $version in $aqlFiltered/.. 
  let $comp := $version/data
  order by $comp/context/start_time/value/text()
  return <kml:Placemark id="{$version/uid/value/text()}">
		<kml:name>{$comp/name/value/text()}</kml:name>
		<kml:description> 
		 Link: http://localhost:8182/ehr/AnnaTest-v3/{$version/uid/value/text()}
 		 Time: {$comp/context/start_time/value/text()}
		</kml:description>
		<kml:styleUrl>#msn_ylw-pushpin</kml:styleUrl>
		<kml:Point>
			<kml:coordinates>{14.2+((xs:dateTime($comp/context/start_time/value/text()) - xs:dateTime('2000-01-01T00:00:00')) div xs:dayTimeDuration('PT24H')) div 365},65.01,0</kml:coordinates>
										
</kml:Point>
	</kml:Placemark>
}
	</kml:Folder>


	<kml:Folder>
		<kml:Name>Blodtrycksnoteringar</kml:Name>

{ let $aqlFiltered := for $ehrRoot in //*
let $c:=$ehrRoot//*[ends-with(string(./attribute::xsi:type),"COMPOSITION")]
let $o:=$c//*[ends-with(string(./attribute::xsi:type),"OBSERVATION") and @archetype_node_id = "openEHR-EHR-OBSERVATION.blood_pressure.v1"]
return $o  

for $version in $aqlFiltered/../.. 
  let $comp := $version/data
  order by $comp/context/start_time/value/text()
  return
	<kml:Placemark id="$version/uid/value/text()">
		<kml:name>Bt. {$comp/*[@archetype_node_id = "openEHR-EHR-OBSERVATION.blood_pressure.v1"]/data/events/data/items[@archetype_node_id="at0004"]/value/magnitude/text()} / {$comp/*[@archetype_node_id = "openEHR-EHR-OBSERVATION.blood_pressure.v1"]/data/events/data/items[@archetype_node_id="at0005"]/value/magnitude/text()}</kml:name>
		<kml:description>
		 {$comp/*[@archetype_node_id = "openEHR-EHR-OBSERVATION.blood_pressure.v1"]/data/origin/value/text()}
 		 
		 {$comp/*[@archetype_node_id = "openEHR-EHR-OBSERVATION.blood_pressure.v1"]/name/value/text()}
		 
		 Systoliskt: {$comp/*[@archetype_node_id = "openEHR-EHR-OBSERVATION.blood_pressure.v1"]/data/events/data/items[@archetype_node_id="at0004"]/value/magnitude/text()}
		 Diastoliskt: {$comp/*[@archetype_node_id = "openEHR-EHR-OBSERVATION.blood_pressure.v1"]/data/events/data/items[@archetype_node_id="at0005"]/value/magnitude/text()}		 
		 
		 Se besšk: http://localhost:8182/ehr/AnnaTest-v3/{$version/uid/value/text()}
		</kml:description>
		<kml:styleUrl>#msn_B</kml:styleUrl>
		<kml:Point>
			<kml:coordinates>{14.2+((xs:dateTime($comp/*[@archetype_node_id = "openEHR-EHR-OBSERVATION.blood_pressure.v1"]/data/origin/value/text()) - xs:dateTime('2000-01-01T00:00:00')) div xs:dayTimeDuration('PT24H')) div 365}, {65.0+$comp/*[@archetype_node_id = "openEHR-EHR-OBSERVATION.blood_pressure.v1"]/data/events/data/items[@archetype_node_id="at0004"]/value/magnitude/text() div 10000},0</kml:coordinates>
		</kml:Point>
	</kml:Placemark>
}
	</kml:Folder>


	<kml:Folder>
		<kml:Name>Blodtryckskurva</kml:Name>
	</kml:Folder>
</kml:Folder>
	
</kml:Document>
</kml:kml>

