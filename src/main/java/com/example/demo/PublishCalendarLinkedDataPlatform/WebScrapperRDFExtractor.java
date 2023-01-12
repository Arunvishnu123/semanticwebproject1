package com.example.demo.PublishCalendarLinkedDataPlatform;


import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.json.JSONObject;
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
            String baseUrl = "https://territoire.emse.fr/ldp/arunraveendransheelafinal/";

            String test  = "/arun" ;

            Model model = ModelFactory.createDefaultModel();
            Resource url = model.createResource(baseUrl + stringIdentifier + id + "/");
            String schema = "http://schema.org/";
            model.setNsPrefix("schema", schema);

            JSONObject jsonObject = new JSONObject(scriptElement.data());

            System.out.println(jsonObject.get("@id"));

            jsonObject.put("@id", url);

            RDFParser.fromString(jsonObject.toString()).forceLang(Lang.JSONLD).parse(model);
            url.addProperty(ResourceFactory.createProperty(schema + "serialNumber"), model.createTypedLiteral( stringIdentifier + id , XSDDatatype.XSDstring));
            url.addProperty(ResourceFactory.createProperty(schema + "editor"), model.createLiteral("Arun","en"));





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
