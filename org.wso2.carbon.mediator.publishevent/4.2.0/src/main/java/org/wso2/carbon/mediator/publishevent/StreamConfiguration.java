package org.wso2.carbon.mediator.publishevent;

import java.util.ArrayList;
import java.util.List;

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
        return this.name;
    }

    public String getNickname(){
        return this.nickname;
    }

    public String getDescription(){
        return this.description;
    }

    public String getVersion(){
        return this.version;
    }

    public List<Property> getMetaProperties() {
        return this.metaProperties;
    }

    public List<Property> getCorrelationProperties() {
        return this.correlationProperties;
    }

    public List<Property> getPayloadProperties() {
        return this.payloadProperties;
    }
}
