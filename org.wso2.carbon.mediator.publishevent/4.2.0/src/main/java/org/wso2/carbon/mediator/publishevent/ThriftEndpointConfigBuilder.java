package org.wso2.carbon.mediator.publishevent;

//TODO: add copyright headers

import org.apache.axiom.om.OMAttribute;
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

    static final QName CREDENTIAL_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "credential");
    static final QName USERNAME_Q = new QName("userName");
    static final QName PASSWORD_Q = new QName("password");
    static final QName CONNECTION_Q = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "connection");
    static final QName LOADBALANCER_Q = new QName("loadbalancer");
    static final QName SECURE_Q = new QName("secure");
    static final QName URLSET_Q = new QName("urlSet");
    static final QName IP_Q = new QName("ip");
    static final QName AUTHPORT_Q = new QName("authPort");
    static final QName RECEIVERPORT_Q = new QName("receiverPort");

    public static ThriftEndpointConfig createThriftEndpointConfig(OMElement thriftEndpointConfigElement) {

        ThriftEndpointConfig thriftEndpointConfig = new ThriftEndpointConfig();

        OMElement credentialElement = thriftEndpointConfigElement.getFirstChildWithName(CREDENTIAL_Q);

        if (credentialElement == null) {
            throw new SynapseException(CREDENTIAL_Q.getLocalPart() + " element missing in thrift endpoint config");
        }

        OMAttribute userNameAttr = credentialElement.getAttribute(USERNAME_Q);
        if (!isNotNullOrEmpty(userNameAttr)) {
            throw new SynapseException(USERNAME_Q.getLocalPart() + " attribute missing in thrift endpoint config");
        }

        OMAttribute passwordAttr = credentialElement.getAttribute(PASSWORD_Q);
        if (!isNotNullOrEmpty(passwordAttr)) {
            throw new SynapseException(PASSWORD_Q.getLocalPart() + " attribute missing in thrift endpoint config");
        }

        thriftEndpointConfig.setUsername(userNameAttr.getAttributeValue());
        thriftEndpointConfig.setPassword(base64DecodeAndDecrypt(passwordAttr.getAttributeValue()));

        OMElement connectionElement = thriftEndpointConfigElement.getFirstChildWithName(CONNECTION_Q);

        if (connectionElement == null) {
            throw new SynapseException(CONNECTION_Q.getLocalPart() + " element missing in thrift endpoint config");
        }

        OMAttribute loadbalancerAttr = connectionElement.getAttribute(LOADBALANCER_Q);
        OMAttribute secureAttr = connectionElement.getAttribute(SECURE_Q);
        OMAttribute urlSet = connectionElement.getAttribute(URLSET_Q);
        OMAttribute ipAttr = connectionElement.getAttribute(IP_Q);
        OMAttribute authenticationPortAttr = connectionElement.getAttribute(AUTHPORT_Q);
        OMAttribute receiverPortAttr = connectionElement.getAttribute(RECEIVERPORT_Q);

        if (isNotNullOrEmpty(loadbalancerAttr) && "true".equals(loadbalancerAttr.getAttributeValue())) {
            thriftEndpointConfig.setLoadbalanced(true);
            thriftEndpointConfig.setUrlSet(urlSet.getAttributeValue());
        } else {
            if (!isNotNullOrEmpty(ipAttr)) {
                throw new SynapseException(IP_Q + " attribute missing in thrift endpoint config");
            }
            if (!isNotNullOrEmpty(secureAttr)) {
                throw new SynapseException(SECURE_Q + " attribute missing in thrift endpoint config");
            }
            if (!isNotNullOrEmpty(authenticationPortAttr)) {
                throw new SynapseException(AUTHPORT_Q + " attribute missing in thrift endpoint config");
            }

            thriftEndpointConfig.setIp(ipAttr.getAttributeValue());

            String security = secureAttr.getAttributeValue();
            if ("true".equals(security)) {
                thriftEndpointConfig.setSecurity(true);
            } else if ("false".equals(security)) {
                thriftEndpointConfig.setSecurity(false);
            } else {
                throw new SynapseException("Invalid security value \"" + security + "\" specified in thrift " +
                        " endpoint config. Value should be \"true\" or \"false\"");
            }
            thriftEndpointConfig.setAuthenticationPort(authenticationPortAttr.getAttributeValue());
            if (receiverPortAttr.getAttributeValue() != null && !receiverPortAttr.getAttributeValue().equals("")) {
                thriftEndpointConfig.setReceiverPort(receiverPortAttr.getAttributeValue());
            } else {
                thriftEndpointConfig.setReceiverPort("");
            }
        }

        return thriftEndpointConfig;
    }

    private static boolean isNotNullOrEmpty(OMAttribute omAttribute) {
        return omAttribute != null && !omAttribute.getAttributeValue().equals("");
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
