package com.koneksys.ld4mbse.rdfstore.services;

import com.koneksys.ld4mbse.rdfstore.model.Environment;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Dataset} {@link Produces producer}.
 * @author rherrera
 */
@ApplicationScoped
public class DatasetProducer {
    /**
     * Logger of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(DatasetProducer.class);
    /**
     * {@code Dataset} factory. To get a {@code Dataset} instance call the
     * {@link #setLocation(java.lang.String)} method first, and then retrieve
     * the reference via {@link #getDataset()} method.
     * <p>Tomcat uses this class to make a singleton resource available while
     * sets the TDB location, check the {@code META-INF/context.xml} file.</p>
     */
    public static class Factory {
        private String location;
        private Dataset dataset;
        /**
         * Gets the TDB location directory.
         * @return the TDB location directory.
         */
        public String getLocation() {
            return location;
        }
        /**
         * Sets the TDB location directory and creates the {@code Dataset}.
         * @param location the TDB location directory.
         */
        public void setLocation(String location) {
            this.location = location;
            dataset = TDBFactory.createDataset(location);
            LOG.info("Dataset created on {}", location);
        }
        /**
         * Gets the underlying {@code Dataset} of this factory.
         * @return {@code null} if {@link #setLocation(java.lang.String)} has
         * not been called, the {@code Dataset} reference otherwise.
         */
        public Dataset getDataset() {
            return dataset;
        }
    }
    /**
     * Gets the underlying factory of this producer.
     * @return the underlying factory of this producer.
     * @throws NamingException if a naming exception is encountered.
     */
    public Factory getFactory() throws NamingException {
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup("java:comp/env");
        return (Factory)envCtx.lookup(Environment.TDB_NAMING_FACTORY);
    }
    /**
     * Gets THE {@link Dataset} of this application for injection points.
     * @return the singleton {@code Dataset} of this application.
     * @throws NamingException if a naming exception is encountered.
     */
    @Produces
    public Dataset getInstace() throws NamingException {
        Factory singleton = getFactory();
        return singleton.getDataset();
    }

}