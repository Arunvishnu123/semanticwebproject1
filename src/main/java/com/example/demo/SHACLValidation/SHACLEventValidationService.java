package com.example.demo.SHACLValidation;

import org.apache.jena.graph.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFParser;
import org.apache.jena.shacl.ShaclValidator;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.shacl.lib.ShLib;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Scanner;

@Service
public class SHACLEventValidationService {

    public String eventValidation(String serialNumber) throws IOException {

        String shape = "C:\\Users\\ARUN\\OneDrive\\Desktop\\semanticwebproject\\demo\\src\\main\\java\\com\\example\\demo\\SHACLShapes\\EventShape.ttl" ;
        Graph shapesGraph = RDFDataMgr.loadGraph(shape);
        Shapes shapes = Shapes.parse(shapesGraph);


        URL url = new URL("https://territoire.emse.fr/ldp/arunfinal/" + serialNumber + "/");
        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
        httpConn.setRequestMethod("GET");

        httpConn.setRequestProperty("Authorization", "Basic bGRwdXNlcjpMaW5rZWREYXRhSXNHcmVhdA==");
        httpConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        InputStream responseStream = httpConn.getResponseCode() / 100 == 2
                ? httpConn.getInputStream()
                : httpConn.getErrorStream();
        Scanner s = new Scanner(responseStream).useDelimiter("\\A");
        String response = s.hasNext() ? s.next() : "";
        System.out.println(response);


        Model model = ModelFactory.createDefaultModel();

        RDFParser.fromString(response).forceLang(Lang.TTL).parse(model);
        Graph graph = model.getGraph();
        ValidationReport report = ShaclValidator.get().validate(shapes,graph);
        ShLib.printReport(report);
        final ByteArrayOutputStream stringWriter = new ByteArrayOutputStream();
        RDFDataMgr.write(stringWriter, report.getModel(), Lang.TTL);
        return stringWriter.toString();
    }
}
