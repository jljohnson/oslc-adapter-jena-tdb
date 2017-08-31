package com.deere.ld4mbse.rdfstore.services;

import java.net.URI;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.ReadWrite;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.shared.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TDB implementation for {@link RDFManager}.
 * @author rherrera
 */
@ApplicationScoped
public class TDBManager implements RDFManager {
    /**
     * Logger of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(TDBManager.class);

    @Inject
    private Dataset dataset;
    /**
     * Constructs an instance specifing the {@link RDFManager}.
     * This constructor is required for testing purposes.
     */
    TDBManager(Dataset dataset) {
        this.dataset = dataset;
    }
    /**
     * Constructs a default instance.
     */
    public TDBManager() {
        super();
    }

    @Override
    public void setModel(Model model, URI uri) {
        Lock lock;
        LOG.debug("> + model @ {}", uri);
        dataset.begin(ReadWrite.WRITE);
        lock = dataset.getLock();
        try {
            lock.enterCriticalSection(false);
            if (uri == null)
                dataset.setDefaultModel(model);
            else
                dataset.replaceNamedModel(uri.toString(), model);
            dataset.commit();
            LOG.debug("< +{} statements", model.size());
        } finally {
            dataset.end();
            lock.leaveCriticalSection();
        }
    }

    @Override
    public Model getModel(URI uri) {
        Lock lock;
        Model buffer, model;
        LOG.debug("> ? model @ {}", uri);
        dataset.begin(ReadWrite.READ);
        lock = dataset.getLock();
        try {
            lock.enterCriticalSection(true);
            buffer = ModelFactory.createDefaultModel();
            if (uri == null)
                model = dataset.getDefaultModel();
            else
                model = dataset.getNamedModel(uri.toString());
            buffer.add(model);
            dataset.commit();
            LOG.debug("< {} statements", buffer.size());
        } finally {
            dataset.end();
            lock.leaveCriticalSection();
        }
        return buffer;
    }

    @Override
    public Model getResource(URI uri, URI model) {
        Lock lock;
        Resource finding;
        Model buffer, source;
        LOG.debug("> ? {} @ {}", uri, model);
        dataset.begin(ReadWrite.READ);
        lock = dataset.getLock();
        try {
            lock.enterCriticalSection(true);
            buffer = ModelFactory.createDefaultModel();
            if (model == null)
                source = dataset.getDefaultModel();
            else
                source = dataset.getNamedModel(uri.toString());
            finding = ResourceFactory.createResource(uri.toString());
            buffer.add(source.query(new SimpleSelector(finding, null, (String)null)));
            dataset.commit();
            LOG.debug("< {} statements", buffer.size());
        } finally {
            dataset.end();
            lock.leaveCriticalSection();
        }
        return buffer;
    }

}