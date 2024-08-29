package com.vivekganesan;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PomParser {

    protected DocumentBuilderFactory getDocumentBuilderFactory() {
        return DocumentBuilderFactory.newInstance();
    }

    public List<String> getDependenciesFromPom(String pomFilePath) throws Exception {
        List<String> dependencies = new ArrayList<>();
        File pomFile = new File(pomFilePath);

        DocumentBuilderFactory dbFactory = getDocumentBuilderFactory();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(pomFile);
        doc.getDocumentElement().normalize();

        NodeList dependencyNodes = doc.getElementsByTagName("dependency");
        for (int i = 0; i < dependencyNodes.getLength(); i++) {
            Element dependencyElement = (Element) dependencyNodes.item(i);
            String groupId = getElementTextContent(dependencyElement, "groupId");
            String artifactId = getElementTextContent(dependencyElement, "artifactId");
            String version = getElementTextContent(dependencyElement, "version");
            dependencies.add(groupId + ":" + artifactId + ":" + version);
        }
        return dependencies;
    }

    private String getElementTextContent(Element parentElement, String tagName) {
        NodeList nodeList = parentElement.getElementsByTagName(tagName);
        if (nodeList.getLength() > 0) {
            return nodeList.item(0).getTextContent();
        }
        return "";
    }
}