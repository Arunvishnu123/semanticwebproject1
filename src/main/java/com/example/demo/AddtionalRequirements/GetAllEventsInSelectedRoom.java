package com.example.demo.AddtionalRequirements;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
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
public class GetAllEventsInSelectedRoom {

    public String getAllEventsInTheSelectedRoom(String roomNumber) throws IOException {


        URL url = new URL("https://territoire.emse.fr/ldp/");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("Content-Type", "application/sparql-query");
        httpConn.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

        httpConn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
        String query =  String.format("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" +
                "SELECT ?uri ?sameAs {  \n" +
                " ?uri  schema:editor ?editor;\n" +
                "        schema:location ?loc ;\n" +
                "        owl:sameAs ?sameAs ;\n" +
                "               schema:serialNumber ?serialNumber ; \n" +
                "FILTER  (?editor = \"Arun\"@en && regex(str(?loc), \"%s\") && regex(?serialNumber, \"course-event-\", \"i\" ))\n" +
                " }", roomNumber) ;
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

        String xsd = "http://www.w3.org/2001/XMLSchema" ;
        model.setNsPrefix("xsd", xsd);

        String sub = "http://localhost:8080/events/" + roomNumber ;
        Resource subUrl  =  model.createResource(sub);
        subUrl.addProperty(RDF.type, ResourceFactory.createProperty(schema + "EventSeries"));

        Resource blankNode =  model.createResource();

        subUrl.addProperty(ResourceFactory.createProperty(schema + "events"), blankNode) ;
        String test = null ;
        for (int i = 0, size = jsonArray.length(); i < size; i++) {

            if( model.listSubjectsWithProperty(ResourceFactory.createProperty(schema + "url"),model.createTypedLiteral(jsonArray.getJSONObject(i).getJSONObject("uri").get("value").toString(), XSDDatatype.XSDanyURI)).toList().isEmpty())
            {
                Resource blankNode1 =  model.createResource();
                blankNode.addProperty(ResourceFactory.createProperty(schema + "event"), blankNode1) ;
                blankNode1.addProperty(RDF.type, ResourceFactory.createProperty(schema + "Event"));
                blankNode1.addProperty(ResourceFactory.createProperty(schema + "url"), model.createTypedLiteral(jsonArray.getJSONObject(i).getJSONObject("uri").get("value").toString(), XSDDatatype.XSDanyURI));
                blankNode1.addProperty(ResourceFactory.createProperty(schema + "sameAs"), model.createTypedLiteral(jsonArray.getJSONObject(i).getJSONObject("sameAs").get("value").toString(), XSDDatatype.XSDanyURI));

            }else {
                model.add(model.listSubjectsWithProperty(ResourceFactory.createProperty(schema + "url"),model.createTypedLiteral(jsonArray.getJSONObject(i).getJSONObject("uri").get("value").toString(), XSDDatatype.XSDanyURI)).toList().get(0),ResourceFactory.createProperty(schema + "sameAs") ,  model.createTypedLiteral(jsonArray.getJSONObject(i).getJSONObject("uri").get("value").toString(), XSDDatatype.XSDanyURI));
                //blankNode1.addProperty(ResourceFactory.createProperty(schema + "sameAs"), model.createResource(jsonArray.getJSONObject(i).getJSONObject("sameAs").get("value").toString()));
            }
        }
        model.write(System.out, "TURTLE");
        final ByteArrayOutputStream stringWriter = new ByteArrayOutputStream();
        model.write(stringWriter, "TURTLE");


        return stringWriter.toString();



    }
}
