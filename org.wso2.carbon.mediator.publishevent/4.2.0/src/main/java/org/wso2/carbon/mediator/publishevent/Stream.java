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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.mediator.publishevent.util.ActivityIDSetter;

import java.util.ArrayList;
import java.util.List;

/**
 * This is the main class of the Event Stream that extract data from mediator and send events.
 */
public class Stream {
    private static final Log log = LogFactory.getLog(Stream.class);
    private LoadBalancingDataPublisher loadBalancingDataPublisher;
    private boolean isPublisherCreated;
    private ThriftEndpointConfig thriftEndpointConfig;
    private StreamConfiguration streamConfiguration;

    public void sendEvents(MessageContext messageContext) throws SynapseException {
        ActivityIDSetter activityIDSetter = new ActivityIDSetter();
        activityIDSetter.setActivityIdInTransportHeader(messageContext);
        try {
            if (!isPublisherCreated) {
                initializeDataPublisher(this);
                isPublisherCreated = true;
            }
            publishEvent(messageContext);
        } catch (SynapseException e) {
            String errorMsg = "Problem occurred while logging events in the BAM Mediator. " + e.getMessage();
            log.error(errorMsg, e);
            throw new SynapseException(errorMsg, e);
        }
    }

    private synchronized static void initializeDataPublisher(Stream stream) throws SynapseException {
        try {
            if (!stream.isPublisherCreated) {
                stream.createDataPublisher();
                stream.setStreamDefinitionToDataPublisher();
                stream.isPublisherCreated = true;
            }
        } catch (SynapseException e) {
            String errorMsg = "Problem initializing the Data Publisher or Stream Definition. " + e.getMessage();
            log.error(errorMsg, e);
            throw new SynapseException(errorMsg, e);
        }
    }

    private void createDataPublisher() throws SynapseException {
        ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
        ArrayList<String> receiverUrlGroups = DataPublisherUtil.getReceiverGroups(thriftEndpointConfig.getReceiverUrlSet());

        ArrayList<String> authenticatorUrlGroups = null;
        if (thriftEndpointConfig.getAuthenticationUrlSet() != null && thriftEndpointConfig.getAuthenticationUrlSet().length() > 0) {
            authenticatorUrlGroups = DataPublisherUtil.getReceiverGroups(thriftEndpointConfig.getAuthenticationUrlSet());
            if (authenticatorUrlGroups.size() != receiverUrlGroups.size()) {
                throw new SynapseException("Receiver URL group count is not equal to Authenticator URL group count." +
                        " Receiver URL groups: " + thriftEndpointConfig.getReceiverUrlSet() + " & Authenticator URL " +
                        " groups: " + thriftEndpointConfig.getAuthenticationUrlSet());
            }
        }

        for (int i = 0; i < receiverUrlGroups.size(); ++i) {
            String receiverGroup = receiverUrlGroups.get(i);
            String[] receiverUrls = receiverGroup.split(",");
            String[] authenticatorUrls = new String[receiverUrls.length];

            if (authenticatorUrlGroups != null) {
                String authenticatorGroup = authenticatorUrlGroups.get(i);
                authenticatorUrls = authenticatorGroup.split(",");
                if (receiverUrls.length != authenticatorUrls.length) {
                    throw new SynapseException("Receiver URL count is not equal to Authenticator URL count. Receiver"
                            + " URL group: " + receiverGroup + ", authenticator URL group: " + authenticatorGroup);
                }
            }

            ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
            for (int j = 0; j < receiverUrls.length; ++j) {
                DataPublisherHolder holder = new DataPublisherHolder(authenticatorUrls[j], receiverUrls[j],
                        thriftEndpointConfig.getUsername(), thriftEndpointConfig.getPassword());
                dataPublisherHolders.add(holder);
            }
            ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
            allReceiverGroups.add(group);
        }
        loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);


        log.info("Data Publisher Created.");
    }

    private void setStreamDefinitionToDataPublisher() throws SynapseException {
        try {
            StreamDefinition streamDef = buildStreamDefinition(streamConfiguration);
            loadBalancingDataPublisher.addStreamDefinition(streamDef);
        } catch (SynapseException e) {
            String errorMsg = "Error while creating the Asynchronous/LoadBalancing Data Publisher" +
                    "or while creating the Stream Definition. " + e.getMessage();
            log.error(errorMsg, e);
            throw new SynapseException(errorMsg, e);
        }
    }

    private void publishEvent(MessageContext messageContext) throws SynapseException {
        try {
            int metaPropertyCount = streamConfiguration.getMetaProperties().size();
            Object[] metaData = new Object[metaPropertyCount];
            List<Property> metaPropertyList = streamConfiguration.getMetaProperties();
            for (int i = 0; i < metaPropertyCount; ++i) {
                metaData[i] = metaPropertyList.get(i).extractPropertyValue(messageContext);
            }

            int correlationPropertyCount = streamConfiguration.getCorrelationProperties().size();
            Object[] correlationData = new Object[correlationPropertyCount];
            List<Property> correlationPropertyList = streamConfiguration.getCorrelationProperties();
            for (int i = 0; i < correlationPropertyCount; ++i) {
                correlationData[i] = correlationPropertyList.get(i).extractPropertyValue(messageContext);
            }

            int payloadPropertyCount = streamConfiguration.getPayloadProperties().size();
            Object[] payloadData = new Object[payloadPropertyCount];
            List<Property> payloadPropertyList = streamConfiguration.getPayloadProperties();
            for (int i = 0; i < payloadPropertyCount; ++i) {
                payloadData[i] = payloadPropertyList.get(i).extractPropertyValue(messageContext);
            }

            loadBalancingDataPublisher.publish(streamConfiguration.getName(), streamConfiguration.getVersion(),
                    metaData, correlationData, payloadData);

        } catch (AgentException e) {
            String errorMsg = "Agent error occurred while sending the event. " + e.getMessage();
            log.error(errorMsg, e);
            throw new SynapseException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error occurred while sending the event. " + e.getMessage();
            log.error(errorMsg, e);
            throw new SynapseException(errorMsg, e);
        }
    }

    public void setThriftEndpointConfig(ThriftEndpointConfig thriftEndpointConfig) {
        this.thriftEndpointConfig = thriftEndpointConfig;
    }

    public void setStreamConfiguration(StreamConfiguration streamConfiguration) {
        this.streamConfiguration = streamConfiguration;
    }

    public StreamConfiguration getStreamConfiguration() {
        return streamConfiguration;
    }

    private StreamDefinition buildStreamDefinition(StreamConfiguration streamConfiguration) throws SynapseException {
        StreamDefinition streamDef;
        try {
            if (streamConfiguration != null) {
                streamDef = new StreamDefinition(streamConfiguration.getName(), streamConfiguration.getVersion());
                streamDef.setNickName(streamConfiguration.getNickname());
                streamDef.setDescription(streamConfiguration.getDescription());
                streamDef.setCorrelationData(generateAttributeList(streamConfiguration.getCorrelationProperties()));
                streamDef.setMetaData(generateAttributeList(streamConfiguration.getMetaProperties()));
                streamDef.setPayloadData(generateAttributeList(streamConfiguration.getPayloadProperties()));
                return streamDef;
            } else {
                String errorMsg = "Stream Definition is null.";
                log.error(errorMsg);
                throw new SynapseException(errorMsg, new Exception());
            }
        } catch (MalformedStreamDefinitionException e) {
            String errorMsg = "Malformed Stream Definition. " + e.getMessage();
            log.error(errorMsg, e);
            throw new SynapseException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error occurred while creating the Stream Definition. " + e.getMessage();
            log.error(errorMsg, e);
            throw new SynapseException(errorMsg, e);
        }
    }

    private List<Attribute> generateAttributeList(List<Property> propertyList) {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        for (Property property : propertyList) {
            attributeList.add(new Attribute(property.getKey(), property.getDatabridgeAttributeType()));
        }
        return attributeList;
    }
}
