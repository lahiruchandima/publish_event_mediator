/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.apache.axis2.description.AxisService;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.databridge.agent.thrift.lb.DataPublisherHolder;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.databridge.agent.thrift.lb.ReceiverGroup;
import org.wso2.carbon.databridge.agent.thrift.util.DataPublisherUtil;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;

import java.util.ArrayList;
import java.util.List;

/**
 * Extracts the current message payload/header data according to the given configuration.
 * Extracted information is sent as an event.
 */
public class PublishEventMediator extends AbstractMediator implements ManagedLifecycle {

    private static final String ADMIN_SERVICE_PARAMETER = "adminService";
    private static final String HIDDEN_SERVICE_PARAMETER = "hiddenService";

    private String eventSink = "";
    private String streamName = "";
    private String streamVersion = "";
    private List<Property> metaProperties = new ArrayList<Property>();
    private List<Property> correlationProperties = new ArrayList<Property>();
    private List<Property> payloadProperties = new ArrayList<Property>();
    private LoadBalancingDataPublisher loadBalancingDataPublisher;
    private ThriftEndpointConfig thriftEndpointConfig;

    @Override
    public void init(SynapseEnvironment synapseEnvironment) {
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

        StreamDefinition streamDef;
        try {
            streamDef = new StreamDefinition(streamName, streamVersion);
            streamDef.setCorrelationData(generateAttributeList(correlationProperties));
            streamDef.setMetaData(generateAttributeList(metaProperties));
            streamDef.setPayloadData(generateAttributeList(payloadProperties));
        } catch (MalformedStreamDefinitionException e) {
            String errorMsg = "Malformed Stream Definition. " + e.getMessage();
            log.error(errorMsg, e);
            throw new SynapseException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error occurred while creating the Stream Definition. " + e.getMessage();
            log.error(errorMsg, e);
            throw new SynapseException(errorMsg, e);
        }
        loadBalancingDataPublisher.addStreamDefinition(streamDef);
        log.info("Data Publisher Created.");
    }

    @Override
    public void destroy() {
        if (loadBalancingDataPublisher != null) {
            loadBalancingDataPublisher.stop();
        }
    }

    @Override
    public boolean isContentAware() {
        return true;
    }

    @Override
    public boolean mediate(MessageContext messageContext) {
        SynapseLog synLog = getLog(messageContext);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Start : " + PublishEventMediatorFactory.getTagName() + " mediator");
            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }

        if (messageContext instanceof Axis2MessageContext) {

            org.apache.axis2.context.MessageContext msgContext = ((Axis2MessageContext) messageContext).getAxis2MessageContext();

            AxisService service = msgContext.getAxisService();
            if (service == null) {
                return true;
            }
            // When this is not inside an API theses parameters should be there
            if ((!service.getName().equals("__SynapseService")) &&
                    (service.getParameter(ADMIN_SERVICE_PARAMETER) != null ||
                            service.getParameter(HIDDEN_SERVICE_PARAMETER) != null)) {
                return true;
            }
        }
        ActivityIDSetter.setActivityIdInTransportHeader(messageContext);

        try {
            Object[] metaData = new Object[metaProperties.size()];
            for (int i = 0; i < metaProperties.size(); ++i) {
                metaData[i] = metaProperties.get(i).extractPropertyValue(messageContext);
            }

            Object[] correlationData = new Object[correlationProperties.size()];
            for (int i = 0; i < correlationProperties.size(); ++i) {
                correlationData[i] = correlationProperties.get(i).extractPropertyValue(messageContext);
            }

            Object[] payloadData = new Object[payloadProperties.size()];
            for (int i = 0; i < payloadProperties.size(); ++i) {
                payloadData[i] = payloadProperties.get(i).extractPropertyValue(messageContext);
            }

            loadBalancingDataPublisher.publish(streamName, streamVersion, metaData, correlationData, payloadData);

        } catch (AgentException e) {
            String errorMsg = "Agent error occurred while sending the event. " + e.getMessage();
            log.error(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Error occurred while sending the event. " + e.getMessage();
            log.error(errorMsg, e);
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : " + PublishEventMediatorFactory.getTagName() + " mediator");
            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }

        return true;
    }

    private List<Attribute> generateAttributeList(List<Property> propertyList) {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        for (Property property : propertyList) {
            attributeList.add(new Attribute(property.getKey(), property.getDatabridgeAttributeType()));
        }
        return attributeList;
    }

    public String getEventSink() {
        return eventSink;
    }

    public String getStreamName() {
        return streamName;
    }

    public String getStreamVersion() {
        return streamVersion;
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

    public void setEventSink(String eventSink) {
        this.eventSink = eventSink;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public void setStreamVersion(String streamVersion) {
        this.streamVersion = streamVersion;
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

    public void setThriftEndpointConfig(ThriftEndpointConfig thriftEndpointConfig) {
        this.thriftEndpointConfig = thriftEndpointConfig;
    }
}