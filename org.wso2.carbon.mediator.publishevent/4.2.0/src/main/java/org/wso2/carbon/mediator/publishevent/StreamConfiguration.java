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
    private List<StreamEntry> entries = new ArrayList<StreamEntry>();
    private List<Property> properties = new ArrayList<Property>();

    public void setName(String name){
        this.name = name;
    }

    public void setNickname(String nickname){
        this.nickname = nickname;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setVersion(String version){
        this.version = version;
    }

    public String getName(){
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

    public List<StreamEntry> getEntries(){
        return this.entries;
    }

    public List<Property> getProperties(){
        return this.properties;
    }

}
