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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.Map;
import java.util.TreeMap;

/**
 * Set the Activity ID to the Transport Header and the Synapse Context.
 */
public class ActivityIDSetter {

    private static final String MSG_BAM_ACTIVITY_ID = "activity_id";
    private static final String ACTIVITY_ID = "activityID";
    private static final Log log = LogFactory.getLog(ActivityIDSetter.class);

    public static void setActivityIdInTransportHeader(MessageContext synapseContext) throws SynapseException {
        try {
            //get the unique ID used for correlating messages for BAM activity monitoring
            String idString = getUniqueId();

            //Get activity ID form synapse context, if available.
            Object idFromSynCtx = synapseContext.getProperty(MSG_BAM_ACTIVITY_ID);

            Axis2MessageContext axis2smc = (Axis2MessageContext) synapseContext;
            org.apache.axis2.context.MessageContext axis2MessageContext = axis2smc.getAxis2MessageContext();

            Map headers = (Map) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

            if (headers != null) {
                String idFromHeader = (String) (headers).get(ACTIVITY_ID);
                if (idFromHeader == null || idFromHeader.equals("")) {
                    if (idFromSynCtx != null) {
                        //case 1 - activity ID present in synapse context but absent elsewhere (transport headers exist)
                        //Use the ID present
                        String inID = String.valueOf(idFromSynCtx);
                        if (!(inID.equals(""))) {
                            idString = inID;
                            if (log.isDebugEnabled()) {
                                log.debug("Incoming message had no activity ID, using the ID '" + inID + "' from the Synapse context instead.");
                            }
                        }
                    } else {
                        //case 2 - no activity ID present anywhere, but transport headers exist
                        //Add generated activity ID to Synapse context for later use if needed
                        synapseContext.setProperty(MSG_BAM_ACTIVITY_ID, idString);
                        if (log.isDebugEnabled()) {
                            log.debug("no activity ID present anywhere, but transport headers exist.");
                        }
                    }
                    //Add the recovered (case1) or generated (case2) activity ID to the transport header
                    headers.put(ACTIVITY_ID, idString);
                } else {
                    //case 3 - activity ID is present in the transport header
                    //Just propagate this ID rather than use the generated ID, and expose it to the synapse context
                    idString = idFromHeader;
                    synapseContext.setProperty(MSG_BAM_ACTIVITY_ID, idString);
                    if (log.isDebugEnabled()) {
                        log.debug("Propagating activity ID found in transport header: " + idFromHeader);
                    }
                }
            } else {
                if (idFromSynCtx != null) {
                    String inID = String.valueOf(idFromSynCtx);
                    if (!(inID.equals(""))) {
                        //case 4 - transport headers do not exist but activity ID present in synapse context
                        //Use the ID from the context to replace the generated activity ID
                        idString = inID;
                        if (log.isDebugEnabled()) {
                            log.debug("Using activity ID '" + inID + "' from synapse context, transport headers do not exist");
                        }
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.info("Activity ID not found anywhere, creating new.");
                    }
                }
                //case 5 - no activity ID found anywhere and transport headers do not exist
                //Propagate the generated ID and add it to the synapse context
                headers = new TreeMap<String, String>();
                headers.put(ACTIVITY_ID, idString);
                axis2MessageContext.setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
                synapseContext.setProperty(MSG_BAM_ACTIVITY_ID, idString);
            }
        } catch (Exception e) {
            String errorMsg = "Error while setting Activity ID in Header ";
            log.error(errorMsg, e);
            throw new SynapseException(errorMsg, e);
        }
    }

    //Generate unique ID (cheaper than generating a UUID)
    private static String getUniqueId() {
        return (String.valueOf(System.nanoTime()) + Math.round(Math.random() * 123456789));
    }
}