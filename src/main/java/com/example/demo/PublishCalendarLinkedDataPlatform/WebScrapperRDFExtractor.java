package com.example.demo.PublishCalendarLinkedDataPlatform;


import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.vocabulary.OWL;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@Service
public class WebScrapperRDFExtractor {

    public void webScraperRDFExtractorFunction(String webURL, String uniqueIdentifier) throws IOException {

        String webPageURL = webURL ;
        Document doc = Jsoup.connect(webPageURL).get();
        Integer id = 0;
        String stringIdentifier = uniqueIdentifier ;
        Elements scriptElements = doc.select("script[type = application/ld+json]");
        for (Element scriptElement : scriptElements) {
            for (int j = id; j < id + 1; j++) {
                id = j + 1;
                break;
            }
            String baseUrl = "https://territoire.emse.fr/ldp/arunfinal/";

            String test  = "/arun" ;

            Model model = ModelFactory.createDefaultModel();
            Resource url = model.createResource(baseUrl + stringIdentifier + id + "/");
            String schema = "http://schema.org/";
            model.setNsPrefix("schema", schema);

            JSONObject jsonObject = new JSONObject(scriptElement.data());

            String startDate = jsonObject.get("startDate").toString() ;
            String endDate = jsonObject.get("endDate").toString() ;
            String locationName = jsonObject.getJSONObject("location").get("name").toString();
            String eventName =  jsonObject.get("name").toString() ;

            System.out.println(jsonObject.get("@id"));

            jsonObject.put("@id", url);

            RDFParser.fromString(jsonObject.toString()).forceLang(Lang.JSONLD).parse(model);


            url.addProperty(ResourceFactory.createProperty(schema + "serialNumber"), model.createTypedLiteral( stringIdentifier + id , XSDDatatype.XSDstring));
            url.addProperty(ResourceFactory.createProperty(schema + "editor"), model.createLiteral("Arun","en"));

            //find similar resource

            try {
                URL sparqlEndPoint = new URL("https://territoire.emse.fr/ldp/aruntest23/");
                HttpURLConnection httpConnection = (HttpURLConnection) sparqlEndPoint.openConnection();
                httpConnection.setRequestMethod("POST");

                httpConnection.setRequestProperty("Content-Type", "application/sparql-query");
                httpConnection.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

                httpConnection.setDoOutput(true);
                OutputStreamWriter writerCheck = new OutputStreamWriter(httpConnection.getOutputStream());

                String askQuery = String.format("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                        "PREFIX schema: <http://schema.org/>\n" +
                        "ASK {  \n" +
                        " ?uri  schema:editor ?editor;\n" +
                        "        schema:startDate ?startDate;\n" +
                        "        schema:name ?eventName ;\n" +
                        "        schema:endDate ?endDate ;\n" +
                        "        schema:location ?loc.\n" +
                        "?loc schema:name ?placeName ;  \n" +
                        "FILTER  (?editor = \"Arun\"@en &&  ?endDate = \"%s\"^^schema:Date &&  ?startDate = \"%s\"^^schema:Date && ?eventName = \"%s\" && ?placeName = \"%s\")\n" +
                        "}", endDate, startDate, eventName, locationName);
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

                if (xmlJSONObj.getJSONObject("sparql").get("boolean").toString() == "true") {

                    URL getSparqlEndpoint = new URL("https://territoire.emse.fr/ldp/aruntest23/");
                    HttpURLConnection getHTTPURL = (HttpURLConnection) getSparqlEndpoint.openConnection();
                    getHTTPURL.setRequestMethod("POST");

                    getHTTPURL.setRequestProperty("Content-Type", "application/sparql-query");
                    getHTTPURL.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

                    getHTTPURL.setDoOutput(true);
                    OutputStreamWriter getWriter = new OutputStreamWriter(getHTTPURL.getOutputStream());
                    String query = String.format("PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
                            "PREFIX schema: <http://schema.org/>\n" +
                            "SELECT ?url {  \n" +
                            " ?url  schema:editor ?editor;\n" +
                            "        schema:startDate ?startDate;\n" +
                            "        schema:name ?eventName ;\n" +
                            "        schema:endDate ?endDate ;\n" +
                            "        schema:location ?loc.\n" +
                            "?loc schema:name ?placeName ;  \n" +
                            "FILTER  (?editor = \"Arun\"@en &&  ?endDate = \"%s\"^^schema:Date &&  ?startDate = \"%s\"^^schema:Date && ?eventName = \"%s\" && ?placeName = \"%s\")\n" +
                            "}", endDate, startDate, eventName, locationName);

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
                    JSONObject jsonObject1 = new JSONObject(getResponse);

                    System.out.println(jsonObject1.getJSONObject("results").getJSONArray("bindings"));
                    JSONArray jsonArray = jsonObject1.getJSONObject("results").getJSONArray("bindings");

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

                        uri.addProperty(OWL.sameAs, model1.createResource(baseUrl + stringIdentifier + id + "/"));
                        url.addProperty(OWL.sameAs, model1.createResource(jsonArray.getJSONObject(k).getJSONObject("url").get("value").toString()));
                        final ByteArrayOutputStream stringWriter3 = new ByteArrayOutputStream();
                        model1.write(stringWriter3, "TURTLE");

                        URL postURL = new URL(jsonArray.getJSONObject(k).getJSONObject("url").get("value").toString());
                        HttpURLConnection PosthttpConn = (HttpURLConnection) postURL.openConnection();
                        PosthttpConn.setRequestMethod("PUT");

                        PosthttpConn.setRequestProperty("Content-Type", "text/turtle");
                        PosthttpConn.setRequestProperty("If-Match", ETag);
                        PosthttpConn.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");

                        PosthttpConn.setDoOutput(true);
                        OutputStreamWriter postWriter = new OutputStreamWriter(PosthttpConn.getOutputStream());
                        postWriter.write(stringWriter3.toString());
                        postWriter.flush();
                        postWriter.close();
                        PosthttpConn.getOutputStream().close();

                        InputStream postResponseStream = PosthttpConn.getResponseCode() / 100 == 2
                                ? PosthttpConn.getInputStream()
                                : PosthttpConn.getErrorStream();
                        Scanner postS = new Scanner(postResponseStream).useDelimiter("\\A");
                        String postResponse = postS.hasNext() ? postS.next() : "";
                        System.out.println(postResponse);

                    }


                }

            }
            catch (Exception e){
                continue;
            }




            model.write(System.out,"TURTLE");



            final ByteArrayOutputStream stringWriter = new ByteArrayOutputStream();
            model.write(stringWriter, "TURTLE");

            URL url2 = new URL(baseUrl);
            HttpURLConnection httpConn1 = (HttpURLConnection) url2.openConnection();
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
