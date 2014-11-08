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
<%@ page import="org.wso2.carbon.mediator.service.ui.Mediator" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.SequenceEditorHelper" %>
<%@ page import="org.wso2.carbon.sequences.ui.util.ns.NameSpacesRegistrar" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>


<%! public static final String META_PROPERTY_VALUES = "metaPropertyValues";
    public static final String META_PROPERTY_KEYS = "metaPropertyKeys";
    public static final String CORRELATION_PROPERTY_VALUES = "correlationPropertyValues";
    public static final String CORRELATION_PROPERTY_KEYS = "correlationPropertyKeys";
    public static final String PAYLOAD_PROPERTY_VALUES = "payloadPropertyValues";
    public static final String PAYLOAD_PROPERTY_KEYS = "payloadPropertyKeys";
    //public static final String STREAM_NAMES = "streamNames";
    //public static final String STREAM_VERSIONS = "streamVersions";
    //public static final String STREAM_NICKNAME = "streamNickname";
    //public static final String STREAM_DESCRIPTION = "streamDescription";
    //public static final String SERVER_PROFILE_LOCATION = "bamServerProfiles";
%>


<style type="text/css">
    .no-border-all{
        border: none!important;
    }
    .no-border-all td{
        border: none!important;
    }
</style>
<script id="source" type="text/javascript">




/*var commonParameterString = "txtUsername=" + "<%=request.getParameter("txtUsername")%>" + "&"
        + "txtPassword=" + "<%=request.getParameter("txtPassword")%>" + "&"
        + "urlSet=" + "<%=request.getParameter("urlSet")%>" + "&"
        + "txtIp=" + "<%=request.getParameter("txtIp")%>" + "&"
        + "authPort=" + "<%=request.getParameter("authPort")%>" + "&"
        + "receiverPort=" + "<%=request.getParameter("receiverPort")%>" + "&"
        + "security=" + "<%=request.getParameter("security")%>" + "&"
        + "loadbalancer=" + "<%=request.getParameter("loadbalancer")%>" + "&"
        + "hfStreamTableData=" + "<%=request.getParameter("hfStreamTableData")%>" + "&"
        + "txtServerProfileLocation=" + "<%=request.getParameter("txtServerProfileLocation")%>";

function saveOverwrite(){
    window.location.href = "configure_server_profiles.jsp?" + commonParameterString + "&hfAction=save&force=true";
}

function removeOverwrite(){
    window.location.href = "configure_server_profiles.jsp?" + commonParameterString + "&hfAction=remove&force=true";
}

function reloadPage(){
    window.location.href = "configure_server_profiles.jsp?" + commonParameterString + "&hfAction=load";
}

*/

function showHideDiv(divId) {
    var theDiv = document.getElementById(divId);
    if (theDiv.style.display == "none") {
        theDiv.style.display = "";
    } else {
        theDiv.style.display = "none";
    }
}

//var streamRowNum = 1;
var metaPropertyRowNum = 1;
var correlationPropertyRowNum = 1;
var payloadPropertyRowNum = 1;

function validatePropertyTable(propertyType){
    var propertyRowInputs = document.getElementById(propertyType+"PropertyTable").getElementsByTagName("input");
    var inputName = "";
    for(var i=0; i<propertyRowInputs.length; i++){
        inputName = propertyRowInputs[i].name;
        if(propertyType=="meta" && ((inputName == "<%=META_PROPERTY_KEYS%>" || inputName == "<%=META_PROPERTY_VALUES%>") && propertyRowInputs[i].value == "")){
            return "Meta Property Name or Property Value cannot be empty.";
        }else if(propertyType=="correlation" && ((inputName == "<%=CORRELATION_PROPERTY_KEYS%>" || inputName == "<%=CORRELATION_PROPERTY_VALUES%>") && propertyRowInputs[i].value == "")){
            return "Correlation Property Name or Property Value cannot be empty.";
        }else if(propertyType=="payload" && ((inputName == "<%=PAYLOAD_PROPERTY_KEYS%>" || inputName == "<%=PAYLOAD_PROPERTY_VALUES%>") && propertyRowInputs[i].value == "")){
            return "Payload Property Name or Property Value cannot be empty.";
        }

    }
    return "true";
}

function onAddPropertyClicked(propertyType){
    var result = validatePropertyTable(propertyType);
    if(result == "true"){
        addPropertyRow(propertyType);
    } else {
        CARBON.showInfoDialog(result);
    }
}

function addPropertyRow(propertyType) {
    if(propertyType=="meta"){
        metaPropertyRowNum++;
        var sId = propertyType+"PropertyTable_" + metaPropertyRowNum;
        var tableContent = "<tr id=\""  + sId + "\">" +
                "<td>\n" +
                "                        <input type=\"text\" name=\"<%=META_PROPERTY_KEYS%>\" value=\"\">\n" +
                "                    </td>\n" +
                "                    <td>\n" +
                "<table class=\"no-border-all\">" +
                "         <tr> " +
                "         <td> " +
                "         <table> " +
                "         <tr> " +
                "         <td><input type=\"radio\" name=\"fieldType_" +  sId + "\" value=\"value\" checked=\"checked\"/></td> " +
                "          <td>Value</td> " +
                "         <tr> " +
                "         <tr> " +
                "         <td><input type=\"radio\" name=\"fieldType_" + sId + "\" value=\"expression\"/></td> " +
                "       <td>Expression</td> " +
                "         <tr> " +
                "       </table> " +
                "       </td> " +
                "         <td> " +
                "<input type=\"text\" name=\"<%=META_PROPERTY_VALUES%>\" value=\"\"/>" +
                "         </td> " +
                "         </tr> " +
                "         </table> " +
                "         </td> " +

                "<td>" +
                "<select id=\"PropertyType_" + sId + "\">" +
                "<option value=\"STRING\" selected=\"selected\" >STRING</option>" +
                "<option value=\"INTEGER\">INTEGER</option>" +
                "<option value=\"BOOLEAN\">BOOLEAN</option>" +
                "<option value=\"DOUBLE\">DOUBLE</option>" +
                "<option value=\"FLOAT\">FLOAT</option>" +
                "<option value=\"LONG\">LONG</option>" +
                "</select>" +
                "</td>" +

                "<td> " +
                "<a onClick='javaScript:removePropertyColumn(\""+ sId + "\")' style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Property</a> " +
                "</td> " +
                "</tr>;" ;

    }else if(propertyType=="correlation"){
        correlationPropertyRowNum++;
        var sId = propertyType+"PropertyTable_" + correlationPropertyRowNum;
        var tableContent = "<tr id=\""  + sId + "\">" +
                "<td>\n" +
                "                        <input type=\"text\" name=\"<%=CORRELATION_PROPERTY_KEYS%>\" value=\"\">\n" +
                "                    </td>\n" +
                "                    <td>\n" +
                "<table class=\"no-border-all\">" +
                "         <tr> " +
                "         <td> " +
                "         <table> " +
                "         <tr> " +
                "         <td><input type=\"radio\" name=\"fieldType_" +  sId + "\" value=\"value\" checked=\"checked\"/></td> " +
                "          <td>Value</td> " +
                "         <tr> " +
                "         <tr> " +
                "         <td><input type=\"radio\" name=\"fieldType_" + sId + "\" value=\"expression\"/></td> " +
                "       <td>Expression</td> " +
                "         <tr> " +
                "       </table> " +
                "       </td> " +
                "         <td> " +
                "<input type=\"text\" name=\"<%=CORRELATION_PROPERTY_VALUES%>\" value=\"\"/>" +
                "         </td> " +
                "         </tr> " +
                "         </table> " +
                "         </td> " +
                "<td>" +
                "<select id=\"PropertyType_" + sId + "\">" +
                "<option value=\"STRING\" selected=\"selected\" >STRING</option>" +
                "<option value=\"INTEGER\">INTEGER</option>" +
                "<option value=\"BOOLEAN\">BOOLEAN</option>" +
                "<option value=\"DOUBLE\">DOUBLE</option>" +
                "<option value=\"FLOAT\">FLOAT</option>" +
                "<option value=\"LONG\">LONG</option>" +
                "</select>" +
                "</td>" +
                "<td> " +
                "<a onClick='javaScript:removePropertyColumn(\""+ sId + "\")' style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Property</a> " +
                "</td> " +
                "</tr>;" ;

    }else{
        payloadPropertyRowNum++;
        var sId = propertyType+"PropertyTable_" + payloadPropertyRowNum;
        var tableContent = "<tr id=\""  + sId + "\">" +
                "<td>\n" +
                "                        <input type=\"text\" name=\"<%=PAYLOAD_PROPERTY_KEYS%>\" value=\"\">\n" +
                "                    </td>\n" +
                "                    <td>\n" +
                "<table class=\"no-border-all\">" +
                "         <tr> " +
                "         <td> " +
                "         <table> " +
                "         <tr> " +
                "         <td><input type=\"radio\" name=\"fieldType_" +  sId + "\" value=\"value\" checked=\"checked\"/></td> " +
                "          <td>Value</td> " +
                "         <tr> " +
                "         <tr> " +
                "         <td><input type=\"radio\" name=\"fieldType_" + sId + "\" value=\"expression\"/></td> " +
                "       <td>Expression</td> " +
                "         <tr> " +
                "       </table> " +
                "       </td> " +
                "         <td> " +
                "<input type=\"text\" name=\"<%=PAYLOAD_PROPERTY_VALUES%>\" value=\"\"/>" +
                "         </td> " +
                "         </tr> " +
                "         </table> " +
                "         </td> " +
                "<td>" +
                "<select id=\"PropertyType_" + sId + "\">" +
                "<option value=\"STRING\" selected=\"selected\" >STRING</option>" +
                "<option value=\"INTEGER\">INTEGER</option>" +
                "<option value=\"BOOLEAN\">BOOLEAN</option>" +
                "<option value=\"DOUBLE\">DOUBLE</option>" +
                "<option value=\"FLOAT\">FLOAT</option>" +
                "<option value=\"LONG\">LONG</option>" +
                "</select>" +
                "</td>" +
                "<td> " +
                "<a onClick='javaScript:removePropertyColumn(\""+ sId + "\")' style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Property</a> " +
                "</td> " +
                "</tr>;" ;
    }



    jQuery("#"+propertyType+"PropertyTable").append(tableContent);
    updatePropertyTableData(propertyType);
}


function removePropertyColumn(id) {
    jQuery("#" + id).remove();
    updatePropertyTableData();
}

function updatePropertyTableData(propertyType){
    var tableData = "", inputs, lists, numOfInputs;
    inputs = document.getElementById(propertyType+"PropertyTable").getElementsByTagName("input");
    lists = document.getElementById(propertyType+"PropertyTable").getElementsByTagName("select");
    numOfInputs = inputs.length;
    alert(numOfInputs);
    for(var i=0; i<numOfInputs; i=i+4){
        if(inputs[i].value != "" && inputs[i+3].value != ""){
            tableData = tableData + inputs[i].value;
            if(inputs[i+1].checked){
                tableData = tableData + "::" + "value";
            } else {
                tableData = tableData + "::" + "expression";
            }
            tableData = tableData + "::" + inputs[i+3].value + "::" + lists[i/4].value + ";";
        }
    }
    document.getElementById("hf"+propertyType+"PropertyTableData").value = tableData;
}

function savePropertyTableData(propertyType){
    updatePropertyTableData(propertyType);
    var streamRowNumber = document.getElementById("hfStreamTableRowNumber").value;
    //document.getElementById("hfStreamsTable_" + streamRowNumber).value = document.getElementById("hfPropertyTableData").value;
    document.getElementById(propertyType+"PropertiesTr").style.display = "none";
    jQuery("#streamsTable_" + document.getElementById("hfStreamTableRowNumber").value).css("background-color","");
}

function saveDumpData(){
    var data = "";
    if(document.getElementById("mHeader").checked){
        data = "dump";
    } else{
        data = "notDump";
    }
    data = data + ";";
    if(document.getElementById("mBody").checked){
        data = data + "dump";
    } else{
        data = data + "notDump";
    }
    var streamRowNumber = document.getElementById("hfStreamTableRowNumber").value;
    document.getElementById("hfStreamsTable_" + streamRowNumber).value = document.getElementById("hfStreamsTable_" + streamRowNumber).value + "^" + data;
    document.getElementById("mHeader").checked = "checked";
    document.getElementById("mBody").checked = "checked";
}

function savePropertiesData(propertyType){
    savePropertyTableData(propertyType);
    saveDumpData();
}



function loadPropertyDataTable(propertyType){
    emptyPropertyTable(propertyType);
    var rowNumber =  document.getElementById("hfStreamTableRowNumber").value;
    var configDataString = document.getElementById("streamsTable_" + rowNumber).getElementsByTagName("input")[4].value;
    var propertyDataString = configDataString.split("^")[0];
    var propertyDataArray = propertyDataString.split(";");
    var numOfProperties = 0;
    for(var i=0; i<propertyDataArray.length; i++){
        if(propertyDataArray[i] != ""){
            addPropertyRow();
            numOfProperties++;
        }
    }

    for(var i=0; i<numOfProperties; i=i+1){
        if(propertyDataArray[i].split("::").length == 4){
            jQuery("#"+propertyType+"PropertyTable").find("tr").find("input")[4*i].value = propertyDataArray[i].split("::")[0];
            jQuery("#"+propertyType+"PropertyTable").find("tr").find("input")[4*i+3].value = propertyDataArray[i].split("::")[1];
            jQuery("#"+propertyType+"PropertyTable").find("tr").find("select")[i].value = propertyDataArray[i].split("::")[2];
            if(propertyDataArray[i].split("::")[3] == "value"){
                jQuery("#"+propertyType+"PropertyTable").find("tr").find("input")[4*i+1].checked = true;
            } else if(propertyDataArray[i].split("::")[3] == "expression"){
                jQuery("#"+propertyType+"PropertyTable").find("tr").find("input")[4*i+2].checked = true;
            }
        }
    }
    updatePropertyTableData(propertyType);
}

function loadDumpData(){
    cancelDumpData();
    var rowNumber =  document.getElementById("hfStreamTableRowNumber").value;
    var configDataString = document.getElementById("streamsTable_" + rowNumber).getElementsByTagName("input")[4].value;
    var dumpDataString = "";
    if(configDataString.split("^").length == 2){
        dumpDataString = configDataString.split("^")[1];
        var dumpDataArray = dumpDataString.split(";");
        if(dumpDataArray.length == 2){
            if(dumpDataArray[0] == "dump"){
                document.getElementById("mHeader").checked = "checked";
            } else {
                document.getElementById("mHeader").checked = "";
            }
            if(dumpDataArray[1] == "dump"){
                document.getElementById("mBody").checked = "checked";
            } else {
                document.getElementById("mBody").checked = "";
            }
        }
    }
}

function emptyPropertyTable(propertyType){
    document.getElementById("hf"+propertyType+"PropertyTableData").value = "";
    jQuery("#"+propertyType+"PropertyTable").find("tr").find("input")[0].value = "";
    jQuery("#"+propertyType+"PropertyTable").find("tr").find("input")[3].value = "";
    jQuery("#"+propertyType+"PropertyTable").find("tr").find("select")[0].value = "STRING";
    jQuery("#"+propertyType+"PropertyTable").find("tr").find("input")[1].checked = true;
    var tableRowNumber = jQuery("#"+propertyType+"PropertyTable").find("tr").length;
    var isFirstRow = true;
    //var firstRowId = "";
    var currentRowId;
    var trArray = new Array();
    for(var i=0; i<tableRowNumber; i=i+1){
        currentRowId = jQuery("#"+propertyType+"PropertyTable").find("tr")[i].id;
        if(currentRowId.split("_")[0] == ""+propertyType+"PropertyTable"){
            if(!isFirstRow){
                //jQuery("#" + currentRowId).remove();
                trArray.push(currentRowId);
            }
            isFirstRow = false;
        }
    }
    for(var i=0; i<trArray.length; i++){
        jQuery("#" + trArray[i]).remove();
    }

}

function cancelPropertyTableData(propertyType){
    emptyPropertyTable(propertyType);
    document.getElementById(propertyType+"PropertiesTr").style.display = "none";
    jQuery("#streamsTable_" + document.getElementById("hfStreamTableRowNumber").value).css("background-color","");
}

function cancelDumpData(){
    document.getElementById("mHeader").checked = "checked";
    document.getElementById("mBody").checked = "checked";
}



function submitPage(){
    //updateStreamTableData();
    document.getElementById('hfAction').value='save';
}


</script>



<%
    Mediator mediator = SequenceEditorHelper.getEditingMediator(request, session);
    boolean isExpression = false;
    String val = "";

    if (!(mediator instanceof org.wso2.carbon.mediator.publishevent.ui.PublishEventMediator)) {
        // todo : proper error handling
        throw new RuntimeException("Unable to edit the mediator");
    }
    PublishEventMediator publishEventMediator = (PublishEventMediator) mediator;
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

            <tr id="metaPropertyTr">
                <td colspan="2">
                    <input name="hfmetaPropertyTableData" id="hfmetaPropertyTableData" type="hidden" value="" />


                    <table>
                        <tr>
                            <td>
                                <table id="metaPropertyTable" width="100%" class="styledLeft" style="margin-left: 0px;">
                                    <thead>
                                    <tr>
                                        <th width="30%">
                                            <fmt:message key="mediator.publishEvent.meta.name"/>
                                        </th>
                                        <th width="30%">
                                            <fmt:message key="mediator.publishEvent.meta.value"/>
                                        </th>
                                        <th width="30%">
                                            <fmt:message key="mediator.publishEvent.meta.type"/>
                                        </th>
                                        <th></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr id="metaPropertyTable_1">
                                        <td>
                                            <input type="text" name="<%=META_PROPERTY_KEYS%>" value=""/>
                                        </td>
                                        <td>
                                            <table class="no-border-all">
                                                <tr>
                                                    <td>
                                                        <table>
                                                            <tr>
                                                                <td><input type="radio" name="fieldType_metaPropertyTable_1" value="value" checked="checked"/></td>
                                                                <td><fmt:message key="mediator.publishEvent.meta.value"/></td>
                                                            </tr>
                                                            <tr>
                                                                <td><input type="radio" name="fieldType_metaPropertyTable_1" value="expression"/></td>
                                                                <td><fmt:message key="mediator.publishEvent.meta.expression"/></td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                    <td>
                                                        <input type="text" name="<%=META_PROPERTY_VALUES%>" value=""/>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>

                                        <td>
                                            <select id="metaPropertyType_1">
                                                <option value="STRING" selected="selected" >STRING</option>
                                                <option value="INTEGER">INTEGER</option>
                                                <option value="BOOLEAN">BOOLEAN</option>
                                                <option value="DOUBLE">DOUBLE</option>
                                                <option value="FLOAT">FLOAT</option>
                                                <option value="LONG">LONG</option>
                                            </select>
                                        </td>

                                        <td>
                                            <a onClick='javaScript:removePropertyColumn("metaPropertyTable_1")' style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Property</a>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <table>
                                    <tr>
                                        <td>
                                            <span>
                                                <a onClick='javaScript:onAddPropertyClicked("meta")' style='background-image: url(../admin/images/add.gif);'class='icon-link addIcon'>Add</a>
                                            </span>
                                        </td>
                                        <td>
                                            <span>
                                                <a onClick='javaScript:savePropertiesData("meta")' style='background-image: url(images/save-button.gif);'class='icon-link addIcon'>Update</a>
                                            </span>
                                        </td>

                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>

            <tr><td colspan="3"><h4><fmt:message key="mediator.publishEvent.correlated.header"/></h4></td></tr>

            <tr id="correlationPropertyTr">
                <td colspan="2">
                    <input name="hfcorrelationPropertyTableData" id="hfcorrelationPropertyTableData" type="hidden" value="" />

                    <table>
                        <tr>
                            <td>
                                <table id="correlationPropertyTable" width="100%" class="styledLeft" style="margin-left: 0px;">
                                    <thead>
                                    <tr>
                                        <th width="30%">
                                            <fmt:message key="mediator.publishEvent.correlated.name"/>
                                        </th>
                                        <th width="30%">
                                            <fmt:message key="mediator.publishEvent.correlated.value"/>
                                        </th>
                                        <th width="30%">
                                            <fmt:message key="mediator.publishEvent.correlated.type"/>
                                        </th>
                                        <th></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr id="correlationPropertyTable_1">
                                        <td>
                                            <input type="text" name="<%=CORRELATION_PROPERTY_KEYS%>" value=""/>
                                        </td>
                                        <td>
                                            <table class="no-border-all">
                                                <tr>
                                                    <td>
                                                        <table>
                                                            <tr>
                                                                <td><input type="radio" name="fieldType_correlationPropertyTable_1" value="value" checked="checked"/></td>
                                                                <td><fmt:message key="mediator.publishEvent.correlated.value"/></td>
                                                            </tr>
                                                            <tr>
                                                                <td><input type="radio" name="fieldType_correlationPropertyTable_1" value="expression"/></td>
                                                                <td><fmt:message key="mediator.publishEvent.correlated.expression"/></td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                    <td>
                                                        <input type="text" name="<%=CORRELATION_PROPERTY_VALUES%>" value=""/>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>

                                        <td>
                                            <select id="correlationPropertyType_1">
                                                <option value="STRING" selected="selected" >STRING</option>
                                                <option value="INTEGER">INTEGER</option>
                                                <option value="BOOLEAN">BOOLEAN</option>
                                                <option value="DOUBLE">DOUBLE</option>
                                                <option value="FLOAT">FLOAT</option>
                                                <option value="LONG">LONG</option>
                                            </select>
                                        </td>

                                        <td>
                                            <a onClick='javaScript:removePropertyColumn("correlationPropertyTable_1")' style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Property</a>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <table>
                                    <tr>
                                        <td>
                                            <span>
                                                <a onClick='javaScript:onAddPropertyClicked("correlation")' style='background-image: url(../admin/images/add.gif);'class='icon-link addIcon'>Add</a>
                                            </span>
                                        </td>
                                        <td>
                                            <span>
                                                <a onClick='javaScript:savePropertiesData("correlation")' style='background-image: url(images/save-button.gif);'class='icon-link addIcon'>Update</a>
                                            </span>
                                        </td>

                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>

            <tr><td colspan="3"><h4><fmt:message key="mediator.publishEvent.payload.header"/></h4></td></tr>

            <tr id="payloadPropertyTr">
                <td colspan="2">
                    <input name="hfpayloadPropertyTableData" id="hfpayloadPropertyTableData" type="hidden" value="" />

                    <table>
                        <tr>
                            <td>
                                <table id="payloadPropertyTable" width="100%" class="styledLeft" style="margin-left: 0px;">
                                    <thead>
                                    <tr>
                                        <th width="30%">
                                            <fmt:message key="mediator.publishEvent.payload.name"/>
                                        </th>
                                        <th width="30%">
                                            <fmt:message key="mediator.publishEvent.payload.value"/>
                                        </th>
                                        <th width="30%">
                                            <fmt:message key="mediator.publishEvent.payload.type"/>
                                        </th>
                                        <th></th>
                                    </tr>
                                    </thead>
                                    <tbody>
                                    <tr id="payloadPropertyTable_1">
                                        <td>
                                            <input type="text" name="<%=PAYLOAD_PROPERTY_KEYS%>" value=""/>
                                        </td>
                                        <td>
                                            <table class="no-border-all">
                                                <tr>
                                                    <td>
                                                        <table>
                                                            <tr>
                                                                <td><input type="radio" name="fieldType_payloadPropertyTable_1" value="value" checked="checked"/></td>
                                                                <td><fmt:message key="mediator.publishEvent.payload.value"/></td>
                                                            </tr>
                                                            <tr>
                                                                <td><input type="radio" name="fieldType_payloadPropertyTable_1" value="expression"/></td>
                                                                <td><fmt:message key="mediator.publishEvent.payload.expression"/></td>
                                                            </tr>
                                                        </table>
                                                    </td>
                                                    <td>
                                                        <input type="text" name="<%=PAYLOAD_PROPERTY_VALUES%>" value=""/>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>

                                        <td>
                                            <select id="payloadPropertyType_1">
                                                <option value="STRING" selected="selected" >STRING</option>
                                                <option value="INTEGER">INTEGER</option>
                                                <option value="BOOLEAN">BOOLEAN</option>
                                                <option value="DOUBLE">DOUBLE</option>
                                                <option value="FLOAT">FLOAT</option>
                                                <option value="LONG">LONG</option>
                                            </select>
                                        </td>

                                        <td>
                                            <a onClick='javaScript:removePropertyColumn("payloadPropertyTable_1")' style='background-image: url(../admin/images/delete.gif);'class='icon-link addIcon'>Remove Property</a>
                                        </td>
                                    </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                        <tr>
                            <td>
                                <table>
                                    <tr>
                                        <td>
                                            <span>
                                                <a onClick='javaScript:onAddPropertyClicked("payload")' style='background-image: url(../admin/images/add.gif);'class='icon-link addIcon'>Add</a>
                                            </span>
                                        </td>
                                        <td>
                                            <span>
                                                <a onClick='javaScript:savePropertiesData("payload")' style='background-image: url(images/save-button.gif);'class='icon-link addIcon'>Update</a>
                                            </span>
                                        </td>

                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
    </div>
</fmt:bundle>