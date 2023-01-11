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
import org.apache.jena.vocabulary.RDF;
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


            Component component = (Component) i.next();
            System.out.println("Component [" + component.getName() + "]");
            for (int j = id; j < id + 1; j++) {
                id = j + 1;
                break;
            }
            String baseUrl =  "https://territoire.emse.fr/ldp/aruntest23/" ;


            Resource iri = model.createResource("/" + stringIdentifier + id );


            iri.addProperty(ResourceFactory.createProperty(schema + "identifier"), model.createTypedLiteral(component.getProperty(Property.UID).get().getValue(), XSDDatatype.XSDstring));
            iri.addProperty(ResourceFactory.createProperty(schema + "serialNumber"), model.createTypedLiteral( stringIdentifier + id , XSDDatatype.XSDstring));
            iri.addProperty(RDF.type,ResourceFactory.createProperty(schema + "Event"));
            if(component.getProperty(Property.DESCRIPTION).get().getValue().contains("CPS")){
                iri.addProperty(RDF.type,ResourceFactory.createProperty(schema + "CourseInstance"));
            }


            try {
                String[] startDateTimeSplit = component.getProperty(Property.DTSTART).get().getValue().split("T");
                String time = startDateTimeSplit[1].substring(0,startDateTimeSplit[1].length()-1);
                String startTime = time.substring(0, 2) + ":" + time.substring(2, 4)  + ":" + time.substring(4, time.length());
                iri.addProperty(ResourceFactory.createProperty(schema + "startTime"), model.createTypedLiteral(startTime, XSDDatatype.XSDtime));

                String  startDate = startDateTimeSplit[0].substring(0, 4) + "-" + startDateTimeSplit[0].substring(4, 6)  + "-" + startDateTimeSplit[0].substring(6, startDateTimeSplit[0].length());
                iri.addProperty(ResourceFactory.createProperty(schema + "startDate"), model.createTypedLiteral(startDate, XSDDatatype.XSDdate));
                String[] endDateTimeSplit = component.getProperty(Property.DTEND).get().getValue().split("T");
                String time1 = endDateTimeSplit[1].substring(0,endDateTimeSplit[1].length()-1);
                String endTime = time1.substring(0, 2) + ":" + time1.substring(2, 4)  + ":" + time1.substring(4, time1.length());
                iri.addProperty(ResourceFactory.createProperty(schema + "endTime"), model.createTypedLiteral(endDateTimeSplit[1], XSDDatatype.XSDtime));
                String  endDate = endDateTimeSplit[0].substring(0, 4) + "-" + endDateTimeSplit[0].substring(4, 6)  + "-" + endDateTimeSplit[0].substring(6, endDateTimeSplit[0].length());
                iri.addProperty(ResourceFactory.createProperty(schema + "endDate"), model.createTypedLiteral(endDate, XSDDatatype.XSDdate));
            }catch (Exception e){
                continue;
            }
            iri.addProperty(ResourceFactory.createProperty(schema + "accessibilitySummary"), model.createLiteral(component.getProperty(Property.SUMMARY).get().getValue(),"en"));
            iri.addProperty(ResourceFactory.createProperty(schema + "editor"), model.createLiteral("Arun","en"));
            iri.addProperty(ResourceFactory.createProperty(schema + "dateCreated"), model.createTypedLiteral(component.getProperty(Property.DTSTAMP).get().getValue(), XSDDatatype.XSDdateTime));
            //integrate platform territoire room url to the location
            try {
                String[] parts = component.getProperty(Property.LOCATION).get().getValue().split(",");
                iri.addProperty(ResourceFactory.createProperty(schema + "address"), component.getProperty(Property.LOCATION).get().getValue());
                System.out.println(component.getProperty(Property.LOCATION).get().getValue());
                for (int k = 0; k < parts.length; k++) {
                    String numberOnly = parts[k].replaceAll("[^0-9]", "");
                    if (numberOnly.length() == 3) {
                        iri.addProperty(ResourceFactory.createProperty(schema + "location"), model.createResource("https://territoire.emse.fr/kg/emse/fayol/" + numberOnly.charAt(0) + "ET/" + numberOnly));
                    }
                }
            }catch (Exception e){
                continue;
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
