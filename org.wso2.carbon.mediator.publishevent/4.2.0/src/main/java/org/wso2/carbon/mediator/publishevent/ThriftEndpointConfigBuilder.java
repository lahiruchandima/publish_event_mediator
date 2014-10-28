package org.wso2.carbon.mediator.publishevent;


import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.synapse.SynapseConstants;

import javax.xml.namespace.QName;
import java.util.Iterator;

/**
 * Builder of ThriftEndpointConfig from OMElements from the string fetched from Registry
 */
public class ThriftEndpointConfigBuilder {

    private ThriftEndpointConfig ThriftEndpointConfig = new ThriftEndpointConfig();

    public boolean createThriftEndpointConfig(OMElement ThriftEndpointConfigElement){
        boolean credentialsOk = this.processCredentialElement(ThriftEndpointConfigElement);
        boolean connectionOk = this.processConnectionElement(ThriftEndpointConfigElement);
        boolean streamsOk = this.processStreamsElement(ThriftEndpointConfigElement);
        return credentialsOk && connectionOk && streamsOk;
    }

    private boolean processCredentialElement(OMElement ThriftEndpointConfig){
        OMElement credentialElement = ThriftEndpointConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "credential"));
        if(credentialElement != null){
            OMAttribute userNameAttr = credentialElement.getAttribute(new QName("userName"));
            OMAttribute passwordAttr = credentialElement.getAttribute(new QName("password"));
            if(this.isNotNullOrEmpty(userNameAttr) && this.isNotNullOrEmpty(passwordAttr)){
                this.ThriftEndpointConfig.setUsername(userNameAttr.getAttributeValue());
                this.ThriftEndpointConfig.setPassword(passwordAttr.getAttributeValue());
            }
            else {
                return false;
            }
        }
        return true;
    }

    private boolean processConnectionElement(OMElement ThriftEndpointConfig){
        OMElement connectionElement = ThriftEndpointConfig.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "connection"));
        if(connectionElement != null){
            OMAttribute loadbalancerAttr = connectionElement.getAttribute(new QName("loadbalancer"));
            OMAttribute secureAttr = connectionElement.getAttribute(new QName("secure"));
            OMAttribute urlSet = connectionElement.getAttribute(new QName("urlSet"));
            OMAttribute ipAttr = connectionElement.getAttribute(new QName("ip"));
            OMAttribute authenticationPortAttr = connectionElement.getAttribute(new QName("authPort"));
            OMAttribute receiverPortAttr = connectionElement.getAttribute(new QName("receiverPort"));
            if(this.isNotNullOrEmpty(loadbalancerAttr) && "true".equals(loadbalancerAttr.getAttributeValue())){
                this.ThriftEndpointConfig.setLoadbalanced(true);
                this.ThriftEndpointConfig.setUrlSet(urlSet.getAttributeValue());
            }
            else {
                if(this.isNotNullOrEmpty(ipAttr) && this.isNotNullOrEmpty(secureAttr) && this.isNotNullOrEmpty(authenticationPortAttr)){
                    this.ThriftEndpointConfig.setIp(ipAttr.getAttributeValue());
                    if("true".equals(secureAttr.getAttributeValue())){
                        this.ThriftEndpointConfig.setSecurity(true);
                    } else if ("false".equals(secureAttr.getAttributeValue())) {
                        this.ThriftEndpointConfig.setSecurity(false);
                    } else {
                        return false; // Secure attribute should have a value
                    }
                    this.ThriftEndpointConfig.setAuthenticationPort(authenticationPortAttr.getAttributeValue());
                    if(receiverPortAttr.getAttributeValue() != null && !receiverPortAttr.getAttributeValue().equals("")){
                        this.ThriftEndpointConfig.setReceiverPort(receiverPortAttr.getAttributeValue());
                    } else {
                        this.ThriftEndpointConfig.setReceiverPort("");
                    }

                }
                else {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean processStreamsElement(OMElement ThriftEndpointConfigElement){
        OMElement streamsElement = ThriftEndpointConfigElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "streams"));
        return streamsElement != null && this.processStreamElements(streamsElement);
    }

    private boolean processStreamElements(OMElement streamsElement){
        OMElement streamElement;
        StreamConfiguration streamConfiguration;
        Iterator itr = streamsElement.getChildrenWithName(new QName("stream"));
        while (itr.hasNext()){
            streamElement = (OMElement)itr.next();
            streamConfiguration = new StreamConfiguration();
            if (streamElement != null && this.processStreamElement(streamElement, streamConfiguration)){
                this.ThriftEndpointConfig.getStreamConfigurations().add(streamConfiguration);
            }
            else {
                return false;
            }
        }
        return true;
    }

    private boolean processStreamElement(OMElement streamElement, StreamConfiguration streamConfiguration){
        OMAttribute nameAttr = streamElement.getAttribute(new QName("name"));
        OMAttribute versionAttr = streamElement.getAttribute(new QName("version"));
        OMAttribute nickNameAttr = streamElement.getAttribute(new QName("nickName"));
        OMAttribute descriptionAttr = streamElement.getAttribute(new QName("description"));
        if(this.isNotNullOrEmpty(nameAttr) && this.isNotNullOrEmpty(nickNameAttr) && this.isNotNullOrEmpty(descriptionAttr)){
            streamConfiguration.setName(nameAttr.getAttributeValue());
            streamConfiguration.setVersion(versionAttr.getAttributeValue());
            streamConfiguration.setNickname(nickNameAttr.getAttributeValue());
            streamConfiguration.setDescription(descriptionAttr.getAttributeValue());

            boolean payloadElementOk = this.processPayloadElement(streamElement, streamConfiguration);

            boolean propertiesElementOk = this.processPropertiesElement(streamElement, streamConfiguration);

            return (payloadElementOk & propertiesElementOk);
        }
        return false; // Incomplete attributes are not accepted
    }

    private boolean processPayloadElement(OMElement streamElement, StreamConfiguration streamConfiguration){
        OMElement payloadElement = streamElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "payload"));
        return payloadElement != null && this.processEntryElements(payloadElement, streamConfiguration);
    }

    private boolean processEntryElements(OMElement payloadElement, StreamConfiguration streamConfiguration){
        OMElement entryElement;
        Iterator itr = payloadElement.getChildrenWithName(new QName("entry"));
        while (itr.hasNext()){
            entryElement = (OMElement)itr.next();
            if (!(entryElement != null && this.processEntryElement(entryElement, streamConfiguration))){
                return false;
            }
        }
        return true; // Empty Entry elements are accepted
    }

    private boolean processEntryElement(OMElement entryElement, StreamConfiguration streamConfiguration){
        OMAttribute nameAttr = entryElement.getAttribute(new QName("name"));
        OMAttribute valueAttr = entryElement.getAttribute(new QName("value"));
        OMAttribute typeAttr = entryElement.getAttribute(new QName("type"));
        if(this.isNotNullOrEmpty(nameAttr) && this.isNotNullOrEmpty(valueAttr) && this.isNotNullOrEmpty(typeAttr)){
            StreamEntry streamEntry = new StreamEntry();
            streamEntry.setName(nameAttr.getAttributeValue());
            streamEntry.setValue(valueAttr.getAttributeValue());
            streamEntry.setType(typeAttr.getAttributeValue());
            streamConfiguration.getEntries().add(streamEntry);
            return true;
        }
        return false; // Empty Entry elements and incomplete Entry parameters are not accepted
    }

    private boolean processPropertiesElement(OMElement streamElement, StreamConfiguration streamConfiguration){
        OMElement propertiesElement = streamElement.getFirstChildWithName(
                new QName(SynapseConstants.SYNAPSE_NAMESPACE, "properties"));
        return propertiesElement == null || this.processPropertyElements(propertiesElement, streamConfiguration);
    }

    private boolean processPropertyElements(OMElement propertiesElement, StreamConfiguration streamConfiguration){
        OMElement propertyElement;
        Iterator itr = propertiesElement.getChildrenWithName(new QName("property"));
        while (itr.hasNext()){
            propertyElement = (OMElement)itr.next();
            if (!(propertyElement != null && this.processPropertyElement(propertyElement, streamConfiguration))){
                return false;
            }
        }
        return true; // Empty Property elements are accepted
    }

    private boolean processPropertyElement(OMElement propertyElement, StreamConfiguration streamConfiguration){
        OMAttribute nameAttr = propertyElement.getAttribute(new QName("name"));
        OMAttribute valueAttr = propertyElement.getAttribute(new QName("value"));
        OMAttribute typeAttr = propertyElement.getAttribute(new QName("type"));
        OMAttribute isExpressionAttr = propertyElement.getAttribute(new QName("isExpression"));
        if(this.isNotNullOrEmpty(nameAttr) && this.isNotNullOrEmpty(valueAttr) && this.isNotNullOrEmpty(typeAttr) && this.isNotNullOrEmpty(isExpressionAttr)){
            Property property = new Property();
            property.setKey(nameAttr.getAttributeValue());
            property.setValue(valueAttr.getAttributeValue());
            property.setType(typeAttr.getAttributeValue());
            property.setExpression("true".equals(isExpressionAttr.getAttributeValue()));
            streamConfiguration.getProperties().add(property);
            return true;
        }
        return false; // Empty Property elements and incomplete Property parameters are not accepted
    }

    private boolean isNotNullOrEmpty(OMAttribute omAttribute){
        return omAttribute != null && !omAttribute.getAttributeValue().equals("");
    }

    public ThriftEndpointConfig getThriftEndpointConfig(){
        return this.ThriftEndpointConfig;
    }

}
