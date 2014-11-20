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
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.databridge.agent.thrift.exception.AgentException;
import org.wso2.carbon.event.sink.EventSink;

import java.util.ArrayList;
import java.util.List;

/**
 * Mediator that extracts data from current message payload/header according to the given configuration.
 * Extracted information is sent as an event.
 */
public class PublishEventMediator extends AbstractMediator {

	private static final String ADMIN_SERVICE_PARAMETER = "adminService";
	private static final String HIDDEN_SERVICE_PARAMETER = "hiddenService";

	private String streamName = "";
	private String streamVersion = "";
	private List<Property> metaProperties = new ArrayList<Property>();
	private List<Property> correlationProperties = new ArrayList<Property>();
	private List<Property> payloadProperties = new ArrayList<Property>();
	private EventSink eventSink;

	@Override
	public boolean isContentAware() {
		return true;
	}

	/**
	 * This is called when a new message is received for mediation.
	 * Extracts data from message to construct an event based on the mediator configuration
	 * Sends the constructed event to the event sink specified in mediator configuration
	 *
	 * @param messageContext Message context of the message to be mediated
	 * @return Always returns true. (instructs to proceed with next mediator)
	 */
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

			org.apache.axis2.context.MessageContext msgContext =
					((Axis2MessageContext) messageContext).getAxis2MessageContext();

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

			eventSink.getLoadBalancingDataPublisher()
			         .publish(streamName, streamVersion, metaData, correlationData, payloadData);

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

	public EventSink getEventSink() {
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

	public void setEventSink(EventSink eventSink) {
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
}