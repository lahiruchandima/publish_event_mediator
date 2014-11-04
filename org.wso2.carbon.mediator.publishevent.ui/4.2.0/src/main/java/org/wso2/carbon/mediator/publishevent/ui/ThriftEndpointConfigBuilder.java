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

package org.wso2.carbon.mediator.publishevent.ui;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import javax.xml.namespace.QName;
import java.nio.charset.Charset;

/**
 * Builder of ThriftEndpointConfig from OMElements from the string fetched from Registry
 */
public class ThriftEndpointConfigBuilder {

    private static final Log log = LogFactory.getLog(ThriftEndpointConfigBuilder.class);

    static final QName RECEIVER_URL_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "receiverUrl");
    static final QName AUTHENTICATOR_URL_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "authenticatorUrl");
    static final QName USERNAME_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "userName");
    static final QName PASSWORD_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "password");

    public static ThriftEndpointConfig createThriftEndpointConfig(OMElement thriftEndpointConfigElement) {

        ThriftEndpointConfig thriftEndpointConfig = new ThriftEndpointConfig();

        OMElement receiverUrl = thriftEndpointConfigElement.getFirstChildWithName(RECEIVER_URL_Q);
        if (receiverUrl == null || "".equals(receiverUrl.getText())) {
            throw new SynapseException(RECEIVER_URL_Q.getLocalPart() + " is missing in thrift endpoint config");
        }
        thriftEndpointConfig.setReceiverUrlSet(receiverUrl.getText());

        OMElement authenticatorUrl = thriftEndpointConfigElement.getFirstChildWithName(AUTHENTICATOR_URL_Q);
        if (authenticatorUrl != null) {
            thriftEndpointConfig.setAuthenticationUrlSet(authenticatorUrl.getText());
        }

        OMElement userName = thriftEndpointConfigElement.getFirstChildWithName(USERNAME_Q);
        if (userName == null || "".equals(userName.getText())) {
            throw new SynapseException(USERNAME_Q.getLocalPart() + " is missing in thrift endpoint config");
        }
        thriftEndpointConfig.setUsername(userName.getText());

        OMElement password = thriftEndpointConfigElement.getFirstChildWithName(PASSWORD_Q);
        if (password == null || "".equals(password.getText())) {
            throw new SynapseException(PASSWORD_Q.getLocalPart() + " attribute missing in thrift endpoint config");
        }
        thriftEndpointConfig.setPassword(base64DecodeAndDecrypt(password.getText()));

        return thriftEndpointConfig;
    }

    private static String encryptAndBase64Encode(String plainText) {
        try {
            return CryptoUtil.getDefaultCryptoUtil().encryptAndBase64Encode(plainText.getBytes(Charset.forName("UTF-8")));
        } catch (CryptoException e) {
            String errorMsg = "Encryption and Base64 encoding error. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return null;
    }

    private static String base64DecodeAndDecrypt(String cipherText) {
        try {
            return new String(CryptoUtil.getDefaultCryptoUtil().base64DecodeAndDecrypt(cipherText), Charset.forName("UTF-8"));
        } catch (CryptoException e) {
            String errorMsg = "Base64 decoding and decryption error. " + e.getMessage();
            log.error(errorMsg, e);
        }
        return null;
    }
}