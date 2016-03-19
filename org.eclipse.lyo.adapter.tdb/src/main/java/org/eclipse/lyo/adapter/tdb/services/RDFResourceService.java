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
package org.eclipse.lyo.adapter.tdb.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;


import org.eclipse.lyo.adapter.tdb.application.PropertyObjectPair;
import org.eclipse.lyo.adapter.tdb.application.TDBManager;
import org.eclipse.lyo.adapter.tdb.resources.Constants;
import org.eclipse.lyo.oslc4j.core.annotation.OslcCreationFactory;
import org.eclipse.lyo.oslc4j.core.annotation.OslcQueryCapability;
import org.eclipse.lyo.oslc4j.core.annotation.OslcService;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.tdb.TDBFactory;

/**
 * This servlet contains the implementation of OSLC RESTful web services for
 * Simulink Block resources.
 * 
 * The servlet contains web services for: <ul margin-top: 0;>
 * <li>returning specific Simulink Block resources in HTML and other formats
 * </li>
 * <li>returning all Simulink Block resources within a specific Simulink model
 * in HTML and other formats</li>
 * <li>adding new Simulink Block resources to a specific Simulink model</li>
 * </ul>
 * 
 * @author Axel Reichwein (axel.reichwein@koneksys.com)
 */
@OslcService(org.eclipse.lyo.adapter.tdb.resources.Constants.TDBRDF_RESOURCE_DOMAIN)
@Path("{modelName}/resources")
public class RDFResourceService {

	@Context
	private HttpServletRequest httpServletRequest;
	@Context
	private HttpServletResponse httpServletResponse;
	@Context
	private UriInfo uriInfo;

	// @OslcQueryCapability(title = "Simulink Block Query Capability", label =
	// "Simulink Block Catalog Query", resourceShape =
	// OslcConstants.PATH_RESOURCE_SHAPES
	// + "/" + Constants.PATH_SIMULINK_BLOCK, resourceTypes = {
	// Constants.TYPE_SIMULINK_BLOCK }, usages = {
	// OslcConstants.OSLC_USAGE_DEFAULT })
	@OslcQueryCapability(title = "TDB Resource Query Capability", label = "RDF Resource Catalog Query", resourceShape = OslcConstants.PATH_RESOURCE_SHAPES
			+ "/" + Constants.PATH_TDBRDF_RESOURCE, resourceTypes = { Constants.TYPE_TDBRDF_RESOURCE}, usages = { OslcConstants.OSLC_USAGE_DEFAULT })	
	@GET
	@Produces({ OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_XML, OslcMediaType.APPLICATION_JSON })
	public void getResources()
					throws IOException, ServletException {			
		TDBManager.adapterModel.write(httpServletResponse.getOutputStream(), null);
	}

	

	
	@GET
	@Produces(MediaType.TEXT_HTML)
	public void getHtmlResources() {
		String requestURL = httpServletRequest.getRequestURL().toString();
		if (TDBManager.resourceValueMap != null) {
			httpServletRequest.setAttribute("resourceValueMap", TDBManager.resourceValueMap);
			httpServletRequest.setAttribute("requestURL", requestURL);

			RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/triplestore/rdf_resources_html.jsp");
			try {
				rd.forward(httpServletRequest, httpServletResponse);
			} catch (Exception e) {
				e.printStackTrace();
				throw new WebApplicationException(e);
			}
		}
	}

	@GET
	@Path("{resourceID}")
	@Produces(MediaType.TEXT_HTML)
	public void getHtmlResource(@PathParam("modelName") final String modelName, @PathParam("resourceID") final String resourceID) {
		String completeResourceURI = "http://localhost:8585/oslc4jtdb/services/" + modelName +"/resources/" + resourceID;
		
		if(TDBManager.vocabModelMap.containsKey(completeResourceURI)){
			// uri is a vocabulary uri
			Model resourceRDFModel = TDBManager.vocabModelMap.get(completeResourceURI);			
			try {
				resourceRDFModel.write(httpServletResponse.getOutputStream(), null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		
		Resource specificResource = null;
		Collection<PropertyObjectPair> propertyObjectPairs = null;
		for (Resource resource : TDBManager.resourceValueMap.keySet()) {
			if (resource.getURI().equals(completeResourceURI)) {
				specificResource = resource;
				propertyObjectPairs = TDBManager.resourceValueMap.get(resource);
				break;
			}
		}

		String requestURL = httpServletRequest.getRequestURL().toString();
		if (specificResource != null) {
			httpServletRequest.setAttribute("specificResource", specificResource);
			httpServletRequest.setAttribute("propertyObjectPairs", propertyObjectPairs);
			httpServletRequest.setAttribute("requestURL", requestURL);

			RequestDispatcher rd = httpServletRequest.getRequestDispatcher("/triplestore/rdf_resource_html.jsp");
			try {
				rd.forward(httpServletRequest, httpServletResponse);
			} catch (Exception e) {
				e.printStackTrace();
				throw new WebApplicationException(e);
			}
		}
	}

	




	@GET
	@Path("{resourceID}")
	@Produces({ OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_JSON })
	public void getResource(@PathParam("modelName") final String modelName, @PathParam("resourceID") final String resourceID) {		
		String completeResourceURI = "http://localhost:8585/oslc4jtdb/services/" + modelName +"/resources/" + resourceID;
		if(TDBManager.vocabModelMap.containsKey(completeResourceURI)){
			// uri is a vocabulary uri
			Model resourceRDFModel = TDBManager.vocabModelMap.get(completeResourceURI);
			TDBManager.addNamespacePrefix(resourceRDFModel);
			try {
				resourceRDFModel.write(httpServletResponse.getOutputStream(), null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return;
		}
		
		Resource specificResource = null;
		Collection<PropertyObjectPair> propertyObjectPairs = null;
		for (Resource resource : TDBManager.resourceValueMap.keySet()) {
			if (resource.getURI().equals(completeResourceURI)) {
				specificResource = resource;
				propertyObjectPairs = TDBManager.resourceValueMap.get(resource);
				break;
			}
		}

		if (specificResource != null) {
			Model resourceRDFModel = ModelFactory.createDefaultModel();
			TDBManager.addNamespacePrefix(resourceRDFModel);

			for (PropertyObjectPair propertyObjectPair : propertyObjectPairs) {
				Property property = propertyObjectPair.getProperty();
				RDFNode object = propertyObjectPair.getObject();
				// System.out.println(property.getURI() + " -> " +
				// object.toString());

				// add to model for rdf serialization
				resourceRDFModel.add(specificResource, property, object);
			}

			// write the RDF
			// resourceRDFModel.setNsPrefix( "integrity_requirement",
			// "http://www.ptc.com/solutions/application-lifecycle-management/Requirement/"
			// );
			try {
				resourceRDFModel.write(httpServletResponse.getOutputStream(), null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	
	@OslcCreationFactory(title = "TDB Resource Creation Factory", label = "TDB Resource Creation", resourceShapes = { OslcConstants.PATH_RESOURCE_SHAPES
			+ "/" + Constants.PATH_TDBRDF_RESOURCE }, resourceTypes = { Constants.TYPE_TDBRDF_RESOURCE }, usages = { OslcConstants.OSLC_USAGE_DEFAULT })
	@POST
	public Response addResource(@PathParam("modelName") final String modelName) throws IOException, ServletException {
		// add RDF to oslc adapter ResourceValueMap
		Model incomingModel = ModelFactory.createDefaultModel();
		incomingModel.read(httpServletRequest.getInputStream(), "RDF/XML");
		incomingModel.write(System.out);
		TDBManager.addTriplesToResourceValueMap(incomingModel);
		
		// add RDF to original triplestore
		Dataset inputDataset = TDBFactory.createDataset(OSLC4JTDBApplication.triplestoreDirectory);
		Model inputModel = inputDataset.getDefaultModel();
		inputModel.add(incomingModel);
		inputModel.close();
		inputDataset.close();

		return Response.ok().build();
	}
	
	
}
