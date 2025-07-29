package org.anandi.inputs.models;

import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.in.XesXmlParser;
import org.deckfour.xes.model.XLog;
import org.jbpt.pm.log.EventLog;
import org.processmining.alphaminer.algorithms.AlphaClassicMinerImpl;
import org.processmining.alphaminer.algorithms.AlphaMiner;
import org.processmining.alphaminer.algorithms.AlphaMinerFactory;
import org.processmining.alphaminer.parameters.AlphaMinerParameters;
import org.processmining.alphaminer.parameters.AlphaVersion;
import org.processmining.framework.util.Pair;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.InductiveMiner.plugins.IMPetriNet;
import org.processmining.alphaminer.plugins.AlphaMinerPlugin;
import org.processmining.plugins.kutoolbox.utils.FakePluginContext;
import org.processmining.plugins.pnml.exporting.PnmlExportNetToPNML;
//import org.processmining.plugins.flowerMiner.
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.util.List;

public class ModelGenerator {

    // first im working on inductive miner
    public void discoverModel(String xesFilePath, String outputPnmlPath) {
        try {
            // 1: read the .xes file
            File xesFile = new File(xesFilePath);
            XesXmlParser parser = new XesXmlParser();
            List<XLog> logs = parser.parse(Files.newInputStream(xesFile.toPath()));
            if (logs.isEmpty()) {
                System.err.println("No logs found in the XES file.");
                return;
            }
            XLog log = logs.get(0);

            // 2: discover the petri net using inductive miner
            FakePluginContext context = new FakePluginContext();
            Petrinet net = discoverAlphaMiner(log, context);

//            AlphaMinerParameters parameters = new AlphaMinerParameters();
//            parameters.setVersion(AlphaVersion.CLASSIC);
//            XEventClassifier classifier = new XEventNameClassifier();
//            AlphaMiner alphaMiner = AlphaMinerFactory.createAlphaMiner(context, log, classifier, parameters);
////            System.out.println(alphaMiner instanceof AlphaClassicMinerImpl);
//            Pair result = alphaMiner.run();
////            alphaMiner.run()




            // 3: export the petri net to pnml
            File outputFile = new File(outputPnmlPath);
            PnmlExportNetToPNML exporter = new PnmlExportNetToPNML();
            exporter.exportPetriNetToPNMLFile(context, net, outputFile);
            cleanTau(outputPnmlPath);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to discover model or export PNML.");
        }
    }

    public Petrinet discoverAlphaMiner(XLog log, FakePluginContext context) {
        AlphaMinerPlugin alphaMiner = new AlphaMinerPlugin();
        Object[] result = alphaMiner.apply(context, log);
        Petrinet net = (Petrinet) result[0];
        Marking initialMarking = (Marking) result[1];
        return net;
    }

    public Petrinet discoverInductiveMiner(XLog log, FakePluginContext context) {
        Object[] result = new IMPetriNet().minePetriNet(context, log);
        Petrinet net = (Petrinet) result[0];
        Marking initialMarking = (Marking) result[1];
        return net;
    }

    public Petrinet discoverHeuristicMiner(XLog log) {
        return null;
    }

    private void cleanTau(String inputFile) {
        try {
            File file = new File(inputFile);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            NodeList transitions = doc.getElementsByTagName("transition");
            for (int i = 0; i < transitions.getLength(); i++) {
                Node transition = transitions.item(i);

                if (transition.getNodeType() == Node.ELEMENT_NODE) {
                    Element transitionElement = (Element) transition;
                    NodeList nameList = transitionElement.getElementsByTagName("name");

                    if (nameList.getLength() > 0) {
                        Element nameElement = (Element) nameList.item(0);
                        NodeList textList = nameElement.getElementsByTagName("text");

                        if (textList.getLength() > 0) {
                            Element textElement = (Element) textList.item(0);
                            String label = textElement.getTextContent();

                            if (label != null && label.toLowerCase().contains("tau")) {
                                System.out.println("Making transition silent: " + label);
                                textElement.setTextContent("");  // Set label to empty
                            }
                        }
                    }
                }
            }
            // label = "null"

            // Save changes back to file (overwrite)
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file); // overwrite original file
            transformer.transform(source, result);

            System.out.println("Tau transitions cleaned and file updated.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
