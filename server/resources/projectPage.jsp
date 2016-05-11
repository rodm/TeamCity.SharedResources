<%--
  ~ Copyright 2000-2013 JetBrains s.r.o.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>
<%@ include file="/include-internal.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="jetbrains.buildServer.sharedResources.SharedResourcesPluginConstants" %>
<%@ page import="jetbrains.buildServer.sharedResources.model.resources.ResourceType" %>


<jsp:useBean id="bean" scope="request" type="jetbrains.buildServer.sharedResources.pages.SharedResourcesBean"/>
<%--@elvariable id="usages" type="java.util.Map<jetbrains.buildServer.sharedResources.model.resources.Resource, java.util.Map<jetbrains.buildServer.serverSide.SBuildType,java.util.List<jetbrains.buildServer.sharedResources.model.Lock>>"--%>
<%--@elvariable id="resourceDescriptors" type="java.util.Map<jetbrains.buildServer.serverSide.SProject,java.util.Map<java.lang.String,jetbrains.buildServer.sharedResources.server.project.ResourceDescriptor>>"--%>

<c:set var="project" value="${bean.project}"/>

<c:set var="PARAM_RESOURCE_NAME" value="<%=SharedResourcesPluginConstants.WEB.PARAM_RESOURCE_NAME%>"/>
<c:set var="PARAM_PROJECT_ID" value="<%=SharedResourcesPluginConstants.WEB.PARAM_PROJECT_ID%>"/>
<c:set var="PARAM_RESOURCE_QUOTA" value="<%=SharedResourcesPluginConstants.WEB.PARAM_RESOURCE_QUOTA%>"/>
<c:set var="PARAM_RESOURCE_TYPE" value="<%=SharedResourcesPluginConstants.WEB.PARAM_RESOURCE_TYPE%>"/>
<c:set var="PARAM_RESOURCE_VALUES" value="<%=SharedResourcesPluginConstants.WEB.PARAM_RESOURCE_VALUES%>"/>
<c:set var="PARAM_OLD_RESOURCE_NAME" value="<%=SharedResourcesPluginConstants.WEB.PARAM_OLD_RESOURCE_NAME%>"/>
<c:set var="PARAM_RESOURCE_STATE" value="<%=SharedResourcesPluginConstants.WEB.PARAM_RESOURCE_STATE%>"/>

<c:set var="ACTIONS" value="<%=SharedResourcesPluginConstants.WEB.ACTIONS%>"/>

<c:set var="type_quota" value="<%=ResourceType.QUOTED%>"/>
<c:set var="type_custom" value="<%=ResourceType.CUSTOM%>"/>
<c:url var="url" value="/admin/editProject.html?projectId=${project.externalId}&tab=JetBrains.SharedResources"/>

<style type="text/css">
  .resourcesDialog {
    width: 49em;
  }
</style>

<script type="text/javascript">
  BS.SharedResourcesActions = {
    getCommonParams: function () {
      // if quota checkbox in unchecked, send no quota info
      var type = $j('#resource_type option:selected').val();
      var params = {};
      params['${PARAM_PROJECT_ID}'] = '${project.projectId}';
      params['${PARAM_RESOURCE_NAME}'] = $j('#resource_name').val();
      params['${PARAM_RESOURCE_STATE}'] = $j('#resource_enabled').prop('checked');

      // infinite
      if (type === 'infinite') {
        params['${PARAM_RESOURCE_TYPE}'] = 'quoted';
      }
      // quoted
      if (type === 'quoted') {
        params['${PARAM_RESOURCE_TYPE}'] = 'quoted';
        params['${PARAM_RESOURCE_QUOTA}'] = $j('#resource_quota').val();
      }
      // custom
      if (type === 'custom') {
        params['${PARAM_RESOURCE_TYPE}'] = 'custom';
        params['${PARAM_RESOURCE_VALUES}'] = $j('#customValues').val();
      }
      return params;
    },

    actionsUrl: window['base_uri'] + "${ACTIONS}",
    addResource: function () {
      var params = this.getCommonParams();
      params['action'] = 'addResource';
      BS.ajaxRequest(this.actionsUrl, {
        parameters: params,
        onComplete: function (transport) {
          var errors = BS.XMLResponse.processErrors(transport.responseXML, {
            onNameError: function (elem) {
              $j('#error_Name').html("The name is already used");
              BS.Util.show('error_Name');
            }
          });
          BS.ResourceDialog.afterSubmit(errors);
        }
      });
    },

    editResource: function (old_resource_name) {
      var params = this.getCommonParams();
      params['${PARAM_OLD_RESOURCE_NAME}'] = old_resource_name;
      params['action'] = 'editResource';
      BS.ajaxRequest(this.actionsUrl, {
        parameters: params,
        onComplete: function (transport) {
          var errors = BS.XMLResponse.processErrors(transport.responseXML, {
            onNameError: function (elem) {
              $j('#error_Name').html("Name is already used");
              BS.Util.show('error_Name');
            }
          });
          BS.ResourceDialog.afterSubmit(errors);
        }
      });
    },

    deleteResource: function (resource_name) {
      var params = {};
      params['${PARAM_PROJECT_ID}'] = '${project.projectId}';
      params['${PARAM_RESOURCE_NAME}'] = resource_name;
      params['action'] = 'deleteResource';

      if (confirm('Are you sure you want to delete this resource? It may result in errors if the name is used as a parameter reference.')) {
        BS.ajaxRequest(this.actionsUrl, {
          parameters: params,
          onSuccess: function () {
            window.location.reload();
          }
        });
      }
    },

    alertCantDelete: function (resource_name) {
      alert('Resource ' + resource_name + " can't be deleted because it is in use");
    },

    enableDisableResource: function (resource_name, new_state) {
      var params = {};
      params['${PARAM_PROJECT_ID}'] = '${project.projectId}';
      params['${PARAM_RESOURCE_NAME}'] = resource_name;
      params['${PARAM_RESOURCE_STATE}'] = new_state;
      params['action'] = 'enableDisableResource';
      if (confirm('Are you sure you want to ' + (new_state ? 'enable' : 'disable') + ' this resource?')) {
        BS.ajaxRequest(this.actionsUrl, {
          parameters: params,
          onSuccess: function () {
            window.location.reload();
          }
        });
      }
    }
  };
</script>

<script type="text/javascript">
  var myValues;
  var r;
  var v;
  <c:forEach var="item" items="${bean.allResources}">
  <c:set var="type" value="${item.type}"/>
  r = {
    name: '<bs:escapeForJs text="${item.name}"/>',
    type: '${item.type}',
    enabled: ${item.enabled}
  };
  <c:choose>

  <%-- quoted resource--%>
  <c:when test="${type == type_quota}">
  r['quota'] = '${item.quota}';
  r['infinite'] = ${item.infinite};
  BS.ResourceDialog.myData['<bs:escapeForJs text="${item.name}"/>'] = r;
  </c:when>

  <%-- custom resource--%>
  <c:when test="${type == type_custom}">
  myValues = [];
  <c:forEach items="${item.values}" var="val">
  myValues.push('<bs:escapeForJs text="${val}"/>');
  </c:forEach>
  r['customValues'] = myValues;
  BS.ResourceDialog.myData['<bs:escapeForJs text="${item.name}"/>'] = r;
  </c:when>

  <c:otherwise>
  console.log('Resource [<bs:escapeForJs text="${item.name}"/>] was not recognized');
  </c:otherwise>
  </c:choose>
  BS.ResourceDialog.existingResources['<bs:escapeForJs text="${item.name}"/>'] = true;
  </c:forEach>
</script>

<div class="section noMargin">
  <h2 class="noBorder">Shared Resources</h2>
  <div class="grayNote">
    This page contains shared resources defined in the current project, as well as inherited resources.<bs:help file="Shared+Resources"/>
  </div>

  <bs:messages key="<%=SharedResourcesPluginConstants.WEB.ACTION_MESSAGE_KEY%>"/>

  <c:if test="${not project.readOnly}">
  <forms:addButton id="addNewResource"
                   onclick="BS.ResourceDialog.showDialog(); return false">Add new resource</forms:addButton>
  </c:if>

  <div>
    <%@ include file="_resourcesDialog.jspf" %>
    <%@ include file="_displayErrors.jspf" %>

    <c:choose>
      <c:when test="${not empty bean.myResources}">
        <p style="margin-top: 2em">Resources defined in the current project</p>
        <l:tableWithHighlighting id="resourcesTable"
                                 className="parametersTable"
                                 mouseovertitle="Click to edit resource"
                                 highlightImmediately="true">
          <tr>
            <th style="width: 45%">Resource Name</th>
            <th colspan="4">Description</th>
          </tr>
          <c:set var="resourcesToDisplay" value="${bean.myResources}"/>
          <c:set var="allowChange" value="${not project.readOnly}"/>
          <%@ include file="_displayResources.jspf" %>
        </l:tableWithHighlighting>
      </c:when>
      <c:otherwise>
        <%--<p>
          <c:out value="There are no resources defined in the current project."/>
        </p>--%>
      </c:otherwise>
    </c:choose>

    <c:forEach var="ir" items="${bean.inheritedResources}">
      <c:set var="p" value="${ir.key}"/> <%-- project --%>
      <c:set var="pr" value="${ir.value}"/> <%--Map<String, Resource>--%>
      <c:if test="${not empty pr}">
        <p style="margin-top: 2em">
          Resources inherited from
          <authz:authorize projectId="${p.externalId}" allPermissions="EDIT_PROJECT" >
            <jsp:attribute name="ifAccessGranted">
              <c:url var="editUrl" value="/admin/editProject.html?projectId=${p.externalId}&tab=JetBrains.SharedResources"/>
              <a href="${editUrl}"><c:out value="${p.extendedFullName}"/></a>
            </jsp:attribute>
            <jsp:attribute name="ifAccessDenied">
              <bs:projectLink project="${p}"><c:out value="${p.extendedFullName}"/></bs:projectLink>
            </jsp:attribute>
          </authz:authorize>
        </p>
        <table class="parametersTable">
          <tr>
            <th style="width: 45%">Resource Name</th>
            <th colspan="2">Description</th>
          </tr>
          <c:set var="resourcesToDisplay" value="${pr}"/>
          <c:set var="allowChange" value="${false}"/>
          <%@ include file="_displayResources.jspf" %>
        </table>
      </c:if>
    </c:forEach>
  </div>
</div>

<div>
  <table class="parametersTable">
    <tr>
      <th style="width: 20%">Project</th>
      <th colspan="2">Resource Name</th>
    </tr>
    <c:forEach var="rditem" items="${resourceDescriptors}">
      <c:set var="prr" value="${rditem.key}"/>
      <c:set var="rds" value="${rditem.value}"/>
      <c:forEach var="rrrd" items="${rds}"> <%--entry<String, RD>.--%>
        <tr>
          <td><bs:out value="${prr.name}"/> </td>
          <td><bs:out value="${rrrd.key}"/></td>
        </tr>
      </c:forEach>
    </c:forEach>
  </table>
</div>
