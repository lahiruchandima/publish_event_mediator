package org.wso2.carbon.mediator.publishevent;

import java.util.ArrayList;
import java.util.List;

/**
 * A specific BAM server configuration stored in Registry
 */
public class ThriftEndpointConfig {

    private String username;
    private String password;
    private String urlSet;
    private String ip;
    private String authenticationPort;
    private String receiverPort;
    private boolean security = true;
    private boolean loadbalancer = false;

    public boolean isLoadbalanced() {
        return loadbalancer;
    }

    public void setLoadbalanced(boolean loadbalancer) {
        this.loadbalancer = loadbalancer;
    }

    public String getUsername(){
        return this.username;
    }

    public void setUsername(String username){
        this.username = username;
    }

    public String getPassword(){
        return this.password;
    }

    public void setPassword(String password){
        this.password = password;
    }

    public String getUrlSet() {
        return urlSet;
    }

    public void setUrlSet(String urlSet) {
        this.urlSet = urlSet;
    }

    public String getIp(){
        return this.ip;
    }

    public void setIp(String ip){
        this.ip = ip;
    }

    public String getAuthenticationPort(){
        return this.authenticationPort;
    }

    public void setAuthenticationPort(String authenticationPort){
        this.authenticationPort = authenticationPort;
    }

    public String getReceiverPort() {
        return receiverPort;
    }

    public void setReceiverPort(String receiverPort) {
        this.receiverPort = receiverPort;
    }

    public boolean isSecure() {
        return security;
    }

    public void setSecurity(boolean security) {
        this.security = security;
    }
}
