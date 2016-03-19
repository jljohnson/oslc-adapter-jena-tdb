<!DOCTYPE html>
<%--
/*********************************************************************************************
 * Copyright (c) 2014 Model-Based Systems Engineering Center, Georgia Institute of Technology.
 *                         http://www.mbse.gatech.edu/
 *                  http://www.mbsec.gatech.edu/research/oslc
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Eclipse Distribution License is available at
 *  http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *  Contributors:
 *
 *	   Axel Reichwein, Koneksys (axel.reichwein@koneksys.com)		
 *******************************************************************************************/
--%>

<%@ page contentType="text/html" language="java" pageEncoding="UTF-8"%>
<%@ page
	import="org.eclipse.lyo.oslc4j.core.model.ServiceProviderCatalog"%>
<%@ page import="org.eclipse.lyo.oslc4j.core.model.ServiceProvider"%>
<%@ page import="org.eclipse.lyo.oslc4j.core.model.Link"%>
<%@ page import="java.net.URI"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.Collection" %>
<%@ page import="org.eclipse.lyo.adapter.tdb.application.PropertyObjectPair" %>
<%@ page import="com.hp.hpl.jena.rdf.model.Resource" %>
<%@ page import="org.eclipse.lyo.oslc4j.core.model.AbstractResource" %>
<%
Map<Resource, Collection<PropertyObjectPair>> resourceValueMap = (Map<Resource, Collection<PropertyObjectPair>>) request.getAttribute("resourceValueMap");
String requestURL = (String)request.getAttribute("requestURL");

%>
<html>
<head>

<meta http-equiv="Content-Type" content="text/html;charset=utf-8">
<title>Triplestore OSLC Adapter: Triplestore Resources</title>
<link rel="stylesheet" type="text/css"
	href="<%=request.getContextPath()%>/css/simple.css">
<link href='http://fonts.googleapis.com/css?family=Open+Sans:400,700'
	rel='stylesheet' type='text/css'>
<link rel="shortcut icon" href="<%=request.getContextPath()%>/images/100px_white-oslc-favicon.ico">


</head>
<body onload="">

	<!-- header -->
	<p id="title">Triplestore OSLC Adapter: Triplestore Resources</p>

	<!-- main content -->
	<div id="main-body">
		
		<!-- oslc logo and adapter details -->
		<a id="oslclogo" href="http://open-services.net/" target="_blank"><img
			src="<%=request.getContextPath()%>/images/oslcLg.png"></a>
		<div id="adapter-details">
			<p class="word-break">
				This document: <a href="<%= requestURL %>"> <%= requestURL %>
				</a><br> Adapter Publisher: <a class="notfancy"
					href="http://www.mbsec.gatech.edu/research/oslc" target="_blank">Georgia
					Institute of Technology OSLC Tools Project</a><br> Adapter
				Identity: org.eclipse.lyo.adapter.tdb
			</p>
		</div>
		<br>

		<!-- resource type and name -->
		<h1><span id="metainfo">Triplestore Resources </span></h1>
		<br>

		<!-- resource attributes and relationships -->			
		<% Object[] elementsArray =  resourceValueMap.keySet().toArray();  %>
		<% int elementsSize =  resourceValueMap.keySet().size();  %>
		<% int i =  0;  %>
		<% if( elementsSize > 0) {  %>
		<p><span id="metainfo">Resources</span></p>
		<table>
			<tr>
				<% while(elementsSize > 0) {;  %>
				<% Resource element = (Resource)elementsArray[i]; %>
				<% String displayedElementURI = element.getURI().replace("http://localhost:8585/oslc4jtdb/services/default/resources/", ""); %>
				<td><a href="<%= element.getURI() %>"> <%=displayedElementURI%></a></td>
				<%i++;%>
				<!-- change here maximum number of cells to be displayed in each table row -->
				<% if( i % 1 == 0) {  %>
			</tr>
			<tr>
				<% }  %>
				<%elementsSize--;%>
				<% };  %>
			</tr>
		</table>
		<% } %>
										

	</div>


	<!-- footer -->
	<p id="footer">OSLC Triplestore Adapter 0.1 brought to you by <a class="nofancyfooter"
	 href="https://www.eclipse.org/lyo/" target="_blank">Eclipse Lyo</a>
	 
</body>
</html>


