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
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
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
    public static final String ENABLE_MEDIATION_STATS = "EnableMediationStats";
    public static final String CLOUD_DEPLOYMENT_PROP = "IsCloudDeployment";
    public static final String SERVER_CONFIG_BAM_URL = "BamServerURL";
    public static final String DEFAULT_BAM_SERVER_URL = "tcp://127.0.0.1:7611";
    private AsyncDataPublisher asyncDataPublisher;
    private LoadBalancingDataPublisher loadBalancingDataPublisher;
    private boolean isPublisherCreated;
    private ThriftEndpointConfig thriftEndpointConfig;
    private StreamConfiguration streamConfiguration;

    public Stream() {
        loadBalancingDataPublisher = null;
        isPublisherCreated = false;
    }

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

    //TODO: clarify with maninda - if cloud, asyncDataPublisher is not created but it is used in setStreamDefinitionToDataPublisher
    private void createDataPublisher() throws SynapseException {
        if (isCloudDeployment()) { // In Stratos environment
            createLoadBalancingDataPublisher(getServerConfigBAMServerURL(),
                    thriftEndpointConfig.getUsername(),
                    thriftEndpointConfig.getPassword());
            /*asyncDataPublisher = new AsyncDataPublisher(getServerConfigBAMServerURL(),
                                                        bamServerConfig.getUsername(),
                                                        bamServerConfig.getPassword());*/
        } else { // In normal Carbon environment
            if (thriftEndpointConfig.isLoadbalanced()) {
                createLoadBalancingDataPublisher(thriftEndpointConfig.getUrlSet(),
                        thriftEndpointConfig.getUsername(),
                        thriftEndpointConfig.getPassword());
            } else {
                if (thriftEndpointConfig.isSecure()) {
                    asyncDataPublisher = new AsyncDataPublisher(
                            "ssl://" + thriftEndpointConfig.getIp() + ":" + thriftEndpointConfig.getAuthenticationPort(),
                            "ssl://" + thriftEndpointConfig.getIp() + ":" + thriftEndpointConfig.getAuthenticationPort(),
                            thriftEndpointConfig.getUsername(), thriftEndpointConfig.getPassword());
                } else {
                    asyncDataPublisher = new AsyncDataPublisher(
                            "ssl://" + thriftEndpointConfig.getIp() + ":" + thriftEndpointConfig.getAuthenticationPort(),
                            "tcp://" + thriftEndpointConfig.getIp() + ":" + thriftEndpointConfig.getReceiverPort(),
                            thriftEndpointConfig.getUsername(), thriftEndpointConfig.getPassword());
                }
            }
        }

        log.info("Data Publisher Created.");
    }

    private void createLoadBalancingDataPublisher(String urlSet, String username, String password) throws SynapseException {
        ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
        ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(urlSet);

        for (String aReceiverGroupURL : receiverGroupUrls) {
            ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
            String[] failOverUrls = aReceiverGroupURL.split("\\|");
            String[] lbURLs = aReceiverGroupURL.split(",");
            if (failOverUrls.length == 1) {
                for (String aUrl : lbURLs) {
                    DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), username, password);
                    dataPublisherHolders.add(aNode);
                }
                ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
                allReceiverGroups.add(group);
            } else if (lbURLs.length != 1) {
                throw new SynapseException("You can either have fali over URLs or load balancing URLS in one receiver group",
                        new Exception("You can either have fali over URLs or load balancing URLS in one receiver group"));
            } else {
                for (String aUrl : failOverUrls) {
                    DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), username, password);
                    dataPublisherHolders.add(aNode);
                }
                ReceiverGroup group = new ReceiverGroup(dataPublisherHolders, true);
                allReceiverGroups.add(group);
            }
        }
        loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);
    }

    private String getServerConfigBAMServerURL() {
        String[] bamServerUrl = ServerConfiguration.getInstance().getProperties(SERVER_CONFIG_BAM_URL);
        if (null != bamServerUrl) {
            return bamServerUrl[bamServerUrl.length - 1];
        } else {
            return DEFAULT_BAM_SERVER_URL;
        }
    }

    private boolean isCloudDeployment() {
        String[] cloudDeploy = ServerConfiguration.getInstance().getProperties(CLOUD_DEPLOYMENT_PROP);
        return null != cloudDeploy && Boolean.parseBoolean(cloudDeploy[cloudDeploy.length - 1]);
    }

    private void setStreamDefinitionToDataPublisher() throws SynapseException {
        try {
            StreamDefinition streamDef = buildStreamDefinition(streamConfiguration);
            if (thriftEndpointConfig.isLoadbalanced()) {
                loadBalancingDataPublisher.addStreamDefinition(streamDef);
            } else {
                asyncDataPublisher.addStreamDefinition(streamDef);
            }
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

            if (thriftEndpointConfig.isLoadbalanced()) {
                loadBalancingDataPublisher.publish(streamConfiguration.getName(),streamConfiguration.getVersion(),
                        metaData, correlationData, payloadData);
            } else {
                if (!asyncDataPublisher.canPublish()) {
                    asyncDataPublisher.reconnect();
                }
                asyncDataPublisher.publish(streamConfiguration.getName(), streamConfiguration.getVersion(),
                        metaData, correlationData, payloadData);
            }

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
