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

package org.eclipse.lyo.adapter.tdb.application;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.lyo.adapter.tdb.services.OSLC4JTDBApplication;
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.Link;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.tdb.TDBFactory;
import com.hp.hpl.jena.util.ResourceUtils;

/**
 * SimulinkManager is responsible for all the communication between the Simulink
 * application and the OSLC Simulink adapter. It is used to load Simulink
 * models, retrieve Simulink elements from models, and map Simulink elements to
 * OSLC resources described as POJOs
 * 
 * @author Axel Reichwein (axel.reichwein@koneksys.com)
 */
public class TDBManager {

	static int sessionID = 1;

	public static Map<Resource, Collection<PropertyObjectPair>> resourceValueMap = null;
	public static Model adapterModel = null;
	public static Map<String, Model> vocabModelMap = null;

	static StringBuffer buffer;

	public static String baseHTTPURI = "http://localhost:" + OSLC4JTDBApplication.portNumber + "/oslc4jtdb";
	static String projectId;

	public static void main(String[] args) {
		loadTDBContent();
	}

	public static synchronized void loadTDBContent() {
		if (resourceValueMap != null) {
			return;
		}
		Thread thread = new Thread() {
			public void start() {

				// load model from input triplestore
				Dataset inputDataset = TDBFactory.createDataset(OSLC4JTDBApplication.triplestoreDirectory);
				Model inputModel = inputDataset.getDefaultModel();

				// map triples of triplestore into RDF model in memory
				addRDFModelToOSLCAdapterModel(inputModel);

				// register rdf vocabulary namespce prefixes
				addNamespacePrefix(adapterModel);
				
				// traversal of adapter graph for saving all triples in memory
				resourceValueMap = new HashMap<Resource, Collection<PropertyObjectPair>>();
				addTriplesToResourceValueMap(adapterModel);
				
				
				
				
				// adapterModel.close(); // service for publishing triplestore
				// content in RDF/XML requires Model to stay opened

				
				
				
				// closing input triplestore
				inputModel.close();
				inputDataset.close();

				// Applications are expected to obey the lock contract,
				// that is, they must not do update operations if they have a
				// read lock as there can be other application threads reading
				// the model concurrently.

				// for (Resource resource : resourceValueMap.keySet()) {
				// System.out.println("");
				// System.out.println("********************");
				// System.out.println(resource.getURI());
				// Collection<PropertyObjectPair> propertyObjectPairs =
				// resourceValueMap.get(resource);
				// for (PropertyObjectPair propertyObjectPair :
				// propertyObjectPairs) {
				// Property property = propertyObjectPair.getProperty();
				// RDFNode object = propertyObjectPair.getObject();
				// System.out.println(property.getURI() + " -> " +
				// object.toString());
				// }
				// }

			}
		};
		long startTime = System.currentTimeMillis();
		thread.start();
		try {
			thread.join();
			long endTime = System.currentTimeMillis();
			long duration = endTime - startTime;
			System.out.println("OSLC Adapter <-> Triplestore Interaction in " + (duration / 1000) + " seconds");
			System.out.println("Data read from triplestore " + OSLC4JTDBApplication.triplestoreDirectory + " at "
					+ new Date().toString());
		} catch (

		InterruptedException e)

		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void addTriplesToResourceValueMap(Model model) {

		vocabModelMap = new HashMap<String, Model>();

		StmtIterator statementsIT2 = model.listStatements();
		while (statementsIT2.hasNext()) {
			Statement statement = statementsIT2.next();
			// if(!statement.isReified()){ // does not work
			Resource subject = statement.getSubject();
			Property property = statement.getPredicate();
			RDFNode object = statement.getObject();

			if (subject.getURI() != null) {

				// make sure that uri does not contain a fragment (part after #)
				URI uri = URI.create(subject.getURI());
				if (uri.getFragment() != null) {
					// remove fragment
					String uriWithoutFragment = subject.getURI().replace("#" + uri.getFragment(), "");
					System.out.println(uriWithoutFragment);

					// get map that links uri without fragment (vocabulary uri)
					// with RDF Model
					Model vocabModel;
					if (vocabModelMap.containsKey(uriWithoutFragment)) {
						vocabModel = vocabModelMap.get(uriWithoutFragment);
					} else {
						vocabModel = ModelFactory.createDefaultModel();
					}
					// populate RDF Model with RDF statement
					vocabModel.add(statement);
					vocabModelMap.put(uriWithoutFragment, vocabModel);
				}

				PropertyObjectPair propertyObjectPair = new PropertyObjectPair(property, object);
				if (!resourceValueMap.containsKey(subject)) {
					Collection<PropertyObjectPair> newPropertyObjectPairs = new ArrayList<PropertyObjectPair>();
					resourceValueMap.put(subject, newPropertyObjectPairs);
					newPropertyObjectPairs.add(propertyObjectPair);

				} else {
					Collection<PropertyObjectPair> propertyObjectPairs = resourceValueMap.get(subject);
					propertyObjectPairs.add(propertyObjectPair);
				}

			}
		}

	}

	public static void addRDFModelToOSLCAdapterModel(Model inputModel) {
		adapterModel = ModelFactory.createDefaultModel();

		// predefined property to save original resource uri
		Property originalURIProperty = ResourceFactory
				.createProperty("http://localhost:" + OSLC4JTDBApplication.portNumber + "/oslc4jtdb/", "originaluri");

		// traversal of input graph
		// StmtIterator statementsIT = inputModel.listStatements();
		// while (statementsIT.hasNext()) {
		// Statement statement = statementsIT.next();
		// // if(!statement.isReified()){ // does not work
		// Resource subject = statement.getSubject();
		// Property property = statement.getPredicate();
		// RDFNode object = statement.getObject();

		// // Create a SPARQL query to getr all triples
		String queryString = "SELECT ?subject ?property ?object " + "WHERE {" + "    ?subject  ?property ?object . "
				+ "      }";
		Query query = QueryFactory.create(queryString);

		// // Execute the query and obtain results
		QueryExecution qe = QueryExecutionFactory.create(query, inputModel);
		ResultSet results = qe.execSelect();

		while (results.hasNext()) {
			QuerySolution querySolution = results.next();

			Resource subject = querySolution.get("subject").asResource();
			Resource property = querySolution.get("property").asResource();
			RDFNode object = querySolution.get("object");

			if (object == null | subject == null | property == null) {
				continue;
			}

			boolean rdfStatementIsOK = true;

			// it is nessecary to also map blank nodes

			// possibly change subject URI
			Resource adapterSubject = subject;
			if (subject.getURI() != null) {

				// change url of subject to url dereferenceable by
				// triplestore adapter
				String oldURI = subject.getURI();

				// uri may refer to a namespace prefix. If that is the case, the
				// uri will be resolved to be a full URI.
				oldURI = replaceNsPrefix(oldURI, inputModel);

				// if resource URI is specific to an oslc adapter,
				// change the url to be specific to the triplestore
				// adapter
				if (oldURI.contains("oslc4j")) {
					String newURI = oldURI.replaceAll("8.8./oslc4j.+/services",
							OSLC4JTDBApplication.portNumber + "/oslc4jtdb/services");
					int separatorIndex = newURI.lastIndexOf("services/");
					if (separatorIndex == -1) {
						// oslsc resource not correctly published! (Example:
						// http://localhost:8080/oslc4jmagicdraw/resourceShapes/package)
						continue;
					}
					String oldURIID1 = newURI.substring(0, separatorIndex + 9);
					String oldURIID2 = newURI.substring(separatorIndex + 9, newURI.length());
					System.out.println(oldURIID2);

					// replace slashes by dash
					String uriWithNewID = oldURIID1 + "default/resources/" + oldURIID2.replace("/", "-");
					System.out.println("updating resource uri");
					System.out.println(uriWithNewID);

					// old way: save new uri in map
					// resourcesToBeRenamed.put(subject.getURI(),
					// uriWithNewID);

					// renaming resource
					// Resource renamedResource =
					// ResourceUtils.renameResource(subject, uriWithNewID);
					Resource renamedResource = ResourceFactory.createResource(uriWithNewID);
					adapterSubject = renamedResource;

					// add to model for rdf serialization
					RDFNode originalSubjectURIObject = ResourceFactory.createResource(subject.getURI());
					adapterModel.add(renamedResource, originalURIProperty, originalSubjectURIObject);
				}
			} else {
				rdfStatementIsOK = false;
				continue;
			}

			// possibly change property URI

			// create new property
			String propertyURI = property.asResource().getURI();
			int propertyNameIndex = propertyURI.lastIndexOf("#");
			if (propertyNameIndex == -1) {
				// oslsc resource not correctly published! (Example:
				// propertyNameIndex = propertyURI.lastIndexOf("#");)
				propertyNameIndex = propertyURI.lastIndexOf("/");
			}
			String propertyName = propertyURI.substring(propertyNameIndex + 1, propertyURI.length());
			// Property propertyAsProperty =
			// ResourceFactory.createProperty(property.asResource().getURI(),
			// propertyName);
			Property propertyAsProperty = ResourceFactory.createProperty(property.asResource().getURI());
			Property adapterProperty = propertyAsProperty;

			if (property.getURI() != null) {
				String oldPropertyURI = property.getURI();

				// uri may refer to a namespace prefix. If that is the case, the
				// uri will be resolved to be a full URI.
				oldPropertyURI = replaceNsPrefix(oldPropertyURI, inputModel);

				// if resource URI is specific to an oslc adapter,
				// change the url to be specific to the triplestore
				// adapter
				if (oldPropertyURI.contains("oslc4j")) {

					System.out.println("updating property uri");
					String newPropertyURI = oldPropertyURI.replaceAll("8.8./oslc4j.+/services",
							OSLC4JTDBApplication.portNumber + "/oslc4jtdb/services");
					int propertySeparatorIndex = newPropertyURI.lastIndexOf("services/");
					String oldPropertyURIID1 = newPropertyURI.substring(0, propertySeparatorIndex + 9);
					String oldPropertyURIID2 = newPropertyURI.substring(propertySeparatorIndex + 9,
							newPropertyURI.length());
							// System.out.println(oldPropertyURIID2);

					// replace slashes by dash
					String propertyUriWithNewID = oldPropertyURIID1 + "default/resources/"
							+ oldPropertyURIID2.replace("/", "-");
					System.out.println(propertyUriWithNewID);

					// get property name
					int propertyNameIndex2 = propertyUriWithNewID.lastIndexOf("#");
					String propertyName2 = propertyUriWithNewID.substring(propertyNameIndex2 + 1,
							propertyUriWithNewID.length());

					// create new statement
					// Property newProperty =
					// ResourceFactory.createProperty(propertyUriWithNewID,
					// propertyName2);
					Property newProperty = ResourceFactory.createProperty(propertyUriWithNewID);
					adapterProperty = newProperty;

					// add to model for rdf serialization
					RDFNode originalPropertyURIObject = ResourceFactory.createResource(property.getURI());
					adapterModel.add(newProperty, originalURIProperty, originalPropertyURIObject);
				}
			} else {
				rdfStatementIsOK = false;
				continue;
			}

			// possibly change object URI
			RDFNode adapterObject = object;

			if (object.isResource()) {
				if (object.asResource().getURI() != null) {
					String oldObjectURI = object.asResource().getURI();

					// uri may refer to a namespace prefix. If that is the case,
					// the uri will be resolved to be a full URI.
					oldObjectURI = replaceNsPrefix(oldObjectURI, inputModel);

					if (oldObjectURI.contains("oslc4j")) {
						String newObjectURI = oldObjectURI.replaceAll("8.8./oslc4j.+/services",
								OSLC4JTDBApplication.portNumber + "/oslc4jtdb/services");
						int separatorIndex = newObjectURI.lastIndexOf("services/");
						String oldURIID1 = newObjectURI.substring(0, separatorIndex + 9);
						String oldURIID2 = newObjectURI.substring(separatorIndex + 9, newObjectURI.length());
						// System.out.println(oldURIID2);

						// replace slashes by dash
						String uriWithNewID = oldURIID1 + "default/resources/" + oldURIID2.replace("/", "-");
						System.out.println("updating object uri");
						System.out.println(uriWithNewID);

						// old way: save new uri in map
						// resourcesToBeRenamed.put(subject.getURI(),
						// uriWithNewID);

						// renaming resource
						// Resource renamedObject =
						// ResourceUtils.renameResource(object.asResource(),
						// uriWithNewID);
						Resource renamedObject = ResourceFactory.createResource(uriWithNewID);
						adapterObject = renamedObject;

						// add to model for rdf serialization
						RDFNode originalObjectURIObject = ResourceFactory.createResource(object.asResource().getURI());
						adapterModel.add(renamedObject, originalURIProperty, originalObjectURIObject);
					}
				} else {
					rdfStatementIsOK = false;
				}
			}

			// new way: save resources in separate triplestore
			if (rdfStatementIsOK) {
				adapterModel.add(adapterSubject, adapterProperty, adapterObject);
			}
		}

	}

	static String replaceNsPrefix(String uri, Model model) {
		for (String prefix : model.getNsPrefixMap().keySet()) {
			if (uri.contains(prefix + ":")) {
				return uri.replace(prefix + ":", model.getNsPrefixMap().get(prefix));
			}
		}
		return uri;
	}

	public static void addNamespacePrefix(Model resourceRDFModel) {
		for (String namespacePrefix : OSLC4JTDBApplication.namespacePrefixMap.keySet()) {
			resourceRDFModel.setNsPrefix(namespacePrefix, OSLC4JTDBApplication.namespacePrefixMap.get(namespacePrefix));
		}		
	}
}
