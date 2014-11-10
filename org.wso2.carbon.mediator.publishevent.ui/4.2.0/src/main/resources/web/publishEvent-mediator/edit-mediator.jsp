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
<%@ page import="org.apache.synapse.util.xpath.SynapseXPath" %>
<%@ page import="org.wso2.carbon.mediator.publishevent.ui.PublishEventMediator" %>
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.mediator.service.util.MediatorProperty" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ page import="org.wso2.carbon.mediator.publishevent.ui.Property" %>
<%@ page import="java.util.List" %>
<%@ page import="org.apache.synapse.config.xml.SynapsePath" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<%! public static final String META_PROPERTY_VALUES = "metaPropertyValue";
    public static final String META_PROPERTY_KEYS = "metaPropertyKey";
    public static final String CORRELATION_PROPERTY_VALUES = "correlationPropertyValues";
    public static final String CORRELATION_PROPERTY_KEYS = "correlationPropertyKeys";
    public static final String PAYLOAD_PROPERTY_VALUES = "payloadPropertyValues";
    public static final String PAYLOAD_PROPERTY_KEYS = "payloadPropertyKeys";
%>


<style type="text/css">
    .no-border-all{
        border: none!important;
    }
    .no-border-all td{
        border: none!important;
    }
</style>



<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    if (!(mediator instanceof org.wso2.carbon.mediator.publishevent.ui.PublishEventMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    PublishEventMediator publishEventMediator = (PublishEventMediator) mediator;


    List<Property> mediatorPropertyList = publishEventMediator.getMetaProperties();
    NameSpacesRegistrar nameSpacesRegistrar = NameSpacesRegistrar.getInstance();
    //nameSpacesRegistrar.registerNameSpaces(mediatorPropertyList, "propertyValue", session);
    String propertyTableStyle = mediatorPropertyList.isEmpty() ? "display:none;" : "";


    String streamName = "";
    String streamVersion = "";

    if(publishEventMediator.getStreamName() != null){
        streamName = publishEventMediator.getStreamName();
    }

    if(publishEventMediator.getStreamVersion() != null){
        streamVersion = publishEventMediator.getStreamVersion();
    }


%>

<fmt:bundle basename="org.wso2.carbon.mediator.publishevent.ui.i18n.Resources">
    <carbon:jsi18n
		resourceBundle="org.wso2.carbon.mediator.publishevent.ui.i18n.JSResources"
		request="<%=request%>" i18nObjectName="publishEventMediatorJsi18n"/>
    <div>
        <script type="text/javascript" src="../publishEvent-mediator/js/mediator-util.js"></script>

        <table class="normal" width="100%">
            <tbody>
            <tr><td colspan="3"><h2><fmt:message key="mediator.publishEvent.header"/></h2></td></tr>


            <tr>
                <td style="width:130px;"><fmt:message key="mediator.publishEvent.stream.name"/><font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td>
                    <input type="text" id="mediator.publishEvent.stream.name" name="mediator.publishEvent.stream.name"
                           style="width:300px;"
                           value='<%=publishEventMediator.getStreamName() != null ? publishEventMediator.getStreamName() : ""%>'/>
                </td>
                <td></td>
            </tr>
            <tr>
                <td style="width:130px;"><fmt:message key="mediator.publishEvent.stream.version"/><font style="color: red; font-size: 8pt;"> *</font>
                </td>
                <td>
                    <input type="text" id="mediator.publishEvent.stream.version" name="mediator.publishEvent.stream.version"
                           style="width:300px;"
                           value='<%=publishEventMediator.getStreamVersion() != null ? publishEventMediator.getStreamVersion() : ""%>'/>
                </td>
                <td></td>
            </tr>


            <tr><td colspan="3"><h4><fmt:message key="mediator.publishEvent.meta.header"/></h4></td></tr>

            <tr>
                <td>


                    <div style="margin-top:0px;">

                        <table id="propertytable" style="<%=propertyTableStyle%>;" class="styledInner">
                            <thead>
                            <tr>
                                <th width="15%"><fmt:message key="mediator.publishEvent.propertyName"/></th>
                                <th width="10%"><fmt:message key="mediator.publishEvent.propertyValue"/></th>
                                <th width="15%"><fmt:message key="mediator.publishEvent.propertyExp"/></th>
                                <th id="ns-edior-th" style="display:none;" width="15%"><fmt:message
                                        key="mediator.publishEvent.nsEditor"/></th>
                                <th width="10%"><fmt:message key="mediator.publishEvent.propertyValueType"/></th>
                                <th><fmt:message key="mediator.publishEvent.action"/></th>
                            </tr>
                            <tbody id="propertytbody">
                            <%
                                int i = 0;
                                for (Property mp : mediatorPropertyList) {
                                    if (mp != null) {
                                        String value = mp.getValue();
                                        String type = mp.getType();
                                        String pathValue;
                                        SynapseXPath path = mp.getExpression();
                                        if(path == null) {
                                            pathValue="";
                                        } else {

                                            pathValue = path.toString();
                                        }
                                        boolean isLiteral = value != null && !"".equals(value);
                            %>
                            <tr id="propertyRaw<%=i%>">
                                <td><input type="text" name="propertyName<%=i%>" id="propertyName<%=i%>"
                                           class="esb-edit small_textbox"
                                           value="<%=mp.getKey()%>"/>
                                </td>
                                <td>
                                    <select class="esb-edit small_textbox" name="propertyTypeSelection<%=i%>"
                                            id="propertyTypeSelection<%=i%>"
                                            onchange="onPropertyTypeSelectionChange('<%=i%>','<fmt:message key="mediator.publishEvent.namespace"/>')">
                                        <% if (isLiteral) {%>
                                        <option value="literal">
                                            <fmt:message key="mediator.publishEvent.value"/>
                                        </option>
                                        <option value="expression">
                                            <fmt:message key="mediator.publishEvent.expression"/>
                                        </option>
                                        <%} else if (path != null) {%>
                                        <option value="expression">
                                            <fmt:message key="mediator.publishEvent.expression"/>
                                        </option>
                                        <option value="literal">
                                            <fmt:message key="mediator.publishEvent.value"/>
                                        </option>
                                        <%} else { %>
                                        <option value="literal">
                                            <fmt:message key="mediator.publishEvent.value"/>
                                        </option>
                                        <option value="expression">
                                            <fmt:message key="mediator.publishEvent.expression"/>
                                        </option>
                                        <% }%>
                                    </select>
                                </td>
                                <td>
                                    <% if (value != null && !"".equals(value)) {%>
                                    <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                                           value="<%=value%>"
                                           class="esb-edit"/>
                                    <%} else if (path != null) {%>
                                    <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                                           value="<%=pathValue%>" class="esb-edit"/>
                                    <%} else { %>
                                    <input id="propertyValue<%=i%>" name="propertyValue<%=i%>" type="text"
                                           class="esb-edit"/>
                                    <% }%>
                                </td>
                                <td id="nsEditorButtonTD<%=i%>" style="<%=isLiteral?"display:none;":""%>">
                                    <% if (!isLiteral && path != null) {%>
                                    <script type="text/javascript">
                                        document.getElementById("ns-edior-th").style.display = "";
                                    </script>
                                    <a href="#nsEditorLink" class="nseditor-icon-link"
                                       style="padding-left:40px"
                                       onclick="showNameSpaceEditor('propertyValue<%=i%>')">
                                        <fmt:message key="mediator.publishEvent.namespace"/></a>
                                </td>
                                <%}%>
                                <td>
                                    <select class="esb-edit small_textbox" name="propertyValueTypeSelection<%=i%>"
                                            id="propertyValueTypeSelection<%=i%>"
                                            onchange="onPropertyValueTypeSelectionChange('<%=i%>','<fmt:message key="mediator.publishEvent.namespace"/>')">

                                        <option <% if (type.equals("string")) out.print("selected"); %> value="string">
                                            <fmt:message key="mediator.publishEvent.type.string"/>
                                        </option>
                                        <option <% if (type.equals("integer")) out.print("selected"); %> value="integer">
                                            <fmt:message key="mediator.publishEvent.type.integer"/>
                                        </option>

                                        <option <% if (type.equals("boolean")) out.print("selected"); %> value="boolean">
                                            <fmt:message key="mediator.publishEvent.type.boolean"/>
                                        </option>

                                        <option <% if (type.equals("double")) out.print("selected"); %> value="double">
                                            <fmt:message key="mediator.publishEvent.type.double"/>
                                        </option>
                                        <option <% if (type.equals("float")) out.print("selected"); %> value="float">
                                            <fmt:message key="mediator.publishEvent.type.float"/>
                                        </option>
                                        <option <% if (type.equals("long")) out.print("selected"); %> value="long">
                                            <fmt:message key="mediator.publishEvent.type.long"/>
                                        </option>

                                    </select>
                                </td>
                                <td><a href="#" class="delete-icon-link"
                                       onclick="deleteproperty(<%=i%>);return false;"><fmt:message
                                        key="mediator.publishEvent.delete"/></a></td>
                            </tr>
                            <% }
                                i++;
                            } %>
                            <input type="hidden" name="propertyCount" id="propertyCount" value="<%=i%>"/>
                            <script type="text/javascript">
                                if (isRemainPropertyExpressions()) {
                                    resetDisplayStyle("");
                                }
                            </script>
                            </tbody>
        </table>
    </div>
    </td>
    </tr>
    <tr>
        <td>
            <div style="margin-top:10px;">
                <a name="addNameLink"></a>
                <a class="add-icon-link"
                   href="#addNameLink"
                   onclick="addproperty('<fmt:message key="mediator.publishEvent.namespace"/>','<fmt:message key="mediator.publishEvent.propemptyerror"/>','<fmt:message key="mediator.publishEvent.valueemptyerror"/>')"><fmt:message
                        key="mediator.publishEvent.addProperty"/></a>
            </div>
        </td>
    </tr>




            </tbody>
        </table>
    </div>
</fmt:bundle>