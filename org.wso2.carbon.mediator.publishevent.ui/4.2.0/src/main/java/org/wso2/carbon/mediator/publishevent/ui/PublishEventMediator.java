/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediator.publishevent.ui;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.SynapseXPathFactory;
import org.apache.synapse.config.xml.SynapseXPathSerializer;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.jaxen.JaxenException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.mediator.service.MediatorException;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class PublishEventMediator extends AbstractMediator {

    private static final Log log = LogFactory.getLog(PublishEventMediator.class);

    private static final String PROPERTY_SEPARATOR = ";";
    private static final String PROPERTY_VALUE_SEPARATOR = "::";
    private static final String PROPERTY_TYPE_VALUE = "value";
    private static final String PROPERTY_TYPE_EXPRESSION = "expression";
    //public static final QName PUBLISH_EVENT_Q = new QName(SynapseConstants.SYNAPSE_NAMESPACE, getTagName());
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

    private String streamName = "";
    private String streamVersion = "";
    private String eventSink = "";
    private List<Property> metaProperties = new ArrayList<Property>();
    private List<Property> correlationProperties = new ArrayList<Property>();
    private List<Property> payloadProperties = new ArrayList<Property>();
    private List<String> eventSinkList = new ArrayList();
    private String serverProfile = "";



    public String getTagLocalName() {
        return "publishEvent";
    }

    public OMElement serialize(OMElement parent) {


        OMElement publishEventElement = fac.createOMElement("publishEvent", synNS);
        saveTracingState(publishEventElement, this);

        if (streamName != null & !streamName.equals("")) {
            OMElement streamNameElement = fac.createOMElement(PublishEventMediator.STREAM_NAME_Q.getLocalPart(), synNS);
            streamNameElement.setText(this.getStreamName());
            publishEventElement.addChild(streamNameElement);

        } else {
            throw new MediatorException("Stream name not specified");
        }
        if (streamVersion != null & !streamName.equals("")) {

            OMElement streamVersionElement = fac.createOMElement(PublishEventMediator.STREAM_VERSION_Q.getLocalPart(), synNS);
            streamVersionElement.setText(this.getStreamVersion());
            publishEventElement.addChild(streamVersionElement);
        } else {
            throw new MediatorException("Stream version not specified");
        }

        OMElement eventSinkElement = fac.createOMElement(PublishEventMediator.EVENT_SINK_Q.getLocalPart(), synNS);
        eventSinkElement.setText(this.getEventSink());
        publishEventElement.addChild(eventSinkElement);


        OMElement streamAttributesElement = fac.createOMElement(PublishEventMediator.ATTRIBUTES_Q.getLocalPart(), synNS);

        OMElement metaAttributesElement = fac.createOMElement(PublishEventMediator.META_Q.getLocalPart(), synNS);
        for (Property property : this.getMetaProperties()) {
            metaAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(metaAttributesElement);

        OMElement correlationAttributesElement = fac.createOMElement(PublishEventMediator.CORRELATION_Q.getLocalPart(), synNS);
        for (Property property : this.getCorrelationProperties()) {
            correlationAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(correlationAttributesElement);

        OMElement payloadAttributesElement = fac.createOMElement(PublishEventMediator.PAYLOAD_Q.getLocalPart(), synNS);
        for (Property property : this.getPayloadProperties()) {
            payloadAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(payloadAttributesElement);

        publishEventElement.addChild(streamAttributesElement);

        if (parent != null) {
            parent.addChild(publishEventElement);
        }

        return publishEventElement;
    }

    public void build(OMElement elem) {
        OMElement streamName = elem.getFirstChildWithName(STREAM_NAME_Q);
        if (streamName == null) {
            throw new SynapseException(STREAM_NAME_Q.getLocalPart() + " element missing");
        }
        this.setStreamName(streamName.getText());

        OMElement streamVersion = elem.getFirstChildWithName(STREAM_VERSION_Q);
        if (streamVersion == null) {
            throw new SynapseException(STREAM_VERSION_Q.getLocalPart() + " element missing");
        }
        this.setStreamVersion(streamVersion.getText());

        OMElement eventSinkName = elem.getFirstChildWithName(EVENT_SINK_Q);
        if (eventSinkName == null) {
            throw new SynapseException(EVENT_SINK_Q.getLocalPart() + " element missing");
        }
        this.setEventSink(eventSinkName.getText());


        OMElement attributes = elem.getFirstChildWithName(ATTRIBUTES_Q);
        if (attributes != null) {
            OMElement meta = attributes.getFirstChildWithName(META_Q);
            if (meta != null) {
                List<Property> propertyList = new ArrayList<Property>();
                    Iterator iter = meta.getChildrenWithName(ATTRIBUTE_Q);
                populateAttributes(propertyList, iter);
                this.setMetaProperties(propertyList);
            }
            OMElement correlation = attributes.getFirstChildWithName(CORRELATION_Q);
            if (correlation != null) {
                List<Property> propertyList = new ArrayList<Property>();
                Iterator iter = correlation.getChildrenWithName(ATTRIBUTE_Q);
                populateAttributes(propertyList, iter);
                this.setCorrelationProperties(propertyList);
            }
            OMElement payload = attributes.getFirstChildWithName(PAYLOAD_Q);
            if (payload != null) {
                List<Property> propertyList = new ArrayList<Property>();
                Iterator iter = payload.getChildrenWithName(ATTRIBUTE_Q);
                populateAttributes(propertyList, iter);
                this.setPayloadProperties(propertyList);
            }
        } else {
            throw new SynapseException(ATTRIBUTES_Q.getLocalPart() + " attribute missing");
        }

    }

    private List<Attribute> generateAttributeList(List<Property> propertyList) {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        for (Property property : propertyList) {
            attributeList.add(new Attribute(property.getName(), property.getDatabridgeAttributeType()));
        }
        return attributeList;
    }

    public String getServerProfile() {
        return serverProfile;
    }

    public String getStreamVersion() {
        return streamVersion;
    }

    public String getEventSink() {
        return eventSink;
    }

    public List<Property> getMetaProperties() {
        return metaProperties;
    }

    public List<Property> getCorrelationProperties() {
        return correlationProperties;
    }

    public List<Property> getPayloadProperties() {
        return payloadProperties;
    }

    public String getStreamName() {
        return streamName;
    }

    public List<String> getEventSinkList() {
        return eventSinkList;
    }

    public void setEventSinkList(List<String> eventSinkList) {
        this.eventSinkList = eventSinkList;
    }

    public void setServerProfile(String serverProfile) {
        this.serverProfile = serverProfile;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public void setStreamVersion(String streamVersion) {
        this.streamVersion = streamVersion;
    }

    public void setEventSink(String eventSink) {
        this.eventSink = eventSink;
    }

    public void setMetaProperties(List<Property> metaProperties) {
        this.metaProperties = metaProperties;
    }

    public void setCorrelationProperties(List<Property> correlationProperties) {
        this.correlationProperties = correlationProperties;
    }

    public void setPayloadProperties(List<Property> payloadProperties) {
        this.payloadProperties = payloadProperties;
    }

    private OMElement createElementForProperty(Property property) {
        OMElement attributeElement = fac.createOMElement(PublishEventMediator.ATTRIBUTE_Q.getLocalPart(), synNS);
        attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediator.getNameAttributeQ().getLocalPart(), nullNS, property.getName()));
        attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediator.TYPE_Q.getLocalPart(), nullNS, property.getType()));
        attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediator.DEFAULT_Q.getLocalPart(), nullNS, property.getDefaultValue()));

        if (property.getExpression() != null) {
            SynapseXPathSerializer.serializeXPath(property.getExpression(), attributeElement, PublishEventMediator.getExpressionAttributeQ().getLocalPart());
        } else {
            attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediator.getValueAttributeQ().getLocalPart(), nullNS, property.getValue()));
        }
        return attributeElement;
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

    private void populateAttributes(List<Property> propertyList, Iterator iter) {
        while (iter.hasNext()) {
            OMElement element = (OMElement) iter.next();
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
            property.setName(nameAttr.getAttributeValue());
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

    public void clearList(String type){
        if(type.equals("meta")){
            metaProperties.clear();
        }else if (type.equals("correlation")){
            correlationProperties.clear();
        }else if (type.equals("payload")){
            payloadProperties.clear();
        }
    }


}
