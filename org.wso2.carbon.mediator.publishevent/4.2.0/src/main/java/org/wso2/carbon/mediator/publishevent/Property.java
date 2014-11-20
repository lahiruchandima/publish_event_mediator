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

import org.apache.synapse.MessageContext;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.wso2.carbon.databridge.commons.AttributeType;

/**
 * Property of a Stream Definition
 */
public class Property {
	private String key = "";
	private String value = null;
	private SynapseXPath expression = null;
	private String defaultValue = "";
	private String type = "";
	private PropertyTypeConverter propertyTypeConverter = new PropertyTypeConverter();

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

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public SynapseXPath getExpression() {
		return expression;
	}

	public void setExpression(SynapseXPath expression) {
		this.expression = expression;
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
		String stringProperty;
		if (expression != null) {
			stringProperty = expression.stringValueOf(messageContext);
		} else {
			stringProperty = getValue();
		}
		if (stringProperty == null || "".equals(stringProperty)) {
			stringProperty = defaultValue;
		}
		if ("STRING".equals(getType())) {
			return propertyTypeConverter.convertToString(stringProperty);
		}
		if ("INTEGER".equals(getType())) {
			return propertyTypeConverter.convertToInt(stringProperty);
		}
		if ("FLOAT".equals(getType())) {
			return propertyTypeConverter.convertToFloat(stringProperty);
		}
		if ("DOUBLE".equals(getType())) {
			return propertyTypeConverter.convertToDouble(stringProperty);
		}
		if ("BOOLEAN".equals(getType())) {
			return propertyTypeConverter.convertToBoolean(stringProperty);
		}
		if ("LONG".equals(getType())) {
			return propertyTypeConverter.convertToLong(stringProperty);
		}
		return stringProperty;
	}
}