/*
 * Copyright (c) 2006, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
            if (streamConfiguration != null){
                streamDef = new StreamDefinition(this.streamConfiguration.getName(), this.streamConfiguration.getVersion());
                streamDef.setNickName(this.streamConfiguration.getNickname());
                streamDef.setDescription(this.streamConfiguration.getDescription());
                streamDef.setCorrelationData(this.getCorrelationDataList());
                streamDef.setMetaData(this.getMetaDataList());
                streamDef.setPayloadData(this.getPayloadDataList());
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

    private List<Attribute> getCorrelationDataList() throws PublishEventMediatorException {
        try{
            List<Attribute> correlationDataAttributeList = new ArrayList<Attribute>();
            correlationDataAttributeList.add(new Attribute(Constants.MSG_STR_ACTIVITY_ID, AttributeType.STRING));
            return correlationDataAttributeList;
        } catch (Exception e) {
            String errorMsg = "Error occurred while getting the Correlation Data list. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }

    private List<Attribute> getMetaDataList() throws PublishEventMediatorException {
        List<Attribute> metaDataAttributeList = new ArrayList<Attribute>();
        try{
            metaDataAttributeList.add(new Attribute(Constants.TENANT_ID, AttributeType.INT));
            metaDataAttributeList.add(new Attribute(Constants.HTTP_METHOD, AttributeType.STRING));
            metaDataAttributeList.add(new Attribute(Constants.CHARACTER_SET_ENCODING, AttributeType.STRING));
            metaDataAttributeList.add(new Attribute(Constants.REMOTE_ADDRESS, AttributeType.STRING));
            metaDataAttributeList.add(new Attribute(Constants.TRANSPORT_IN_URL, AttributeType.STRING));
            metaDataAttributeList.add(new Attribute(Constants.MESSAGE_TYPE, AttributeType.STRING));
            metaDataAttributeList.add(new Attribute(Constants.REMOTE_HOST, AttributeType.STRING));
            metaDataAttributeList.add(new Attribute(Constants.SERVICE_PREFIX, AttributeType.STRING));
            metaDataAttributeList.add(new Attribute(Constants.HOST, AttributeType.STRING));
            return metaDataAttributeList;
        } catch (Exception e) {
            String errorMsg = "Error occurred while getting the Meta Data list. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }

    private List<Attribute> getPayloadDataList() throws PublishEventMediatorException {
        List<Attribute> payLoadDataAttributeList = new ArrayList<Attribute>();
        try{
            this.addConstantPayloadToPayloadDataList(payLoadDataAttributeList);
            this.addPropertyPayloadToPayloadDataList(payLoadDataAttributeList);
            this.addEntityPayloadToPayloadDataList(payLoadDataAttributeList);
            return payLoadDataAttributeList;
        } catch (Exception e) {
            String errorMsg = "Error occurred while getting the Payload Data list. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }

    private void addConstantPayloadToPayloadDataList(List<Attribute> attributes) throws PublishEventMediatorException {
        try{
            attributes.add(new Attribute(Constants.MSG_DIRECTION, AttributeType.STRING));
            attributes.add(new Attribute(Constants.SERVICE_NAME, AttributeType.STRING));
            attributes.add(new Attribute(Constants.OPERATION_NAME, AttributeType.STRING));
            attributes.add(new Attribute(Constants.MSG_ID, AttributeType.STRING));
            attributes.add(new Attribute(Constants.REQUEST_RECEIVED_TIME, AttributeType.LONG));
        } catch (Exception e) {
            String errorMsg = "Error occurred while adding the Constant Fields to Payload Data list. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }

    private void addPropertyPayloadToPayloadDataList(List<Attribute> attributes) throws PublishEventMediatorException {
        try{
            List<Property> properties = this.streamConfiguration.getProperties();
            if (properties != null) {
                for (Property property : properties) {
                    if(property.getKey() != null && property.getType() != null){
                        attributes.add(new Attribute(property.getKey(), this.resolveTypes(property.getType())));
                    }
                }
            }
        } catch (NullPointerException e) {
            String errorMsg = "Undefined key or type in Stream Property. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error occurred while adding the Property Fields to Payload Data list. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }

    private void addEntityPayloadToPayloadDataList(List<Attribute> attributes) throws PublishEventMediatorException {
        try{
            String entryName = "";
            List<StreamEntry> streamEntries = this.streamConfiguration.getEntries();
            if (streamEntries != null) {
                for (StreamEntry streamEntry : streamEntries) {
                    if(streamEntry.getName() != null && streamEntry.getType() != null){
                        if("SOAPBody".equals(streamEntry.getName())){
                            entryName = "soap_body";
                        } else if("SOAPHeader".equals(streamEntry.getName())){
                            entryName = "soap_header";
                        }
                        attributes.add(new Attribute(entryName, this.resolveTypes(streamEntry.getType())));
                    }
                }
            }
        } catch (NullPointerException e) {
            String errorMsg = "Undefined key or type in Stream Entry. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error occurred while adding the Entity Fields to Payload Data list. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }

    private AttributeType resolveTypes(String propertyType) throws PublishEventMediatorException {
        try{
            if ("STRING".equals(propertyType)){
                return AttributeType.STRING;
            } else if ("INTEGER".equals(propertyType)) {
                return AttributeType.INT;
            } else if ("FLOAT".equals(propertyType)) {
                return AttributeType.FLOAT;
            } else if ("DOUBLE".equals(propertyType)) {
                return AttributeType.DOUBLE;
            } else if ("BOOLEAN".equals(propertyType)) {
                return AttributeType.BOOL;
            } else if ("LONG".equals(propertyType)) {
                return AttributeType.LONG;
            } else {
                return AttributeType.STRING;
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred while resolving types for Payload Data list. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }
}
