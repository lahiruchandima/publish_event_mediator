package org.wso2.carbon.mediator.publishevent;

/**
 * Property of a Stream Definition
 */
public class Property {

    private String key = "";
    private String value = "";
    private String type = "";

    private boolean isExpression = false;

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

    public boolean isExpression() {
        return isExpression;
    }

    public void setExpression(boolean expression) {
        isExpression = expression;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
