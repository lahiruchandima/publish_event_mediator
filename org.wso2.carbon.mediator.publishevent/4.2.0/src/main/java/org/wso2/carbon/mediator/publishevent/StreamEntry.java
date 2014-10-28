package org.wso2.carbon.mediator.publishevent;

public class StreamEntry {

    private String name = "";
    private String value = "";
    private String type = "";

    public void setName(String name){
        this.name = name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setType(String type){
        this.type = type;
    }

    public String getName(){
        return this.name;
    }

    public String getValue() {
        return value;
    }

    public String getType(){
        return this.type;
    }
}