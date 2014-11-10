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
    publishEventMediator.setEventSink(request.getParameter("mediator.publishEvent.eventSink.select"));
    publishEventMediator.clearList("meta");
    publishEventMediator.clearList("correlation ");
    publishEventMediator.clearList("payload");

    String propertyString =request.getParameter("hfmetaPropertyTableData");
    XPathFactory xPathFactory = XPathFactory.getInstance();
    String propertyCountParameter = request.getParameter("propertyCount");
    if (propertyCountParameter != null && !"".equals(propertyCountParameter)) {
        Property currentProperty;
        List<Property> metaProperties = new ArrayList<Property>();
        int propertyCount = 0;

        try {


            propertyCount = Integer.parseInt(propertyCountParameter.trim());
            for (int i = 0; i <= propertyCount; i++) {
                String name = request.getParameter("propertyName" + i);
                if (name != null && !"".equals(name)) {
                    String valueId = "propertyValue" + i;
                    String value = request.getParameter(valueId);
                    currentProperty = new Property();
                    currentProperty.setKey(name);

                    String expression = request.getParameter("propertyTypeSelection" + i);
                    boolean isExpression = expression != null && "expression".equals(expression.trim());

                    if (value != null) {
                        if (isExpression) {
                            if(value.trim().startsWith("json-eval(")) {
                                SynapseXPath jsonPath = new SynapseXPath(value.trim().substring(10, value.length() - 1));
                                currentProperty.setExpression(jsonPath);
                            } else {
                                currentProperty.setExpression(xPathFactory.createSynapseXPath(valueId, value.trim(), session));
                            }
                        } else {
                            currentProperty.setValue(value);
                        }
                    }

                    String type=request.getParameter("propertyValueTypeSelection" + i);
                    currentProperty.setType(type);


                    metaProperties.add(currentProperty);
                }
            }

            ((PublishEventMediator) mediator).setMetaProperties(metaProperties);
        }catch (NumberFormatException ignored) {
            throw new RuntimeException("Invalid number format");
        } catch (Exception exception) {
            throw new RuntimeException("Invalid Path Expression");
        }
    }
    //publishEventMediator.extractProperties(request.getParameter("hfmetaPropertyTableData"),"meta");
    //publishEventMediator.extractProperties(request.getParameter("hfcorrelationPropertyTableData"),"correlation");
    //publishEventMediator.extractProperties(request.getParameter("hfpayloadPropertyTableData"),"payload");





%>

