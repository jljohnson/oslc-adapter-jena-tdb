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
package org.eclipse.lyo.adapter.tdb.clients;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.wink.client.handlers.BasicAuthSecurityHandler;
import org.apache.wink.client.handlers.ClientHandler;


import org.eclipse.lyo.adapter.tdb.services.OSLC4JTDBApplication;
import org.eclipse.lyo.oslc4j.client.OslcRestClient;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.QueryCapability;
import org.eclipse.lyo.oslc4j.core.model.Service;
import org.eclipse.lyo.oslc4j.core.model.ServiceProvider;
import org.eclipse.lyo.oslc4j.core.model.ServiceProviderCatalog;
import org.eclipse.lyo.oslc4j.provider.jena.JenaProvidersRegistry;
//import org.eclipse.lyo.oslc4j.provider.json4j.Json4JProvidersRegistry;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;







/**
 * The main() method of OSLCWebClient4CreatingSimulinkModel creates several new Simulink elements 
 * in a Simulink model using the OSLC Simulink adapter. First, the Simulink elements 
 * to be added to the model are created as POJOs. Second, the POJOs are transformed 
 * into RDF/XML by OSLC4J, and then sent as the body of HTTP requests to the creationfactory services
 * of the OSLC Simulink adapter. OSLCWebClient4CreatingSimulinkModel 
 * adds Simulink elements to a Simulink model through individual HTTP requests.
 *  
 * 
 * @author Axel Reichwein (axel.reichwein@koneksys.com)
 */
public class OSLCTriplestoreAdapterResourceCreationClient {

	private static final Set<Class<?>> PROVIDERS = new HashSet<Class<?>>();

	static {
		PROVIDERS.addAll(JenaProvidersRegistry.getProviders());
		// PROVIDERS.addAll(Json4JProvidersRegistry.getProviders());
	}

	public static void main(String[] args) {

		String baseHTTPURI = "http://localhost:" + OSLC4JTDBApplication.portNumber + "/oslc4jtdb";
		String projectId = "default";

		// URI of the HTTP request
		String tdbResourceCreationFactoryURI = baseHTTPURI + "/services/"
				+ projectId + "/resources";

		// create RDF to add to the triplestore
		Model resourceRDFModel = ModelFactory.createDefaultModel();
		Resource resource = ResourceFactory.createResource("http://localhost:8585/oslc4jtdb/services/default/resources/newBlock4"); 
		Property property = ResourceFactory.createProperty("http://localhost:8585/oslc4jtdb/services/default/resources/newProperty4");
		RDFNode object = ResourceFactory.createResource("http://localhost:8585/oslc4jtdb/services/default/resources/newObject4");
		resourceRDFModel.add(resource, property, object);		
		StringWriter out = new StringWriter();
		resourceRDFModel.write(out, "RDF/XML");
		resourceRDFModel.write(System.out);
		
		HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(tdbResourceCreationFactoryURI);
        String xml = out.toString();
        HttpEntity entity;
		try {
			entity = new ByteArrayEntity(xml.getBytes("UTF-8"));
			post.setEntity(entity);
	        post.setHeader("Accept", "application/rdf+xml");
	        HttpResponse response = client.execute(post);
	       
			
	        System.out.println("Response Code : " 
	                + response.getStatusLine().getStatusCode()); 
	        
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		

		

	}

}
