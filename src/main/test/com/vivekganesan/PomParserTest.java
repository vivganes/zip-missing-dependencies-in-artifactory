package com.vivekganesan;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PomParserTest {

    private PomParser pomParser;
    private DocumentBuilderFactory mockFactory;
    private DocumentBuilder mockBuilder;
    private Document mockDocument;

    @Before
    public void setUp() throws Exception {
        mockFactory = mock(DocumentBuilderFactory.class);
        mockBuilder = mock(DocumentBuilder.class);
        mockDocument = mock(Document.class);

        when(mockFactory.newDocumentBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.parse(any(File.class))).thenReturn(mockDocument);

        pomParser = new PomParser() {
            @Override
            protected DocumentBuilderFactory getDocumentBuilderFactory() {
                return mockFactory;
            }
        };
    }

    @Test
    public void testGetDependenciesFromPom() throws Exception {
        Element mockRootElement = mock(Element.class);
        NodeList mockDependencyNodeList = mock(NodeList.class);

        when(mockDocument.getDocumentElement()).thenReturn(mockRootElement);
        when(mockDocument.getElementsByTagName("dependency")).thenReturn(mockDependencyNodeList);
        when(mockDependencyNodeList.getLength()).thenReturn(2);

        Element mockDep1 = createMockDependencyElement("group1", "artifact1", "1.0.0");
        Element mockDep2 = createMockDependencyElement("group2", "artifact2", "2.0.0");

        when(mockDependencyNodeList.item(0)).thenReturn(mockDep1);
        when(mockDependencyNodeList.item(1)).thenReturn(mockDep2);

        List<String> dependencies = pomParser.getDependenciesFromPom("pom.xml");

        assertEquals("Expected 2 dependencies", 2, dependencies.size());
        assertTrue("Should contain group1:artifact1:1.0.0", dependencies.contains("group1:artifact1:1.0.0"));
        assertTrue("Should contain group2:artifact2:2.0.0", dependencies.contains("group2:artifact2:2.0.0"));
    }

    private Element createMockDependencyElement(String groupId, String artifactId, String version) {
        Element mockDep = mock(Element.class);
        Element mockGroupId = mock(Element.class);
        Element mockArtifactId = mock(Element.class);
        Element mockVersion = mock(Element.class);

        NodeList mockGroupIdList = mock(NodeList.class);
        NodeList mockArtifactIdList = mock(NodeList.class);
        NodeList mockVersionList = mock(NodeList.class);

        when(mockDep.getElementsByTagName("groupId")).thenReturn(mockGroupIdList);
        when(mockDep.getElementsByTagName("artifactId")).thenReturn(mockArtifactIdList);
        when(mockDep.getElementsByTagName("version")).thenReturn(mockVersionList);

        when(mockGroupIdList.getLength()).thenReturn(1);
        when(mockArtifactIdList.getLength()).thenReturn(1);
        when(mockVersionList.getLength()).thenReturn(1);

        when(mockGroupIdList.item(0)).thenReturn(mockGroupId);
        when(mockArtifactIdList.item(0)).thenReturn(mockArtifactId);
        when(mockVersionList.item(0)).thenReturn(mockVersion);

        when(mockGroupId.getTextContent()).thenReturn(groupId);
        when(mockArtifactId.getTextContent()).thenReturn(artifactId);
        when(mockVersion.getTextContent()).thenReturn(version);

        return mockDep;
    }
}