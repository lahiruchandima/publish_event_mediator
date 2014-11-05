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

import org.apache.synapse.config.xml.AbstractMediatorSerializer;
import org.apache.synapse.Mediator;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.config.xml.SynapseXPathSerializer;

public class PublishEventMediatorSerializer extends AbstractMediatorSerializer {

    @Override
    public OMElement serializeSpecificMediator(Mediator mediator) {
        assert mediator instanceof PublishEventMediator : PublishEventMediatorFactory.getTagName() + " mediator is expected";

        PublishEventMediator publishEventMediator = (PublishEventMediator) mediator;
        OMElement mediatorElement = fac.createOMElement(PublishEventMediatorFactory.getTagName(), synNS);

        OMElement serverProfileElement = fac.createOMElement(PublishEventMediatorFactory.EVENT_SINK_Q.getLocalPart(), synNS);
        serverProfileElement.setText(publishEventMediator.getEventSink());
        mediatorElement.addChild(serverProfileElement);

        OMElement streamNameElement = fac.createOMElement(PublishEventMediatorFactory.STREAM_NAME_Q.getLocalPart(), synNS);
        streamNameElement.setText(publishEventMediator.getStreamName());
        mediatorElement.addChild(streamNameElement);

        OMElement streamVersionElement = fac.createOMElement(PublishEventMediatorFactory.STREAM_VERSION_Q.getLocalPart(), synNS);
        streamVersionElement.setText(publishEventMediator.getStreamVersion());
        mediatorElement.addChild(streamVersionElement);

        OMElement streamAttributesElement = fac.createOMElement(PublishEventMediatorFactory.ATTRIBUTES_Q.getLocalPart(), synNS);

        OMElement metaAttributesElement = fac.createOMElement(PublishEventMediatorFactory.META_Q.getLocalPart(), synNS);
        for (Property property : publishEventMediator.getMetaProperties()) {
            metaAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(metaAttributesElement);

        OMElement correlationAttributesElement = fac.createOMElement(PublishEventMediatorFactory.CORRELATION_Q.getLocalPart(), synNS);
        for (Property property : publishEventMediator.getCorrelationProperties()) {
            correlationAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(correlationAttributesElement);

        OMElement payloadAttributesElement = fac.createOMElement(PublishEventMediatorFactory.PAYLOAD_Q.getLocalPart(), synNS);
        for (Property property : publishEventMediator.getPayloadProperties()) {
            payloadAttributesElement.addChild(createElementForProperty(property));
        }
        streamAttributesElement.addChild(payloadAttributesElement);

        mediatorElement.addChild(streamAttributesElement);

        return mediatorElement;
    }

    @Override
    public String getMediatorClassName() {
        return PublishEventMediator.class.getName();
    }

    private OMElement createElementForProperty(Property property) {
        OMElement attributeElement = fac.createOMElement(PublishEventMediatorFactory.ATTRIBUTE_Q.getLocalPart(), synNS);
        attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediatorFactory.getNameAttributeQ().getLocalPart(), nullNS, property.getKey()));
        attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediatorFactory.TYPE_Q.getLocalPart(), nullNS, property.getType()));
        attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediatorFactory.DEFAULT_Q.getLocalPart(), nullNS, property.getDefaultValue()));

        if (property.getExpression() != null) {
            SynapseXPathSerializer.serializeXPath(property.getExpression(), attributeElement, PublishEventMediatorFactory.getExpressionAttributeQ().getLocalPart());
        } else {
            attributeElement.addAttribute(fac.createOMAttribute(PublishEventMediatorFactory.getValueAttributeQ().getLocalPart(), nullNS, property.getValue()));
        }
        return attributeElement;
    }
}