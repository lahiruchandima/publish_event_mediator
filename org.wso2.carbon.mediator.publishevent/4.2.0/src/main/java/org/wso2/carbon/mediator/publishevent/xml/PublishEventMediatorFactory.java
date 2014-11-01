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
package org.wso2.carbon.mediator.publishevent.xml;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.AbstractMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.mediator.publishevent.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Creates the publishEvent mediator with given configuration XML taken from the registry which is mentioned in the sequence.
 */
public class PublishEventMediatorFactory extends AbstractMediatorFactory {
    private static final Log log = LogFactory.getLog(PublishEventMediatorFactory.class);
    public static final QName PUBLISH_EVENT_Q = new QName(SynapseConstants.SYNAPSE_NAMESPACE, getTagName());
    public static final String SERVER_PROFILE_LOCATION = "bamServerProfiles";
    static final QName STREAM_NAME_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "streamName");
    static final QName STREAM_VERSION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "streamVersion");
    static final QName ATTRIBUTES_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attributes");
    static final QName ATTRIBUTE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "attribute");
    static final QName NAMESPACE_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "namespace");
    static final QName NAMESPACES_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "namespaces");
    static final QName META_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "meta");
    static final QName CORILATION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "correlation");
    static final QName PLAYLOAD_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "payload");
    static final QName NAME_Q = new QName("name");
    static final QName VALUE_Q = new QName("value");
    static final QName TYPE_Q = new QName("type");
    static final QName DEFAULT_Q = new QName("default");
    static final QName PREFIX_Q = new QName("prefix");
    static final QName EXPRESSION_Q = new QName("expression");
    static final QName URI_Q = new QName("uri");

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

        StreamConfiguration streamConfiguration = new StreamConfiguration();

        OMElement streamName = omElement.getFirstChildWithName(STREAM_NAME_Q);
        if (streamName == null) {
            throw new SynapseException(STREAM_NAME_Q.getLocalPart() + " element missing");
        }
        streamConfiguration.setName(streamName.getText());

        OMElement streamVersion = omElement.getFirstChildWithName(STREAM_VERSION_Q);
        if (streamVersion == null) {
            throw new SynapseException(STREAM_VERSION_Q.getLocalPart() + " element missing");
        }
        streamConfiguration.setVersion(streamVersion.getText());

        Map<String, String> namespaceMap = new HashMap<String, String>();
        OMElement namespaces = omElement.getFirstChildWithName(NAMESPACES_Q);
        if (namespaces != null) {
            Iterator iter = namespaces.getChildrenWithName(NAMESPACE_Q);

            while (iter.hasNext()) {
                OMElement namespaceElement = (OMElement) iter.next();

                OMAttribute prefixAtr = namespaceElement.getAttribute(PREFIX_Q);
                if (prefixAtr == null) {
                    throw new SynapseException(PREFIX_Q.getLocalPart() + " attribute missing in " + NAMESPACE_Q.getLocalPart());
                }
                OMAttribute uriAtr = namespaceElement.getAttribute(URI_Q);
                if (uriAtr == null) {
                    throw new SynapseException(URI_Q.getLocalPart() + " attribute missing in " + NAMESPACE_Q.getLocalPart());
                }
                namespaceMap.put(prefixAtr.getAttributeValue(), uriAtr.getAttributeValue());
            }
        }

        OMElement attributes = omElement.getFirstChildWithName(ATTRIBUTES_Q);
        if (attributes != null) {
            OMElement meta = attributes.getFirstChildWithName(META_Q);
            if (meta != null) {
                List<Property> propertyList = new ArrayList<Property>();
                Iterator iter = meta.getChildrenWithName(ATTRIBUTE_Q);
                populateAttributes(propertyList, iter, META_Q, namespaceMap);
                streamConfiguration.setMetaProperties(propertyList);
            }
            OMElement correlation = attributes.getFirstChildWithName(CORILATION_Q);
            if (correlation != null) {
                List<Property> propertyList = new ArrayList<Property>();
                Iterator iter = correlation.getChildrenWithName(ATTRIBUTE_Q);
                populateAttributes(propertyList, iter, CORILATION_Q, namespaceMap);
                streamConfiguration.setCorrelationProperties(propertyList);
            }
            OMElement payload = attributes.getFirstChildWithName(PLAYLOAD_Q);
            if (payload != null) {
                List<Property> propertyList = new ArrayList<Property>();
                Iterator iter = payload.getChildrenWithName(ATTRIBUTE_Q);
                populateAttributes(propertyList, iter, PLAYLOAD_Q, namespaceMap);
                streamConfiguration.setPayloadProperties(propertyList);
            }
        } else {
            throw new SynapseException(ATTRIBUTES_Q.getLocalPart() + " attribute missing");
        }

        mediator.getStream().setStreamConfiguration(streamConfiguration);

        String endpointConfigString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<serverProfile xmlns=\"http://ws.apache.org/ns/synapse\">\n" +
                "    <connection loadbalancer=\"false\" secure=\"true\" urlSet=\"\" ip=\"localhost\" authPort=\"7711\" receiverPort=\"\" />\n" +
                "    <credential userName=\"admin\" password=\"kuv2MubUUveMyv6GeHrXr9il59ajJIqUI4eoYHcgGKf/BBFOWn96NTjJQI+wYbWjKW6r79S7L7ZzgYeWx7DlGbff5X3pBN2Gh9yV0BHP1E93QtFqR7uTWi141Tr7V7ZwScwNqJbiNoV+vyLbsqKJE7T3nP8Ih9Y6omygbcLcHzg=\" />\n" +
                "</serverProfile>";

        try {
            OMElement resourceElement = new StAXOMBuilder(new ByteArrayInputStream(endpointConfigString.getBytes(Charset.forName("UTF-8")))).getDocumentElement();
            ThriftEndpointConfig endpointConfig = ThriftEndpointConfigBuilder.createThriftEndpointConfig(resourceElement);
            mediator.getStream().setThriftEndpointConfig(endpointConfig);
        } catch (XMLStreamException e) {
            String errorMsg = "Failed to create XML OMElement from the String. " + e.getMessage();
            log.error(errorMsg, e);
        }

        return mediator;
    }

    private void populateAttributes(List<Property> propertyList, Iterator iter, QName qName, Map<String, String> namespaceMap) {
        while (iter.hasNext()) {
            OMElement element = (OMElement) iter.next();
            OMAttribute nameAtr = element.getAttribute(NAME_Q);
            if (nameAtr == null) {
                throw new SynapseException(NAME_Q.getLocalPart() + " attribute missing in " + qName.getLocalPart());
            }
            OMAttribute typeAtr = element.getAttribute(TYPE_Q);
            if (typeAtr == null) {
                throw new SynapseException(TYPE_Q.getLocalPart() + " attribute missing in " + qName.getLocalPart());
            }
            OMAttribute valueAtr = element.getAttribute(VALUE_Q);
            if (valueAtr == null) {
                throw new SynapseException(VALUE_Q.getLocalPart() + " attribute missing in " + qName.getLocalPart());
            }

            OMAttribute expressionAttr = element.getAttribute(EXPRESSION_Q);
            boolean isExpression = (expressionAttr == null || "true".equals(expressionAttr.getAttributeValue()));

            OMAttribute defaultAtr = element.getAttribute(DEFAULT_Q);
            String defaultAtrValue = "";
            if (defaultAtr != null) {
                defaultAtrValue = defaultAtr.getAttributeValue();
            }

            //attributeList.add(new Attribute(nameAtr.getAttributeValue(), getType(typeAtr.getAttributeValue())));

            Property property = new Property();
            property.setKey(nameAtr.getAttributeValue());
            property.setValue(valueAtr.getAttributeValue());
            property.setDefaultValue(defaultAtrValue);
            property.setType(typeAtr.getAttributeValue());
            property.setExpression(isExpression);
            if (isExpression) {
                property.generateXPath(namespaceMap);
            }
            propertyList.add(property);
        }
    }

    private boolean isNotNullOrEmpty(String string) {
        return string != null && !string.equals("");
    }
}
