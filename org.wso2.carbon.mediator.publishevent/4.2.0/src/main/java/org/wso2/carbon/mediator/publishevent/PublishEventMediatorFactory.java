/*
 * Copyright (c) {$year}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.mediator.publishevent;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.jaxen.JaxenException;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;

/**
 * Creates the publishEvent mediator with given configuration XML taken from the registry which is mentioned in the sequence.
 */
public class PublishEventMediatorFactory extends AbstractMediatorFactory {
    public static final QName PUBLISH_EVENT_Q = new QName(SynapseConstants.SYNAPSE_NAMESPACE, getTagName());
    public static final QName EVENT_SINK_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "eventSink");
    public static final QName STREAM_NAME_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "streamName");
    public static final QName STREAM_VERSION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "streamVersion");
    public static final QName ATTRIBUTES_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attributes");
    public static final QName ATTRIBUTE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attribute");
    public static final QName META_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "meta");
    public static final QName CORRELATION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "correlation");
    public static final QName PAYLOAD_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "payload");
    public static final QName TYPE_Q = new QName("type");
    public static final QName DEFAULT_Q = new QName("defaultValue");

    public static String getTagName() {
        return "publishEvent";
    }

    @Override
    public QName getTagQName() {
        return PUBLISH_EVENT_Q;
    }

    @Override
    public Mediator createSpecificMediator(OMElement omElement, Properties properties) {
        PublishEventMediator mediator = new PublishEventMediator();

        OMElement streamName = omElement.getFirstChildWithName(STREAM_NAME_Q);
        if (streamName == null) {
            throw new SynapseException(STREAM_NAME_Q.getLocalPart() + " element missing");
        }
        mediator.setStreamName(streamName.getText());

        OMElement streamVersion = omElement.getFirstChildWithName(STREAM_VERSION_Q);
        if (streamVersion == null) {
            throw new SynapseException(STREAM_VERSION_Q.getLocalPart() + " element missing");
        }
        mediator.setStreamVersion(streamVersion.getText());

        OMElement attributes = omElement.getFirstChildWithName(ATTRIBUTES_Q);
        if (attributes != null) {
            OMElement meta = attributes.getFirstChildWithName(META_Q);
            if (meta != null) {
                List<Property> propertyList = new ArrayList<Property>();
                Iterator iterator = meta.getChildrenWithName(ATTRIBUTE_Q);
                populateAttributes(propertyList, iterator);
                mediator.setMetaProperties(propertyList);
            }
            OMElement correlation = attributes.getFirstChildWithName(CORRELATION_Q);
            if (correlation != null) {
                List<Property> propertyList = new ArrayList<Property>();
                Iterator iterator = correlation.getChildrenWithName(ATTRIBUTE_Q);
                populateAttributes(propertyList, iterator);
                mediator.setCorrelationProperties(propertyList);
            }
            OMElement payload = attributes.getFirstChildWithName(PAYLOAD_Q);
            if (payload != null) {
                List<Property> propertyList = new ArrayList<Property>();
                Iterator iterator = payload.getChildrenWithName(ATTRIBUTE_Q);
                populateAttributes(propertyList, iterator);
                mediator.setPayloadProperties(propertyList);
            }
        } else {
            throw new SynapseException(ATTRIBUTES_Q.getLocalPart() + " attribute missing");
        }

        OMElement eventSinkElement = omElement.getFirstChildWithName(EVENT_SINK_Q);
        if (eventSinkElement == null) {
            throw new SynapseException(EVENT_SINK_Q.getLocalPart() + " element missing");
        }
        String eventSinkName = eventSinkElement.getText();
        mediator.setEventSink(eventSinkName);

        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String path = carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + "event-sinks.xml";
        try {
            BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(reader);
            OMElement eventSinks = builder.getDocumentElement();
            eventSinks.build();
            Iterator iterator = eventSinks.getChildrenWithLocalName("eventSink");
            boolean eventSinkFound = false;
            while (iterator.hasNext()) {
                OMElement eventSink = (OMElement)iterator.next();
                OMAttribute nameAttribute = eventSink.getAttribute(new QName("name"));
                if (nameAttribute != null && eventSinkName.equals(nameAttribute.getAttributeValue())) {
                    mediator.setThriftEndpointConfig(ThriftEndpointConfig.createThriftEndpointConfig(eventSink));
                    eventSinkFound = true;
                    break;
                }
            }
            if (!eventSinkFound) {
                throw new SynapseException("Event sink \"" + eventSinkName + "\" not found in event-sinks.xml");
            }
        } catch (FileNotFoundException e) {
            throw new SynapseException("event-sinks.xml file is not found in configuration directory", e);
        } catch (XMLStreamException e) {
            throw new SynapseException("event-sinks.xml content is invalid", e);
        }

        return mediator;
    }

    private void populateAttributes(List<Property> propertyList, Iterator iterator) {
        while (iterator.hasNext()) {
            OMElement element = (OMElement) iterator.next();
            OMAttribute nameAttr = element.getAttribute(ATT_NAME);
            if (nameAttr == null) {
                throw new SynapseException(ATT_NAME.getLocalPart() + " attribute missing in " + element.getLocalName());
            }
            OMAttribute typeAttr = element.getAttribute(TYPE_Q);
            if (typeAttr == null) {
                throw new SynapseException(TYPE_Q.getLocalPart() + " attribute missing in " + element.getLocalName());
            }
            OMAttribute valueAttr = element.getAttribute(ATT_VALUE);
            OMAttribute expressionAttr = element.getAttribute(ATT_EXPRN);
            if (valueAttr != null && expressionAttr != null) {
                throw new SynapseException(element.getLocalName() + " element can either have \"" + ATT_VALUE.getLocalPart() +
                        "\" or \"" + ATT_EXPRN.getLocalPart() + "\" attribute but not both");
            }

            if (valueAttr == null && expressionAttr == null) {
                throw new SynapseException(element.getLocalName() + " element must have either \"" + ATT_VALUE.getLocalPart() +
                        "\" or \"" + ATT_EXPRN.getLocalPart() + "\" attribute");
            }

            Property property = new Property();
            property.setKey(nameAttr.getAttributeValue());
            property.setType(typeAttr.getAttributeValue());
            if (valueAttr != null) {
                property.setValue(valueAttr.getAttributeValue());
            } else {
                try {
                    property.setExpression(SynapseXPathFactory.getSynapseXPath(element, ATT_EXPRN));
                } catch (JaxenException e) {
                    throw new SynapseException("Invalid expression attribute in " + element.getLocalName(), e);
                }
            }

            OMAttribute defaultAtr = element.getAttribute(DEFAULT_Q);
            if (defaultAtr != null) {
                property.setDefaultValue(defaultAtr.getAttributeValue());
            }

            propertyList.add(property);
        }
    }

    public static QName getNameAttributeQ() {
        return ATT_NAME;
    }

    public static QName getValueAttributeQ() {
        return ATT_VALUE;
    }

    public static QName getExpressionAttributeQ() {
        return ATT_EXPRN;
    }
}