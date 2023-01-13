package com.example.demo.AddAttendees;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
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
public class AddAttendeesService {

    public String  addAttendee(String attendeeName, String courseName, String eventDate) throws IOException {



        URL url = new URL("https://territoire.emse.fr/ldp/");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("Content-Type", "application/sparql-query");
        httpConn.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

        httpConn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());

        String sparqlQuery  =  String.format("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "SELECT ?url ?serialNumber\n" +
                " WHERE {  \n" +
                "      ?url schema:accessibilitySummary ?summary ;\n" +
                "       schema:startDate ?startDate ;\n" +
                "       schema:editor ?eitor ;\n" +
                "        FILTER (regex(?summary, \"%s\" , \"i\" ) && (?startDate = \"%s\"^^xsd:date) && (?eitor = \"Arun\"@en)) .\n" +
                " }\n", courseName, eventDate);
        writer.write(sparqlQuery);
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

        System.out.println(jsonArray.getJSONObject(0).getJSONObject("url").get("value").toString());

        for (int i = 0, size = jsonArray.length(); i < size; i++) {
            URL url1 = new URL(jsonArray.getJSONObject(i).getJSONObject("url").get("value").toString());
            HttpURLConnection httpConn1 = (HttpURLConnection) url1.openConnection();
            httpConn1.setRequestMethod("GET");

            httpConn1.setRequestProperty("Content-Type", "text/turtle");
            httpConn1.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

            InputStream responseStream1 = httpConn1.getResponseCode() / 100 == 2
                    ? httpConn1.getInputStream()
                    : httpConn1.getErrorStream();
            Scanner s1 = new Scanner(responseStream1).useDelimiter("\\A");
            String response1 = s1.hasNext() ? s1.next() : "";
            System.out.println(response1);

            String ETag = httpConn1.getHeaderFields().get("ETag").get(0);

            System.out.println(ETag);

            Model model = ModelFactory.createDefaultModel();

            RDFParser.fromString(response1).forceLang(Lang.TTL).parse(model);

            model.write(System.out, "TURTLE") ;
            String schema = "http://schema.org/";
            model.setNsPrefix("schema", schema);
            Resource uri = model.createResource(jsonArray.getJSONObject(i).getJSONObject("url").get("value").toString());
            Resource blankNode = model.createResource();
            if (model.contains( uri , ResourceFactory.createProperty(schema + "attendees"))){
                model.add(model.listSubjectsWithProperty(ResourceFactory.createProperty(schema + "attendee")).toList().get(0),ResourceFactory.createProperty(schema + "attendee"),attendeeName);

            }
            else {
                uri.addProperty(ResourceFactory.createProperty(schema + "attendees"), blankNode);
                model.add(blankNode, RDF.type, ResourceFactory.createProperty(schema + "Person"));
                model.add(blankNode, ResourceFactory.createProperty(schema + "attendee"), model.createTypedLiteral(attendeeName, XSDDatatype.XSDstring));

            }

            final ByteArrayOutputStream stringWriter = new ByteArrayOutputStream();
            model.write(stringWriter, "TURTLE");


            URL url2 = new URL(jsonArray.getJSONObject(i).getJSONObject("url").get("value").toString());
            HttpURLConnection httpConn2 = (HttpURLConnection) url2.openConnection();
            httpConn2.setRequestMethod("PUT");

            httpConn2.setRequestProperty("Content-Type", "text/turtle");
            httpConn2.setRequestProperty("If-Match", ETag );
            httpConn2.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

            httpConn2.setDoOutput(true);
            OutputStreamWriter writer2 = new OutputStreamWriter(httpConn2.getOutputStream());
            writer2.write(stringWriter.toString());
            writer2.flush();
            writer2.close();
            httpConn2.getOutputStream().close();

            InputStream responseStream2 = httpConn2.getResponseCode() / 100 == 2
                    ? httpConn2.getInputStream()
                    : httpConn2.getErrorStream();
            Scanner s2 = new Scanner(responseStream2).useDelimiter("\\A");
            String response2 = s2.hasNext() ? s2.next() : "";
            System.out.println(response2);


        }

        return "Successfully Updated" ;
    }
}
