# Semantic web projects 

Convert events extracted from ICS file(unstructured) to RDF and web sites(json-ld) and publish it to platform territoire.

## Technologies Used 
- [ ] Java as a programming lanaguage
- [ ] Spring boot for creating REST API
- [ ] Apache Jena for RDF modeling 
- [ ] Carbon LDP(Platform territoire)
- [ ] SPARQL -  For date extraction 
- [ ] SHACL -  For Event validation


## Features Implemented

### Convert any ICS file to rdf and publish it in Territorie Platform.

```python 
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "PREFIX schema: <http://schema.org/>\n" +
                    "ASK {  \n" +
                    " ?uri  schema:editor ?editor;\n" +
                    "        schema:endDate ?endDate;\n" +
                    "        schema:startDate ?startDate;\n" +
                    "        schema:location ?loc ;\n" +
                    "        schema:startTime ?startTime ;\n" +
                    "        schema:endTime ?endTime ;\n" +
                    "FILTER  (?editor = \"Arun\"@en && regex(str(?loc), \"%s\") &&  ?endDate = \"%s\"^^xsd:date &&  ?startDate = \"%s\"^^xsd:date && ?startTime = \"%s\"^^xsd:time && ?endTime = \"%s\"^^xsd:time )\
```