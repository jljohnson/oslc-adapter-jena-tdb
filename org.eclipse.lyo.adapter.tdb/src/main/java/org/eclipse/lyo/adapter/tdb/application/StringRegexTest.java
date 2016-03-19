package org.eclipse.lyo.adapter.tdb.application;

public class StringRegexTest {

	public static void main(String[] args) {
		
		// replace port number and web app name
		String oldURI = "http://localhost:8383/oslc4jintegrity/services/project102/requirements/179";		
//		String newURI = oldURI.replaceAll("8*8*/oslc4j", "8585/oslc4j");
		String newURI = oldURI.replaceAll("8.8./oslc4j.+/services", "8585/oslc4jtdb/services");
		System.out.println(newURI);
		
		// replace resource id
		// only if uri contains services
		if(oldURI.contains("/services/")){
			int separatorIndex = newURI.lastIndexOf("services/");
			String oldURIID1 = newURI.substring(0, separatorIndex);
			String oldURIID2 = newURI.substring(separatorIndex + 9, newURI.length() - 1);
			System.out.println(oldURIID2);
			
			// replace slashes by dash
			String uriWithNewID = oldURIID1 + oldURIID2.replace("/", "-");
			System.out.println(uriWithNewID);
		}
		
		

	}

}
