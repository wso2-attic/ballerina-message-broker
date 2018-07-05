/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.brokerperformancetester.Utils;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

/**
 * Util class to analyze and create .jmx file
 */
public class XMLUtils {

    private static final Logger LOG = Logger.getLogger(XMLUtils.class);

    String jndiFolderPath;
    int loopCount;
    int threadCount;
    int rampTime;

    public XMLUtils(String jndiFolderPath, int loopCount, int threadCount, int rampTime) {
        this.jndiFolderPath = jndiFolderPath;
        this.loopCount = loopCount;
        this.threadCount = threadCount;
        this.rampTime = rampTime;
    }

    public void generateTestPlan() {
        try {
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(Constants.FILE_PATH);
            XPathFactory factory = XPathFactory.newInstance();
            XPath xpath = factory.newXPath();

            // set loop count node
            Node jmxNode = (Node) xpath.evaluate(Constants.XPATH_THREAD_GROUP, doc, XPathConstants.NODE);
            NodeList childNodeList = jmxNode.getChildNodes();
            for (int childCount = 0; childCount < childNodeList.getLength(); childCount++) {
                Node node = childNodeList.item(childCount);
                if (Constants.XML_STRING_PROP.equals(node.getNodeName())) {
                    Element nodeElement = (Element) node;
                    if (nodeElement.getAttribute(Constants.XML_NAME) != null) {
                        switch (nodeElement.getAttribute(Constants.XML_NAME)) {
                            case Constants.XML_NO_OF_THREADS:
                                LOG.info("Setting thread count to " + threadCount);
                                node.setTextContent(threadCount + "");
                                break;
                            case Constants.XML_RAMP_TIME:
                                LOG.info("Setting ramp time to " + rampTime);
                                node.setTextContent(rampTime + "");
                        }
                    }
                } else if (Constants.XML_ELEMENT_PROP.equals(node.getNodeName())) {
                    NodeList elementPropChildNodes = node.getChildNodes();
                    for (int propChildCount = 0; propChildCount < elementPropChildNodes.getLength(); propChildCount++) {
                        Node propChildNode = elementPropChildNodes.item(propChildCount);
                        if (Constants.XML_STRING_PROP.equals(propChildNode.getNodeName())) {
                            LOG.info("Setting loop count to " + loopCount);
                            propChildNode.setTextContent(loopCount + "");
                        }
                    }
                }
            }

            jmxNode = (Node) xpath.evaluate(Constants.XPATH_PUBLISHER_SAMPLER, doc, XPathConstants.NODE);
            childNodeList = jmxNode.getChildNodes();

            for (int childCount = 0; childCount < childNodeList.getLength(); childCount++) {
                Node node = childNodeList.item(childCount);
                if (Constants.XML_STRING_PROP.equals(node.getNodeName())) {
                    Element nodeElement = (Element) node;
                    if (nodeElement.getAttribute(Constants.XML_NAME) != null) {
                        switch (nodeElement.getAttribute(Constants.XML_NAME)) {
                            case Constants.XML_JMS_PROVIDER_URL:
                                LOG.info("Setting jndi properties location to " + jndiFolderPath);
                                node.setTextContent(jndiFolderPath + "");
                        }
                    }
                }
            }

            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(new File(Constants.FILE_PATH));
            transformer.transform(source, result);

        } catch (Exception ex) {
            LOG.error("Error occured while creating test plan." + ex.getMessage());
        }
    }
}

