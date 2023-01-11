package com.example.demo.EventsInSaintEtienne;


import org.apache.jena.datatypes.xsd.XSDDatatype;
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
public class EventThatAreCourses {
    public  String eventsThatAreCourses() throws IOException {
        URL url = new URL("https://territoire.emse.fr/ldp/aruntest23/");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("Content-Type", "application/sparql-query");
        httpConn.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

        httpConn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
        writer.write("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\nPREFIX schema: <http://schema.org/>\nPREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\nPREFIX ldp: <http://www.w3.org/ns/ldp#>\n               \nselect ?sub\nWHERE   \n {\n?sub rdf:type schema:CourseInstance ;\n     schema:editor ?editor ; \nFILTER (?editor = \"Arun\"@en) .\n}");
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

        String sub = "http://localhost:8080/Event/saintetienne/courses";
        Resource subUrl  =  model.createResource(sub);


        for (int i = 0, size = jsonArray.length(); i < size; i++) {

            subUrl.addProperty(RDF.type, ResourceFactory.createProperty(schema + "EventSeries"));
            subUrl.addProperty(ResourceFactory.createProperty(schema + "category"), model.createTypedLiteral("Events that are  Courses in Saint-Etienne", XSDDatatype.XSDstring));
            subUrl.addProperty(ResourceFactory.createProperty(rdfs + "comment"), model.createTypedLiteral("Events in saint-etienne that are courses ", XSDDatatype.XSDstring));
            subUrl.addProperty(ResourceFactory.createProperty(schema + "events"), model.createResource(jsonArray.getJSONObject(i).getJSONObject("sub").get("value").toString()));
        }


        model.write(System.out, "TURTLE");


        final ByteArrayOutputStream stringWriter = new ByteArrayOutputStream();
        model.write(stringWriter, "TURTLE");


        return stringWriter.toString();
    }

}
