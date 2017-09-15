package com.koneksys.ld4mbse.rdfstore.services;

import java.net.URI;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;

/**
 * RDF manager.
 * @author rherrera
 */
public interface RDFManager {
    /**
     * Determines whether a named model exists into the dataset.
     * @param model the model identifier.
     * @return {@code true} if a model with the given uri exists into the
     * dataset; {@code false} otherwise.
     */
    boolean containsModel(URI model);
    /**
     * Stores a model under a given URI.
     * @param model the model to set.
     * @param uri the model identifier. If {@code null} then the default
     * model on de {@link Dataset} will be replaced, be careful..
     */
    void setModel(Model model, URI uri);
    /**
     * Retrieves a model under a given URI.
     * @param model the model identifier. If {@code null} then the default
     * model on de {@link Dataset} will be retrieved.
     * @return the stored model.
     */
    Model getModel(URI model);
    /**
     * Gets a resource on a given model.
     * @param uri the resource identifier.
     * @param model optional, the graph name to search into.
     * @return the associated model to the resource model.
     */
    Model getResource(URI uri, URI model);
}