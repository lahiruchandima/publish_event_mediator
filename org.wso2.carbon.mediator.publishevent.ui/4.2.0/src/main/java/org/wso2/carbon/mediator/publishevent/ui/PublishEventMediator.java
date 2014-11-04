/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.mediator.publishevent.ui;

import org.apache.axiom.om.OMElement;
import org.wso2.carbon.databridge.agent.thrift.lb.LoadBalancingDataPublisher;
import org.wso2.carbon.mediator.service.ui.AbstractMediator;

import java.util.ArrayList;
import java.util.List;


public class PublishEventMediator extends AbstractMediator {

    private String streamVersion = "";
    private List<Property> metaProperties = new ArrayList<Property>();
    private List<Property> correlationProperties = new ArrayList<Property>();
    private List<Property> payloadProperties = new ArrayList<Property>();
    private String serverProfile = "";
    private LoadBalancingDataPublisher loadBalancingDataPublisher;
    private ThriftEndpointConfig thriftEndpointConfig;



    public String getTagLocalName() {
        return "publishEvent";
    }

    public OMElement serialize(OMElement parent) {
        OMElement publishEvent = fac.createOMElement("publishEvent", synNS);
        saveTracingState(publishEvent, this);

        return publishEvent;
    }

    public void build(OMElement elem) {

    }

}
