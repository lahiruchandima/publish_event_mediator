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

package org.wso2.carbon.mediator.publishevent.xml;

import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.Mediator;
import org.apache.axiom.om.OMElement;
import org.wso2.carbon.mediator.publishevent.Property;
import org.wso2.carbon.mediator.publishevent.PublishEventMediator;
import org.wso2.carbon.mediator.publishevent.StreamConfiguration;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PublishEventMediatorSerializer extends AbstractMediatorSerializer {

    @Override
    public OMElement serializeSpecificMediator(Mediator mediator) {
        assert mediator instanceof PublishEventMediator : PublishEventMediatorFactory.getTagName() + " mediator is expected";

        PublishEventMediator publishEventMediator = (PublishEventMediator) mediator;
        OMElement mediatorElement = fac.createOMElement(PublishEventMediatorFactory.getTagName(), synNS);

        OMElement serverProfileElement = fac.createOMElement("serverProfile", synNS);
        serverProfileElement.addAttribute(fac.createOMAttribute("name", nullNS, publishEventMediator.getServerProfile().split("/")[publishEventMediator.getServerProfile().split("/").length - 1]));
        mediatorElement.addChild(serverProfileElement);

        StreamConfiguration streamConfig = publishEventMediator.getStream().getStreamConfiguration();

        OMElement streamNameElement = fac.createOMElement(PublishEventMediatorFactory.STREAM_NAME_Q.getLocalPart(), synNS);
        streamNameElement.setText(streamConfig.getName());
        mediatorElement.addChild(streamNameElement);

        OMElement streamVersionElement = fac.createOMElement(PublishEventMediatorFactory.STREAM_VERSION_Q.getLocalPart(), synNS);
        streamVersionElement.setText(streamConfig.getVersion());
        mediatorElement.addChild(streamVersionElement);

        OMElement streamNicknameElement = fac.createOMElement(PublishEventMediatorFactory.STREAM_NICKNAME_Q.getLocalPart(), synNS);
        streamNicknameElement.setText(streamConfig.getNickname());
        mediatorElement.addChild(streamNicknameElement);

        OMElement streamDescriptionElement = fac.createOMElement(PublishEventMediatorFactory.STREAM_DESCRIPTION_Q.getLocalPart(), synNS);
        streamDescriptionElement.setText(streamConfig.getDescription());
        mediatorElement.addChild(streamDescriptionElement);

        OMElement streamAttributesElement = fac.createOMElement(PublishEventMediatorFactory.ATTRIBUTES_Q.getLocalPart(), synNS);

        OMElement metaAttributesElement = fac.createOMElement(PublishEventMediatorFactory.META_Q.getLocalPart(), synNS);
        List<Property> metaProperties = streamConfig.getMetaProperties();
        for (Property property : metaProperties) {
            metaAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(metaAttributesElement);

        OMElement correlationAttributesElement = fac.createOMElement(PublishEventMediatorFactory.CORRELATION_Q.getLocalPart(), synNS);
        List<Property> correlationProperties = streamConfig.getCorrelationProperties();
        for (Property property : correlationProperties) {
            correlationAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(correlationAttributesElement);

        OMElement payloadAttributesElement = fac.createOMElement(PublishEventMediatorFactory.PAYLOAD_Q.getLocalPart(), synNS);
        List<Property> payloadProperties = streamConfig.getPayloadProperties();
        for (Property property : payloadProperties) {
            payloadAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(payloadAttributesElement);

        mediatorElement.addChild(streamAttributesElement);

        OMElement namespacesElement = fac.createOMElement(PublishEventMediatorFactory.NAMESPACES_Q.getLocalPart(), synNS);
        Set<Map.Entry<String, String>> namespaceEntrySet = streamConfig.getNamespaceMap().entrySet();
        for (Map.Entry<String, String> namespaceEntry : namespaceEntrySet) {
            OMElement namespaceElement = fac.createOMElement(PublishEventMediatorFactory.NAMESPACE_Q.getLocalPart(), synNS);
            namespaceElement.addAttribute(fac.createOMAttribute(PublishEventMediatorFactory.PREFIX_Q.getLocalPart(), nullNS, namespaceEntry.getKey()));
            namespaceElement.addAttribute(fac.createOMAttribute(PublishEventMediatorFactory.URI_Q.getLocalPart(), nullNS, namespaceEntry.getValue()));
            namespacesElement.addChild(namespaceElement);
        }
        mediatorElement.addChild(namespacesElement);

        return mediatorElement;
    }

    @Override
    public String getMediatorClassName() {
        return PublishEventMediator.class.getName();
    }

    private OMElement createElementForProperty(Property property) {
        OMElement attributeElement = fac.createOMElement(PublishEventMediatorFactory.ATTRIBUTE_Q.getLocalPart(), synNS);
        attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediatorFactory.NAME_Q.getLocalPart(), nullNS, property.getKey()));
        attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediatorFactory.TYPE_Q.getLocalPart(), nullNS, property.getType()));
        attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediatorFactory.DEFAULT_Q.getLocalPart(), nullNS, property.getDefaultValue()));
        attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediatorFactory.VALUE_Q.getLocalPart(), nullNS, property.getValue()));
        attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediatorFactory.EXPRESSION_Q.getLocalPart(), nullNS, property.isExpression() ? "true" : "false"));
        return attributeElement;
    }
}
