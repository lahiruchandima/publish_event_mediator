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

package org.wso2.carbon.mediator.publishevent.ui;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.mediator.service.util.MediatorProperty;

/**
 * Property of a Stream Definition
 */
public class Property extends MediatorProperty {

	private String defaultValue = "";
	private String type = "";

	private static final Log log = LogFactory.getLog(Property.class);

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Returns the data bridge attribute type of this object
	 *
	 * @return Data bridge attribute type of this object
	 */
	public AttributeType getDatabridgeAttributeType() {
		//TODO:
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
}