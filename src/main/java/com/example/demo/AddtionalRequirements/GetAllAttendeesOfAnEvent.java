package com.example.demo.AddtionalRequirements;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Service
public class GetAllAttendeesOfAnEvent {

    public String getAttendeeListOfAnEvent(String courseName, String startDate) throws IOException {
        URL url = new URL("https://territoire.emse.fr/ldp/");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("Content-Type", "application/sparql-query");
        httpConn.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

        httpConn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
        String query = String.format("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>               \n" +
                "select ?uri ?attendee\n" +
                "WHERE   \n" +
                " {\n" +
                "?uri\n" +
                "    schema:serialNumber ?serialNumber ; \n" +
                "      schema:editor ?editor ;\n" +
                "      schema:startDate ?startDate ; \n" +
                "      schema:accessibilitySummary ?summary ; \n" +
                "      schema:attendees ?attendees. \n" +
                "?attendees schema:attendee ?attendee ;\n" +
                "FILTER (?editor = \"Arun\"@en && regex(?serialNumber,\"sem-\", \"i\") && regex(?summary,\"%s\", \"i\") && ?startDate=\"%s\"^^xsd:date) .\n" +
                "}", courseName, startDate);
        writer.write(query);
        writer.flush();
        writer.close();
        httpConn.getOutputStream().close();

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        System.out.println(response);
        JSONObject jsonObject = new JSONObject(response);

        System.out.println(jsonObject.getJSONObject("results").getJSONArray("bindings"));
        JSONArray jsonArray = jsonObject.getJSONObject("results").getJSONArray("bindings");

        Model model = ModelFactory.createDefaultModel();
        String schema = "http://schema.org/";
        model.setNsPrefix("schema", schema);

        String rdfs = "http://www.w3.org/2000/01/rdf-schema" ;
        model.setNsPrefix("rdfs", rdfs);

        String xsd = "http://www.w3.org/2001/XMLSchema#" ;
        model.setNsPrefix("xsd", xsd);
        String owl = "http://www.w3.org/2002/07/owl#" ;
        model.setNsPrefix("owl", owl);

        String sub = "http://localhost:8080/course/attendees/" ;
        Resource subUrl  =  model.createResource(sub);
        Resource blankNode = model.createResource();
        subUrl.addProperty(ResourceFactory.createProperty(schema + "attendee"),blankNode);
        blankNode.addProperty(RDF.type, ResourceFactory.createProperty(schema + "Person"));
        try {

            subUrl.addProperty(ResourceFactory.createProperty(schema + "event"), model.createResource(jsonArray.getJSONObject(0).getJSONObject("uri").get("value").toString()));
        }catch (Exception e){
            System.out.println("attendee not added");
        }
        for (int i = 0, size = jsonArray.length(); i < size; i++) {
            blankNode.addProperty(ResourceFactory.createProperty(schema + "attendee"),jsonArray.getJSONObject(i).getJSONObject("attendee").get("value").toString());
        }
        model.write(System.out, "TURTLE");
        final ByteArrayOutputStream stringWriter = new ByteArrayOutputStream();
        model.write(stringWriter, "TURTLE");

        return stringWriter.toString() ;
    }
}
