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

package org.wso2.carbon.mediator.publishevent.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.mediator.publishevent.Property;
import org.wso2.carbon.mediator.publishevent.PublishEventMediatorException;
import org.wso2.carbon.mediator.publishevent.StreamConfiguration;
import org.wso2.carbon.mediator.publishevent.StreamEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the Stream Definition at the stream initialization
 */
public class StreamDefinitionBuilder {

    private StreamConfiguration streamConfiguration = null;
    private static Log log = LogFactory.getLog(StreamDefinitionBuilder.class);

    public StreamDefinition buildStreamDefinition(StreamConfiguration streamConfiguration) throws PublishEventMediatorException {
        this.streamConfiguration = streamConfiguration;
        StreamDefinition streamDef;
        try {
            if (streamConfiguration != null) {
                streamDef = new StreamDefinition(this.streamConfiguration.getName(), this.streamConfiguration.getVersion());
                streamDef.setNickName(this.streamConfiguration.getNickname());
                streamDef.setDescription(this.streamConfiguration.getDescription());
                streamDef.setCorrelationData(this.getAttributeList(streamConfiguration.getCorrelationProperties()));
                streamDef.setMetaData(this.getAttributeList(streamConfiguration.getMetaProperties()));
                streamDef.setPayloadData(this.getAttributeList(streamConfiguration.getPayloadProperties()));
                return streamDef;
            } else {
                String errorMsg = "Stream Definition is null.";
                log.error(errorMsg);
                throw new PublishEventMediatorException(errorMsg, new Exception());
            }
        } catch (MalformedStreamDefinitionException e) {
            String errorMsg = "Malformed Stream Definition. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error occurred while creating the Stream Definition. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }

    private List<Attribute> getAttributeList(List<Property> propertyList) {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        for (Property property : propertyList) {
            attributeList.add(new Attribute(property.getKey(), property.getDatabridgeAttributeType()));
        }
        return attributeList;
    }
}
