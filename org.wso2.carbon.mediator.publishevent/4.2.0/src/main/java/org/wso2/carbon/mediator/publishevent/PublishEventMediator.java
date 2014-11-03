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

import org.apache.axis2.description.AxisService;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.wso2.carbon.mediator.publishevent.xml.PublishEventMediatorFactory;


/**
 * Extracts the current message payload/header data according to the given configuration.
 * Extracted information is sent as an event.
 */
public class PublishEventMediator extends AbstractMediator {

    private static final String ADMIN_SERVICE_PARAMETER = "adminService";
    private static final String HIDDEN_SERVICE_PARAMETER = "hiddenService";

    private String serverProfile = "";
    private Stream stream = new Stream();

    public PublishEventMediator() {
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

        try {
            stream.sendEvents(messageContext);
        } catch (SynapseException e) {
            return true;
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("End : " + PublishEventMediatorFactory.getTagName() + " mediator");

            if (synLog.isTraceTraceEnabled()) {
                synLog.traceTrace("Message : " + messageContext.getEnvelope());
            }
        }

        return true;
    }

    public Stream getStream() {
        return stream;
    }

    public String getServerProfile() {
        return serverProfile;
    }

    public void setServerProfile(String serverProfile) {
        this.serverProfile = serverProfile;
    }
}