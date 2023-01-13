# Semantic web projects 

Convert events extracted from ICS file(unstructured) to RDF and web sites(json-ld) and publish it to platform territoire.

## Technologies Used 
- [ ] Java as a programming lanaguage 
- [ ] Spring boot for creating REST API
- [ ] Apache Jena for RDF modeling 
- [ ] Carbon LDP(Platform territoire)
- [ ] SPARQL -  For date extraction 
- [ ] SHACL -  For Event validation
- [ ] IntelliJ as IDE
- [ ] Curl and Postman for API checking

## Procedure to run the application

- [ ] Open the project using any of the java IDE
- [ ] Run the application 
- [ ] Go to the location http://localhost:8080/swagger-ui/index.html#/ to interact with the application 
- [ ] I used Swagger UI as front-end for interatcting with the platform



## Features Implemented

### Convert any ICS file to rdf and publish it in Territorie Platform.

Enter the location of the ics file and string identifier to identify this dataset. I used ical4j library to parse the ics file and convert it to RDF

Please check this link to see the generated rdf graphs from the ics file -  https://territoire.emse.fr/ldp/arunfinal/

![ScreenShot](./images/uploadurl.PNG)

While creating RDF graphs the event check in platform territiore for the similar events. If it is available link the both the events using owl:sameAs in both the directions.

- [ ] Information of classroom are already published in platform territoire and it is available in the format https://territoire.emse.fr/kg/emse/fayol/floorname/roomNo  where floorname can be 1ET,2ET etc and roomNo can be 104,212 etc.  I linked this url with the help of schema:location while using an  ics file downloaded from the cps2 time table. 

###### SPARQL Ask query to check if any similar events are alreeady publish in platform territoire
```python 
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX schema: <http://schema.org/>
ASK {  
 ?uri  schema:editor ?editor;
        schema:endDate ?endDate;
        schema:startDate ?startDate;
        schema:location ?loc ;
        schema:startTime ?startTime ;
        schema:endTime ?endTime ;
FILTER  (?editor = "Arun"@en && regex(str(?loc), "%s") &&  ?endDate = "%s"^^xsd:date &&  ?startDate = "%s"^^xsd:date && ?startTime = "%s"^^xsd:time && ?endTime = "%s"^^xsd:time )
 }
```     

###### If the ASK query return true then it get the URI of the similar events using SELECT query 
```python 
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX schema: <http://schema.org/>
SELECT ?url {  
 ?url  schema:editor ?editor;
        schema:endDate ?endDate;
        schema:startDate ?startDate;
        schema:location ?loc ;
        schema:startTime ?startTime ;
        schema:endTime ?endTime ;
FILTER  (?editor = "Arun"@en && regex(str(?loc), "%s") &&  ?endDate = "%s"^^xsd:date &&  ?startDate = "%s"^^xsd:date && ?startTime = "%s"^^xsd:time && ?endTime = "%s"^^xsd:time )
 }
 ```  
- [ ] Then it send a get request to the selected URL for the ETAG and the triples contained in hat URL 
- [ ] Add the URL of the new event using owl:sameAs
- [ ] Republish the triples to the platform territoire using put request
