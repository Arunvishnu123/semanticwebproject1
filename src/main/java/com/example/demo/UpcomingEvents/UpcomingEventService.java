package com.example.demo.UpcomingEvents;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.vocabulary.OWL;
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
public class UpcomingEventService {
    public  String upcomingEvents(String selectedDate) throws IOException {


        URL url = new URL("https://territoire.emse.fr/ldp/aruntest23/");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("POST");

        httpConn.setRequestProperty("Content-Type", "application/sparql-query");
        httpConn.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

        httpConn.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(httpConn.getOutputStream());
        String query =  String.format("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                "PREFIX schema: <http://schema.org/>\n" +
                "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
                "PREFIX ldp: <http://www.w3.org/ns/ldp#>\n" +
                "PREFIX owl: <http://www.w3.org/2002/07/owl#>              \n" +
                "select  ?uri  ?sameAs\n" +
                "WHERE   \n" +
                " {\n" +
                "?uri a schema:CourseInstance;\n" +
                "schema:editor ?editor ;\n" +
                "schema:startDate ?startDate;\n" +
                "schema:serialNumber ?serialNumber ; \n" +
                "owl:sameAs ?sameAs ; \n" +
                "FILTER (?editor = \"Arun\"@en && ?startDate > \"%s\"^^xsd:date && regex(?serialNumber,\"sem-\", \"i\")) .\n" +
                " }", selectedDate);
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


        String sub = "http://localhost:8080/upcomingevents/" + selectedDate ;
        Resource subUrl  =  model.createResource(sub);
        subUrl.addProperty(RDF.type, ResourceFactory.createProperty(schema + "EventSeries"));

        Resource blankNode =  model.createResource();

        subUrl.addProperty(ResourceFactory.createProperty(schema + "events"), blankNode) ;
        subUrl.addProperty(ResourceFactory.createProperty(schema + "startDate"), model.createTypedLiteral(selectedDate, XSDDatatype.XSDdate));

        subUrl.addProperty(ResourceFactory.createProperty(schema + "category"), model.createTypedLiteral("Upcoming Events", XSDDatatype.XSDstring));
        subUrl.addProperty(ResourceFactory.createProperty(rdfs + "comment"), model.createTypedLiteral("Upcoming Events from the selected date by the user", XSDDatatype.XSDstring));
        for (int i = 0, size = jsonArray.length(); i < size; i++) {
            if( model.listSubjectsWithProperty(ResourceFactory.createProperty(schema + "url"),model.createTypedLiteral(jsonArray.getJSONObject(i).getJSONObject("uri").get("value").toString(), XSDDatatype.XSDanyURI)).toList().isEmpty())
            {
                Resource blankNode1 =  model.createResource();
                blankNode.addProperty(ResourceFactory.createProperty(schema + "event"), blankNode1) ;
                blankNode1.addProperty(RDF.type, ResourceFactory.createProperty(schema + "Event"));
                blankNode1.addProperty(ResourceFactory.createProperty(schema + "url"), model.createTypedLiteral(jsonArray.getJSONObject(i).getJSONObject("uri").get("value").toString(), XSDDatatype.XSDanyURI));
                blankNode1.addProperty(OWL.sameAs, model.createTypedLiteral(jsonArray.getJSONObject(i).getJSONObject("sameAs").get("value").toString(), XSDDatatype.XSDanyURI));

            }else {
                model.add(model.listSubjectsWithProperty(ResourceFactory.createProperty(schema + "url"),model.createTypedLiteral(jsonArray.getJSONObject(i).getJSONObject("uri").get("value").toString(), XSDDatatype.XSDanyURI)).toList().get(0),OWL.sameAs ,  model.createTypedLiteral(jsonArray.getJSONObject(i).getJSONObject("sameAs").get("value").toString(), XSDDatatype.XSDanyURI));
                //blankNode1.addProperty(ResourceFactory.createProperty(schema + "sameAs"), model.createResource(jsonArray.getJSONObject(i).getJSONObject("sameAs").get("value").toString()));
            }

            //subUrl.addProperty(ResourceFactory.createProperty(schema + "events"), model.createResource(jsonArray.getJSONObject(i).getJSONObject("subject").get("value").toString()));
        }


        model.write(System.out, "TURTLE");

        final ByteArrayOutputStream stringWriter = new ByteArrayOutputStream();
        model.write(stringWriter, "TURTLE");


        return stringWriter.toString();
    }
}
