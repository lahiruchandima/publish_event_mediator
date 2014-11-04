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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Stream Configuration Definition of an Event
 */
public class StreamConfiguration {

    private String name = "";
    private String nickname = "";
    private String description = "";
    private String version = "";
    private List<Property> metaProperties = new ArrayList<Property>();
    private List<Property> correlationProperties = new ArrayList<Property>();
    private List<Property> payloadProperties = new ArrayList<Property>();

    public void setName(String name){
        this.name = name;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVersion(String version){
        this.version = version;
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

    public String getName() {
        return name;
    }

    public String getNickname(){
        return nickname;
    }

    public String getDescription(){
        return description;
    }

    public String getVersion(){
        return version;
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
}
