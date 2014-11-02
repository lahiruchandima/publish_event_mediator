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
import org.apache.synapse.util.xpath.SynapseXPath;
import org.jaxen.JaxenException;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.mediator.publishevent.util.PropertyTypeConverter;

import java.util.Map;

/**
 * Property of a Stream Definition
 */
public class Property {

    private String key = "";
    private String value = "";
    private String defaultValue = "";
    private String type = "";
    private boolean isExpression = false;
    SynapseXPath synapseXPath = null;

    private PropertyTypeConverter propertyTypeConverter = new PropertyTypeConverter();
    private static final Log log = LogFactory.getLog(Property.class);

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void generateXPath(Map<String, String> namespaceMap) {
        try {
            synapseXPath = new SynapseXPath(value);
            for (Map.Entry<String, String> entry : namespaceMap.entrySet()) {
                synapseXPath.addNamespace(entry.getKey(), entry.getValue());
            }
        } catch (JaxenException e) {
            log.error("Invalid XPath specified for property \"" + key + "\". Error: " + e.getLocalizedMessage());
        }
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isExpression() {
        return isExpression;
    }

    public void setExpression(boolean expression) {
        isExpression = expression;
    }

    public String getType() {
        return type;
    }
    public AttributeType getDatabridgeAttributeType() {
        if ("STRING".equals(type)) {
            return AttributeType.STRING;
        }
        if ("INTEGER".equals(type)) {
            return AttributeType.INT;
        }
        if ("FLOAT".equals(type)) {
            return AttributeType.FLOAT;
        }
        if ("DOUBLE".equals(type)) {
            return AttributeType.DOUBLE;
        }
        if ("BOOLEAN".equals(type)) {
            return AttributeType.BOOL;
        }
        if ("LONG".equals(type)) {
            return AttributeType.LONG;
        }
        return AttributeType.STRING;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object extractPropertyValue(MessageContext messageContext) {
        try {
            String stringProperty;
            String propertyType;
            if (isExpression()) {
                stringProperty = synapseXPath.stringValueOf(messageContext);
            } else {
                stringProperty =  getValue();
            }
            propertyType = getType();
            if ("STRING".equals(propertyType)) {
                return this.propertyTypeConverter.convertToString(stringProperty);
            } else if ("INTEGER".equals(propertyType)) {
                return this.propertyTypeConverter.convertToInt(stringProperty);
            } else if ("FLOAT".equals(propertyType)) {
                return this.propertyTypeConverter.convertToFloat(stringProperty);
            } else if ("DOUBLE".equals(propertyType)) {
                return this.propertyTypeConverter.convertToDouble(stringProperty);
            } else if ("BOOLEAN".equals(propertyType)) {
                return this.propertyTypeConverter.convertToBoolean(stringProperty);
            } else if ("LONG".equals(propertyType)) {
                return this.propertyTypeConverter.convertToLong(stringProperty);
            } else {
                return stringProperty;
            }
        } catch (Exception e) {
            String errorMsg = "Error occurred while extracting property value. " + e.getMessage();
            log.error(errorMsg, e);
            return null;
        }
    }
}
