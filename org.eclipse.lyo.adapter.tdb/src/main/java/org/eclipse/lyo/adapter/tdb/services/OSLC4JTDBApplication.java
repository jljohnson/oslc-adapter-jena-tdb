/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *  
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * Contributors:
 *
 *     Michael Fiedler     - initial API and implementation for Bugzilla adapter
 *     
 *     Axel Reichwein	   - implementation for Simulink adapter (axel.reichwein@koneksys.com)
 *     
 *******************************************************************************/
package org.eclipse.lyo.adapter.tdb.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.apache.xerces.util.URI;

import org.eclipse.lyo.adapter.tdb.application.TDBManager;

import org.eclipse.lyo.adapter.tdb.services.RDFResourceService;

import org.eclipse.lyo.oslc4j.application.OslcResourceShapeResource;
import org.eclipse.lyo.oslc4j.application.OslcWinkApplication;
import org.eclipse.lyo.oslc4j.core.exception.OslcCoreApplicationException;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;

import org.eclipse.lyo.oslc4j.provider.jena.JenaProvidersRegistry;
import org.eclipse.lyo.oslc4j.provider.json4j.Json4JProvidersRegistry;

/**
 * OSLC4JSimulinkApplication registers all entity providers for converting POJOs
 * into RDF/XML, JSON and other formats. OSLC4JSimulinkApplication registers
 * also registers each servlet class containing the implementation of OSLC
 * RESTful web services.
 * 
 * OSLC4JSimulinkApplication also reads the user-defined configuration file with
 * loadPropertiesFile(). This is done at the initialization of the web
 * application, for example when the first resource or service of the OSLC
 * Simulink adapter is requested.
 * 
 * @author Axel Reichwein (axel.reichwein@koneksys.com)
 */
public class OSLC4JTDBApplication extends OslcWinkApplication {

	public static final Set<Class<?>> RESOURCE_CLASSES = new HashSet<Class<?>>();
	public static final Map<String, Class<?>> RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP = new HashMap<String, Class<?>>();

	public static String triplestoreDirectory = null;
	// public static String adapterTriplestoreDirectory = null;
	public static String portNumber = null;
	public static Map<String, String> namespacePrefixMap = null;

	public static int delayInSecondsBetweenDataRefresh = 100000;

	// public static String configFilePath =
	// "oslc4jtdb configuration/config.properties";
	// public static String configFilePath = "configuration/config.properties";
	// public static String configFilePath =
	// "C:/Users/Axel/Desktop/apache-tomcat-7.0.59/configuration/config.properties";
	public static String warConfigFilePath = "../oslc4jtdb configuration/config.properties";
	public static String localConfigFilePath = "oslc4jtdb configuration/config.properties";
	public static String configFilePath = null;

	public static String warNamespacePrefixConfigFilePath = "../oslc4jtdb configuration/namespaceprefixes.txt";
	public static String localNamespacePrefixConfigFilePath = "oslc4jtdb configuration/namespaceprefixes.txt";
	public static String configNamespacePrefixFilePath = null;

	static {
		RESOURCE_CLASSES.addAll(JenaProvidersRegistry.getProviders());
		RESOURCE_CLASSES.addAll(Json4JProvidersRegistry.getProviders());

		RESOURCE_CLASSES.add(ServiceProviderCatalogService.class);
		RESOURCE_CLASSES.add(ServiceProviderService.class);
		RESOURCE_CLASSES.add(RDFResourceService.class);

		RESOURCE_CLASSES.add(OslcResourceShapeResource.class);

		// RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP.put(Constants.PATH_RDF_RESOURCE,
		// RDFResource.class);

		loadPropertiesFile();

		readDataFirstTime();

		readDataPeriodically();

	}

	public OSLC4JTDBApplication() throws OslcCoreApplicationException, URISyntaxException {
		super(RESOURCE_CLASSES, OslcConstants.PATH_RESOURCE_SHAPES, RESOURCE_SHAPE_PATH_TO_RESOURCE_CLASS_MAP);
	}

	private static void loadPropertiesFile() {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			// loading properties file
			// input = new FileInputStream("./configuration/config.properties");
			input = new FileInputStream(warConfigFilePath); // for war file
			configFilePath = warConfigFilePath;
		} catch (FileNotFoundException e) {
			try {
				input = new FileInputStream(localConfigFilePath);
				configFilePath = localConfigFilePath;
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} // for war file
		}

		try {
			input = new FileInputStream(warNamespacePrefixConfigFilePath); // for
																			// war
																			// file
			configNamespacePrefixFilePath = warNamespacePrefixConfigFilePath;
		} catch (FileNotFoundException e) {
			try {
				input = new FileInputStream(localNamespacePrefixConfigFilePath);
				configNamespacePrefixFilePath = localNamespacePrefixConfigFilePath;
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} // for war file
		}

		// load property file content and convert backslashes into forward
		// slashes
		String str;
		if (input != null) {
			try {
				str = readFile(configFilePath, Charset.defaultCharset());
				prop.load(new StringReader(str.replace("\\", "/")));

				// get the property value

				String triplestoreDirectoryFromUser = prop.getProperty("triplestoreDirectory");
				// String adapterTriplestoreDirectoryFromUser =
				// prop.getProperty("adapterTriplestoreDirectory");

				String delayInSecondsBetweenDataRefreshFromUser = prop.getProperty("delayInSecondsBetweenDataRefresh");

				// add trailing slash if missing
				if (!triplestoreDirectoryFromUser.endsWith("/")) {
					triplestoreDirectoryFromUser = triplestoreDirectoryFromUser + "/";
				}
				triplestoreDirectory = triplestoreDirectoryFromUser;

				// if (!adapterTriplestoreDirectoryFromUser.endsWith("/")) {
				// adapterTriplestoreDirectoryFromUser =
				// adapterTriplestoreDirectoryFromUser + "/";
				// }
				// adapterTriplestoreDirectory =
				// adapterTriplestoreDirectoryFromUser;

				portNumber = prop.getProperty("portNumber");

				try {
					delayInSecondsBetweenDataRefresh = Integer.parseInt(delayInSecondsBetweenDataRefreshFromUser);
				} catch (Exception e) {

				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}

		// read predefined namespace prefix settings
		namespacePrefixMap = getNamespacePrefixes();		

	}

	private static TreeMap<String, String> getNamespacePrefixes() {
		final int lhs = 0;
		final int rhs = 1;

		TreeMap<String, String> map = new TreeMap<String, String>();
		BufferedReader bfr;
		try {
			bfr = new BufferedReader(new FileReader(new File(configNamespacePrefixFilePath)));
			String line;
			while ((line = bfr.readLine()) != null) {
				if (!line.startsWith("#") && !line.isEmpty()) {
					String[] pair = line.trim().split("=");
					try {
						java.net.URI uri = java.net.URI.create(pair[rhs].trim());
						map.put(pair[lhs].trim(), pair[rhs].trim());
			        } catch (Exception e1) {
			            System.err.println("Wrong namespace URI for prefix " + pair[lhs].trim());;
			        }
					
					
					
				}
			}
			bfr.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return map;

	}

	public static ArrayList<String> readSVNFileURLs(List<String[]> allElements) {
		List<String> svnurls = new ArrayList<String>();

		for (String[] element : allElements) {
			if ((element.length == 1)) {
				svnurls.add(element[0]);
			}
		}

		ArrayList<String> subversionFileURLs = new ArrayList<String>();
		// for (String subversionFileURL : SubversionFileURLsFromUserArray) {
		for (String subversionFileURL : svnurls) {
			// make sure to delete possible space character
			if (subversionFileURL.startsWith(" ")) {
				subversionFileURL = subversionFileURL.substring(1, subversionFileURL.length());
			}
			if (subversionFileURL.endsWith(" ")) {
				subversionFileURL = subversionFileURL.substring(0, subversionFileURL.length() - 1);
			}

			try {
				// make sure that URL is valid
				new URL(subversionFileURL);

				// make sure that url is not a duplicate
				if (!subversionFileURLs.contains(subversionFileURL)) {
					subversionFileURLs.add(subversionFileURL);
				}

			} catch (Exception e) {

			}
		}
		return subversionFileURLs;
	}

	static String readFile(String path, Charset encoding) throws IOException {
		byte[] encoded = Files.readAllBytes(Paths.get(path));
		return encoding.decode(ByteBuffer.wrap(encoded)).toString();
	}

	public static void readDataFirstTime() {
		Thread thread = new Thread() {
			public void start() {
				TDBManager.loadTDBContent();
			}
		};
		thread.start();
		try {
			thread.join();
			System.out.println("Triplestore content read.");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void readDataPeriodically() {
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				TDBManager.loadTDBContent();
			}
		}, delayInSecondsBetweenDataRefresh * 1000, delayInSecondsBetweenDataRefresh * 1000);
	}

}
