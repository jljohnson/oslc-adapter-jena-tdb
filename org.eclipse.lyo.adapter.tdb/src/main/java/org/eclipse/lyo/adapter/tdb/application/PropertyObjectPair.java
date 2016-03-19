package org.eclipse.lyo.adapter.tdb.application;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class PropertyObjectPair {
	
	Property property;
	RDFNode object;
	public Property getProperty() {
		return property;
	}
	public PropertyObjectPair(Property property, RDFNode object) {
		super();
		this.property = property;
		this.object = object;
	}
	public void setProperty(Property property) {
		this.property = property;
	}
	public RDFNode getObject() {
		return object;
	}
	public void setObject(RDFNode object) {
		this.object = object;
	}

}
