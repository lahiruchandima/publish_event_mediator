<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1" %>

<%--
  ~  Copyright (c) 2008, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  Licensed under the Apache License, Version 2.0 (the "License");
  ~  you may not use this file except in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~        http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing, software
  ~  distributed under the License is distributed on an "AS IS" BASIS,
  ~  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~  See the License for the specific language governing permissions and
  ~  limitations under the License.
  --%>

<%@ page import="org.wso2.carbon.mediator.publishevent.ui.PublishEventMediator" %>
<%@ page import="org.wso2.carbon.mediator.publishevent.ui.Property" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.XPathFactory" %>
<%@ page import="org.apache.synapse.util.AXIOMUtils" %>
<%@ page import="org.apache.axiom.om.util.AXIOMUtil" %>
<%@ page import="javax.xml.stream.XMLStreamException" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>

<%

    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    String PROPERTY_SEPARATOR = ";";
    String PROPERTY_VALUE_SEPARATOR = "::";
    String PROPERTY_TYPE_VALUE = "value";
    String PROPERTY_TYPE_EXPRESSION = "expression";
    String uri = "", prefix = "";
    if (!(mediator instanceof PublishEventMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    PublishEventMediator publishEventMediator = (PublishEventMediator) mediator;

    publishEventMediator.setStreamName(request.getParameter("mediator.publishEvent.stream.name"));
    publishEventMediator.setStreamVersion(request.getParameter("mediator.publishEvent.stream.version"));
    publishEventMediator.clearList("meta");
    publishEventMediator.clearList("correlation ");
    publishEventMediator.clearList("payload");

    String propertyString =request.getParameter("hfmetaPropertyTableData");

    Property currentProperty;
    List<Property> metaProperties = new ArrayList<Property>();
    int i;
    String[] properties = propertyString.split(PROPERTY_SEPARATOR);
    for (String property : properties) {
    if(property != null && !property.equals("")){
    i = 0;
    currentProperty = new Property();
    currentProperty.setKey(property.split(PROPERTY_VALUE_SEPARATOR)[i++]);
    if(PROPERTY_TYPE_VALUE.equals(property.split(PROPERTY_VALUE_SEPARATOR)[i])){
    currentProperty.setValue(property.split(PROPERTY_VALUE_SEPARATOR)[++i]);
    } else if(PROPERTY_TYPE_EXPRESSION.equals(property.split(PROPERTY_VALUE_SEPARATOR)[i])){
    currentProperty.setExpression(SynapseXPath.parseXPathString(property.split(PROPERTY_VALUE_SEPARATOR)[++i]));
    }
    currentProperty.setType(property.split(PROPERTY_VALUE_SEPARATOR)[++i]);


    metaProperties.add(currentProperty);

    }
    }

    ((PublishEventMediator) mediator).setMetaProperties(metaProperties);


    //publishEventMediator.extractProperties(request.getParameter("hfmetaPropertyTableData"),"meta");
    //publishEventMediator.extractProperties(request.getParameter("hfcorrelationPropertyTableData"),"correlation");
    //publishEventMediator.extractProperties(request.getParameter("hfpayloadPropertyTableData"),"payload");





%>

