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

package org.wso2.carbon.mediator.publishevent;

import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.databridge.agent.thrift.AsyncDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.mediator.publishevent.builders.CorrelationDataBuilder;
import org.wso2.carbon.mediator.publishevent.builders.MetaDataBuilder;
import org.wso2.carbon.mediator.publishevent.builders.PayloadDataBuilder;
import org.wso2.carbon.mediator.publishevent.util.ActivityIDSetter;
import org.wso2.carbon.mediator.publishevent.util.StreamDefinitionBuilder;

import java.util.ArrayList;

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
    private StreamDefinitionBuilder streamDefinitionBuilder;
    private LoadBalancingDataPublisher loadBalancingDataPublisher;
    private boolean isPublisherCreated;
    private ThriftEndpointConfig thriftEndpointConfig;
    private StreamConfiguration streamConfiguration;
    private PayloadDataBuilder payloadDataBuilder;
    private MetaDataBuilder metaDataBuilder;
    private CorrelationDataBuilder correlationDataBuilder;

    public Stream() {
        streamDefinitionBuilder = new StreamDefinitionBuilder();
        loadBalancingDataPublisher = null;
        isPublisherCreated = false;
        payloadDataBuilder = new PayloadDataBuilder();
        metaDataBuilder = new MetaDataBuilder();
        correlationDataBuilder = new CorrelationDataBuilder();
    }

    public void sendEvents(MessageContext messageContext) throws PublishEventMediatorException {
        ActivityIDSetter activityIDSetter = new ActivityIDSetter();
        activityIDSetter.setActivityIdInTransportHeader(messageContext);
        try {
            if (!isPublisherCreated) {
                initializeDataPublisher(this);
                isPublisherCreated = true;
            }
            publishEvent(messageContext);
        } catch (PublishEventMediatorException e) {
            String errorMsg = "Problem occurred while logging events in the BAM Mediator. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }

    private synchronized static void initializeDataPublisher(Stream stream) throws PublishEventMediatorException {
        try {
            if (!stream.isPublisherCreated) {
                stream.createDataPublisher();
                stream.setStreamDefinitionToDataPublisher();
                stream.isPublisherCreated = true;
            }
        } catch (PublishEventMediatorException e) {
            String errorMsg = "Problem initializing the Data Publisher or Stream Definition. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }

    //TODO: clarify with maninda - if cloud, asyncDataPublisher is not created but it is used in setStreamDefinitionToDataPublisher
    private void createDataPublisher() throws PublishEventMediatorException {
        if (this.isCloudDeployment()) { // In Stratos environment
            this.createLoadBalancingDataPublisher(this.getServerConfigBAMServerURL(),
                    this.thriftEndpointConfig.getUsername(),
                    this.thriftEndpointConfig.getPassword());
            /*asyncDataPublisher = new AsyncDataPublisher(this.getServerConfigBAMServerURL(),
                                                        this.bamServerConfig.getUsername(),
                                                        this.bamServerConfig.getPassword());*/
        } else { // In normal Carbon environment
            if (this.thriftEndpointConfig.isLoadbalanced()) {
                this.createLoadBalancingDataPublisher(this.thriftEndpointConfig.getUrlSet(),
                        this.thriftEndpointConfig.getUsername(),
                        this.thriftEndpointConfig.getPassword());
            } else {
                if (this.thriftEndpointConfig.isSecure()) {
                    asyncDataPublisher = new AsyncDataPublisher("ssl://" + this.thriftEndpointConfig.getIp() + ":" + this.thriftEndpointConfig.getAuthenticationPort(),
                            "ssl://" + this.thriftEndpointConfig.getIp() + ":" + this.thriftEndpointConfig.getAuthenticationPort(),
                            this.thriftEndpointConfig.getUsername(), this.thriftEndpointConfig.getPassword());
                } else {
                    asyncDataPublisher = new AsyncDataPublisher("ssl://" + this.thriftEndpointConfig.getIp() + ":" + this.thriftEndpointConfig.getAuthenticationPort(),
                            "tcp://" + this.thriftEndpointConfig.getIp() + ":" + this.thriftEndpointConfig.getReceiverPort(),
                            this.thriftEndpointConfig.getUsername(), this.thriftEndpointConfig.getPassword());
                }
            }
        }

        log.info("Data Publisher Created.");
    }

    private void createLoadBalancingDataPublisher(String urlSet, String username, String password) throws PublishEventMediatorException {
        ArrayList<ReceiverGroup> allReceiverGroups = new ArrayList<ReceiverGroup>();
        ArrayList<String> receiverGroupUrls = DataPublisherUtil.getReceiverGroups(urlSet);

        for (String aReceiverGroupURL : receiverGroupUrls) {
            ArrayList<DataPublisherHolder> dataPublisherHolders = new ArrayList<DataPublisherHolder>();
            String[] failOverUrls = aReceiverGroupURL.split("\\|");
            String[] lbURLs = aReceiverGroupURL.split(",");
            if (failOverUrls == null || failOverUrls.length == 1) {
                for (String aUrl : lbURLs) {
                    DataPublisherHolder aNode = new DataPublisherHolder(null, aUrl.trim(), username, password);
                    dataPublisherHolders.add(aNode);
                }
                ReceiverGroup group = new ReceiverGroup(dataPublisherHolders);
                allReceiverGroups.add(group);
            } else if (lbURLs != null && lbURLs.length != 1) {
                throw new PublishEventMediatorException("You can either have fali over URLs or load balancing URLS in one receiver group",
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
        this.loadBalancingDataPublisher = new LoadBalancingDataPublisher(allReceiverGroups);
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

    private void setStreamDefinitionToDataPublisher() throws PublishEventMediatorException {
        try {
            StreamDefinition streamDef = this.streamDefinitionBuilder.buildStreamDefinition(this.streamConfiguration);
            if (this.thriftEndpointConfig.isLoadbalanced()) {
                loadBalancingDataPublisher.addStreamDefinition(streamDef);
            } else {
                asyncDataPublisher.addStreamDefinition(streamDef);
            }
        } catch (PublishEventMediatorException e) {
            String errorMsg = "Error while creating the Asynchronous/LoadBalancing Data Publisher" +
                    "or while creating the Stream Definition. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        }
    }

    private void publishEvent(MessageContext messageContext) throws PublishEventMediatorException {
        org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        AxisConfiguration axisConfiguration = msgCtx.getConfigurationContext().getAxisConfiguration();
        try {
            Object[] metaData = this.metaDataBuilder.createMetadata(messageContext, axisConfiguration);
            Object[] correlationData = this.correlationDataBuilder.createCorrelationData(messageContext);
            Object[] payloadData = this.payloadDataBuilder.createPayloadData(messageContext, msgCtx,
                    this.streamConfiguration);

            if (this.thriftEndpointConfig.isLoadbalanced()) {
                loadBalancingDataPublisher.publish(this.streamConfiguration.getName(),
                        this.streamConfiguration.getVersion(), metaData,
                        correlationData, payloadData);
            } else {
                if (!asyncDataPublisher.canPublish()) {
                    asyncDataPublisher.reconnect();
                }
                asyncDataPublisher.publish(this.streamConfiguration.getName(),
                        this.streamConfiguration.getVersion(),
                        metaData, correlationData, payloadData);
            }

        } catch (AgentException e) {
            String errorMsg = "Agent error occurred while sending the event. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error occurred while sending the event. " + e.getMessage();
            log.error(errorMsg, e);
            throw new PublishEventMediatorException(errorMsg, e);
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
}
