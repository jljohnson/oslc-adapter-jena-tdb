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
<%@ page import="java.util.Map"%>
<%@ page import="java.util.Collection"%>
<%@ page
	import="org.eclipse.lyo.adapter.tdb.application.PropertyObjectPair"%>
<%@ page import="com.hp.hpl.jena.rdf.model.Resource"%>
<%
Resource resource = (Resource)request.getAttribute("specificResource");;
Collection<PropertyObjectPair> propertyObjectPairs = (Collection<PropertyObjectPair>)request.getAttribute("propertyObjectPairs");
String requestURL = (String)request.getAttribute("requestURL");
%>
<html>
<head>

<meta http-equiv="Content-Type" content="text/html;charset=utf-8">
<title>Triplestore OSLC Adapter: Triplestore Resource</title>
<link rel="stylesheet" type="text/css"
	href="<%=request.getContextPath()%>/css/simple.css">
<link href='http://fonts.googleapis.com/css?family=Open+Sans:400,700'
	rel='stylesheet' type='text/css'>
<link rel="shortcut icon"
	href="<%=request.getContextPath()%>/images/100px_white-oslc-favicon.ico">


</head>
<body onload="">

	<!-- header -->
	<p id="title">Triplestore OSLC Adapter: Triplestore Resource</p>

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
		<h1>
			<span id="metainfo">Triplestore Resource </span></h1>
		<br>

		<!-- resource attributes and relationships -->
		<p>
			<span id="metainfo">Resource URI</span>
			<a
					href="<%= resource.getURI() %>">
						<%=resource.getURI()%></a>
		</p>


		<% if( propertyObjectPairs.size() > 0) {  %>
		<p>
			<span id="metainfo">Resource Properties</span>
		</p>
		<% } %>
		<table>
			<% for (PropertyObjectPair propertyObjectPair : propertyObjectPairs) {  %>
			<tr>
				<% String propertyURI = propertyObjectPair.getProperty().getURI(); %>
				<% String displayedPropertyURI = propertyURI.replace("http://localhost:8585/oslc4jtdb/", ""); %>
				<% displayedPropertyURI = displayedPropertyURI.replace("services/default/resources/", ""); %>
				<td><a href="<%= propertyObjectPair.getProperty().getURI() %>">
						<%=displayedPropertyURI%></a></td>
				<% if( propertyObjectPair.getObject().isResource()) {  %>
				<td><a
					href="<%= propertyObjectPair.getObject().asResource().getURI() %>">
						<%=propertyObjectPair.getObject().asResource().getURI()%></a></td>				
				<% } else {  %>
				<td><%=propertyObjectPair.getObject().toString().replace("^^http://www.w3.org/1999/02/22-rdf-syntax-ns#XMLLiteral", "")%></td>
				<% } %>


				
			</tr>
			<tr></tr>
			<tr></tr>
			<% } %>
		</table>




		







	</div>


	<!-- footer -->
	<p id="footer">
		OSLC Triplestore Adapter 0.1 brought to you by <a
			class="nofancyfooter" href="https://www.eclipse.org/lyo/"
			target="_blank">Eclipse Lyo</a>
</body>
</html>


