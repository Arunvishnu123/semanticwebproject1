package com.example.demo.PublishCalendarLinkedDataPlatform;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.Property;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Scanner;

@Service
public class PublishICSCalendarData {

    public void  ICSCalendarDataPublisher(String fileLocation, String uniqueIdentifier) throws IOException, InterruptedException, ParserException {
        FileInputStream fin = new FileInputStream(fileLocation);
        CalendarBuilder builder = new CalendarBuilder();
        Calendar calendar = builder.build(fin);

        Integer id = 0;

        String stringIdentifier = uniqueIdentifier ;

        for (Iterator i = calendar.getComponents().iterator(); i.hasNext(); ) {
            Model model = ModelFactory.createDefaultModel();

            String schema = "http://schema.org/";
            model.setNsPrefix("schema", schema);

            String startDate ;
            String endDate ;
            String startTime;
            String endTime ;
            String location = null;


            Component component = (Component) i.next();
            System.out.println("Component [" + component.getName() + "]");
            for (int j = id; j < id + 1; j++) {
                id = j + 1;
                break;
            }
            String baseUrl =  "https://territoire.emse.fr/ldp/arunfinal/" ;


            Resource iri = model.createResource("/" + stringIdentifier + id );


            iri.addProperty(ResourceFactory.createProperty(schema + "identifier"), model.createTypedLiteral(component.getProperty(Property.UID).get().getValue(), XSDDatatype.XSDstring));
            iri.addProperty(ResourceFactory.createProperty(schema + "serialNumber"), model.createTypedLiteral( stringIdentifier + id , XSDDatatype.XSDstring));
            iri.addProperty(RDF.type,ResourceFactory.createProperty(schema + "Event"));
            if(component.getProperty(Property.DESCRIPTION).get().getValue().contains("CPS")){
                iri.addProperty(RDF.type,ResourceFactory.createProperty(schema + "CourseInstance"));
            }

            if(component.getProperty(Property.LOCATION).get().getValue().contains("EMSE")){
                iri.addProperty(ResourceFactory.createProperty(schema + "organizer"),model.createTypedLiteral("EMSE", XSDDatatype.XSDstring));
                String lines[] = component.getProperty(Property.DESCRIPTION).get().getValue().split("\\r?\\n");
                if(component.getProperty(Property.DESCRIPTION).get().getValue().contains("M1")){

                    iri.addProperty(ResourceFactory.createProperty(schema + "instructor"), model.createLiteral(lines[4],"en"));
                }else{
                    iri.addProperty(ResourceFactory.createProperty(schema + "instructor"), model.createLiteral(lines[3],"en"));
                }

                try {
                    String[] parts = component.getProperty(Property.LOCATION).get().getValue().split(",");
                    iri.addProperty(ResourceFactory.createProperty(schema + "address"), component.getProperty(Property.LOCATION).get().getValue());
                    System.out.println(component.getProperty(Property.LOCATION).get().getValue());
                    for (int k = 0; k < parts.length; k++) {
                        String numberOnly = parts[k].replaceAll("[^0-9]", "");
                        if (numberOnly.length() == 3) {
                            location  =  "https://territoire.emse.fr/kg/emse/fayol/" + numberOnly.charAt(0) + "ET/" + numberOnly ;
                            iri.addProperty(ResourceFactory.createProperty(schema + "location"), model.createResource("https://territoire.emse.fr/kg/emse/fayol/" + numberOnly.charAt(0) + "ET/" + numberOnly));
                        }
                    }
                }catch (Exception e){
                    continue;
                }
            }else {
                iri.addProperty(ResourceFactory.createProperty(schema + "location"), component.getProperty(Property.LOCATION).get().getValue());
            }


            try {
                String[] startDateTimeSplit = component.getProperty(Property.DTSTART).get().getValue().split("T");
                String time = startDateTimeSplit[1].substring(0,startDateTimeSplit[1].length()-1);
                startTime = time.substring(0, 2) + ":" + time.substring(2, 4)  + ":" + time.substring(4, time.length());
                iri.addProperty(ResourceFactory.createProperty(schema + "startTime"), model.createTypedLiteral(startTime, XSDDatatype.XSDtime));

             startDate = startDateTimeSplit[0].substring(0, 4) + "-" + startDateTimeSplit[0].substring(4, 6)  + "-" + startDateTimeSplit[0].substring(6, startDateTimeSplit[0].length());
                iri.addProperty(ResourceFactory.createProperty(schema + "startDate"), model.createTypedLiteral(startDate, XSDDatatype.XSDdate));
                String[] endDateTimeSplit = component.getProperty(Property.DTEND).get().getValue().split("T");
                String time1 = endDateTimeSplit[1].substring(0,endDateTimeSplit[1].length()-1);
                 endTime = time1.substring(0, 2) + ":" + time1.substring(2, 4)  + ":" + time1.substring(4, time1.length());
                iri.addProperty(ResourceFactory.createProperty(schema + "endTime"), model.createTypedLiteral(endTime, XSDDatatype.XSDtime));
                endDate = endDateTimeSplit[0].substring(0, 4) + "-" + endDateTimeSplit[0].substring(4, 6)  + "-" + endDateTimeSplit[0].substring(6, endDateTimeSplit[0].length());
                iri.addProperty(ResourceFactory.createProperty(schema + "endDate"), model.createTypedLiteral(endDate, XSDDatatype.XSDdate));
            }catch (Exception e){
                continue;
            }
            iri.addProperty(ResourceFactory.createProperty(schema + "accessibilitySummary"), model.createLiteral(component.getProperty(Property.SUMMARY).get().getValue(),"en"));
            iri.addProperty(ResourceFactory.createProperty(schema + "editor"), model.createLiteral("Arun","en"));
            iri.addProperty(ResourceFactory.createProperty(schema + "dateCreated"), model.createTypedLiteral(component.getProperty(Property.DTSTAMP).get().getValue(), XSDDatatype.XSDdateTime));
            //integrate platform territoire room url to the location




            URL sparqlEndPoint  = new URL("https://territoire.emse.fr/ldp/");
            HttpURLConnection httpConnection  = (HttpURLConnection) sparqlEndPoint.openConnection();
            httpConnection.setRequestMethod("POST");

            httpConnection.setRequestProperty("Content-Type", "application/sparql-query");
            httpConnection.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

            httpConnection.setDoOutput(true);
            OutputStreamWriter writerCheck = new OutputStreamWriter(httpConnection.getOutputStream());

            String askQuery  = String.format("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                    "PREFIX schema: <http://schema.org/>\n" +
                    "ASK {  \n" +
                    " ?uri  schema:editor ?editor;\n" +
                    "        schema:endDate ?endDate;\n" +
                    "        schema:startDate ?startDate;\n" +
                    "        schema:location ?loc ;\n" +
                    "        schema:startTime ?startTime ;\n" +
                    "        schema:endTime ?endTime ;\n" +
                    "FILTER  (?editor = \"Arun\"@en && regex(str(?loc), \"%s\") &&  ?endDate = \"%s\"^^xsd:date &&  ?startDate = \"%s\"^^xsd:date && ?startTime = \"%s\"^^xsd:time && ?endTime = \"%s\"^^xsd:time )\n" +
                    " }", location,endDate, startDate,startTime,endTime) ;
            writerCheck.write(askQuery);
            writerCheck.flush();
            writerCheck.close();
            httpConnection.getOutputStream().close();

            InputStream responseStreamCheck = httpConnection.getResponseCode() / 100 == 2
                    ? httpConnection.getInputStream()
                    : httpConnection.getErrorStream();
            Scanner sCheck = new Scanner(responseStreamCheck).useDelimiter("\\A");
            String responseCheck = sCheck.hasNext() ? sCheck.next() : "";
            System.out.println(responseCheck);

            JSONObject xmlJSONObj = XML.toJSONObject(responseCheck);

            System.out.println(xmlJSONObj.getJSONObject("sparql").get("boolean"));

            if(xmlJSONObj.getJSONObject("sparql").get("boolean").toString() == "true"){

                URL getSparqlEndpoint  = new URL("https://territoire.emse.fr/ldp/");
                HttpURLConnection getHTTPURL  = (HttpURLConnection) getSparqlEndpoint.openConnection();
                getHTTPURL.setRequestMethod("POST");

                getHTTPURL.setRequestProperty("Content-Type", "application/sparql-query");
                getHTTPURL.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

                getHTTPURL.setDoOutput(true);
                OutputStreamWriter getWriter = new OutputStreamWriter(getHTTPURL.getOutputStream());
                String query = String.format("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX schema: <http://schema.org/>\n" +
                        "SELECT ?url {  \n" +
                        " ?url  schema:editor ?editor;\n" +
                        "        schema:endDate ?endDate;\n" +
                        "        schema:startDate ?startDate;\n" +
                        "        schema:location ?loc ;\n" +
                        "        schema:startTime ?startTime ;\n" +
                        "        schema:endTime ?endTime ;\n" +
                        "FILTER  (?editor = \"Arun\"@en && regex(str(?loc), \"%s\") &&  ?endDate = \"%s\"^^xsd:date &&  ?startDate = \"%s\"^^xsd:date && ?startTime = \"%s\"^^xsd:time && ?endTime = \"%s\"^^xsd:time )\n" +
                        " }", location,endDate, startDate,startTime,endTime);

                getWriter.write(query);
                getWriter.flush();
                getWriter.close();
                getHTTPURL.getOutputStream().close();

                InputStream getResponseStream = getHTTPURL.getResponseCode() / 100 == 2
                        ? getHTTPURL.getInputStream()
                        : getHTTPURL.getErrorStream();
                Scanner getS = new Scanner(getResponseStream).useDelimiter("\\A");
                String getResponse = getS.hasNext() ? getS.next() : "";
                System.out.println(getResponse);
                JSONObject jsonObject = new JSONObject(getResponse);

                System.out.println(jsonObject.getJSONObject("results").getJSONArray("bindings"));
                JSONArray jsonArray = jsonObject.getJSONObject("results").getJSONArray("bindings");

                for (int k = 0, size = jsonArray.length(); k < size; k++) {
                    System.out.println(jsonArray.getJSONObject(k).getJSONObject("url").get("value").toString());
                    URL getGraph = new URL(jsonArray.getJSONObject(k).getJSONObject("url").get("value").toString());
                    HttpURLConnection getGraphhttpConn = (HttpURLConnection) getGraph.openConnection();
                    getGraphhttpConn.setRequestMethod("GET");

                    getGraphhttpConn.setRequestProperty("Content-Type", "text/turtle");
                    getGraphhttpConn.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

                    InputStream getGraphResponseStream = getGraphhttpConn.getResponseCode() / 100 == 2
                            ? getGraphhttpConn.getInputStream()
                            : getGraphhttpConn.getErrorStream();
                    Scanner GetGraphS = new Scanner(getGraphResponseStream).useDelimiter("\\A");
                    String getGraphResponse = GetGraphS.hasNext() ? GetGraphS.next() : "";
                    System.out.println(getGraphResponse);

                    String ETag = getGraphhttpConn.getHeaderFields().get("ETag").get(0);
                    Model model1 = ModelFactory.createDefaultModel();

                    RDFParser.fromString(getGraphResponse).forceLang(Lang.TTL).parse(model1);
                    Resource uri = model1.createResource(jsonArray.getJSONObject(k).getJSONObject("url").get("value").toString());

                    uri.addProperty(OWL.sameAs,model1.createResource(baseUrl + stringIdentifier + id )) ;

                    final ByteArrayOutputStream stringWriter3 = new ByteArrayOutputStream();
                    model1.write(stringWriter3, "TURTLE");
                    iri.addProperty(OWL.sameAs,model.createResource(jsonArray.getJSONObject(k).getJSONObject("url").get("value").toString())) ;


                    URL postURL = new URL(jsonArray.getJSONObject(k).getJSONObject("url").get("value").toString());
                    HttpURLConnection PosthttpConn = (HttpURLConnection) postURL.openConnection();
                    PosthttpConn.setRequestMethod("PUT");

                    PosthttpConn.setRequestProperty("Content-Type", "text/turtle");
                    PosthttpConn.setRequestProperty("If-Match", ETag );
                    PosthttpConn.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

                    PosthttpConn.setDoOutput(true);
                    OutputStreamWriter postWriter = new OutputStreamWriter(PosthttpConn.getOutputStream());
                    postWriter.write(stringWriter3.toString());
                    postWriter.flush();
                    postWriter.close();
                    PosthttpConn.getOutputStream().close();

                    InputStream postResponseStream =PosthttpConn.getResponseCode() / 100 == 2
                            ? PosthttpConn.getInputStream()
                            : PosthttpConn.getErrorStream();
                    Scanner postS = new Scanner(postResponseStream).useDelimiter("\\A");
                    String postResponse = postS.hasNext() ? postS.next() : "";
                    System.out.println(postResponse);

                }


            }


            iri.addProperty(ResourceFactory.createProperty(schema + "description"), model.createLiteral(component.getProperty(Property.DESCRIPTION).get().getValue(),"en"));
            final ByteArrayOutputStream stringWriter = new ByteArrayOutputStream();
            model.write(stringWriter, "TURTLE");


            URL url1 = new URL(baseUrl);
            HttpURLConnection httpConn1 = (HttpURLConnection) url1.openConnection();
            httpConn1.setRequestMethod("POST");
            httpConn1.setRequestProperty("Content-Type", "text/turtle");
            httpConn1.setRequestProperty("Slug", "aruntest23" );
            httpConn1.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");
            httpConn1.setDoOutput(true);
            OutputStreamWriter writer1 = new OutputStreamWriter(httpConn1.getOutputStream());
            writer1.write(stringWriter.toString());
            writer1.flush();
            writer1.close();

            httpConn1.getOutputStream().close();

            InputStream responseStream1 = httpConn1.getResponseCode() / 100 == 2
                    ? httpConn1.getInputStream()
                    : httpConn1.getErrorStream();
            Scanner s1 = new Scanner(responseStream1).useDelimiter("\\A");
            String response1 = s1.hasNext() ? s1.next() : "";
            System.out.println(response1);

        }
    }


}
