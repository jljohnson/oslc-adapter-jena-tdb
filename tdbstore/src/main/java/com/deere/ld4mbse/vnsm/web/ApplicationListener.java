package com.deere.ld4mbse.vnsm.web;

import com.deere.ld4mbse.vnsm.services.DatasetProducer;
import java.io.File;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.jena.query.Dataset;
import org.apache.jena.tdb.TDBFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application verifier and resources releaser.
 * @author rherrera
 */
public class ApplicationListener implements ServletContextListener {
    /**
     * Logger of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ApplicationListener.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String tdbLocation = context.getInitParameter("TDB_LOCATION");
        File tdbDirectory = new File(tdbLocation);
        if (tdbDirectory.exists()) {
            if (!tdbDirectory.isDirectory()) {
                LOG.error(tdbLocation = tdbLocation+" is not a directory");
                throw new IllegalStateException(tdbLocation);
            }
            if (!tdbDirectory.canRead()) {
                LOG.error(tdbLocation = "Cannot read on " + tdbLocation);
                throw new IllegalStateException(tdbLocation);
            }
            if (!tdbDirectory.canWrite()) {
                LOG.error(tdbLocation = "Cannot write on " + tdbLocation);
                throw new IllegalStateException(tdbLocation);
            }
            LOG.info("{} TDB directory ready", tdbDirectory);
        } else if (tdbDirectory.mkdirs()) {
            LOG.info("{} TDB directory created", tdbDirectory);
        } else {
            LOG.error(tdbLocation = tdbLocation+" directory cannot be created");
            throw new IllegalStateException(tdbLocation);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Dataset dataset;
        DatasetProducer.Factory factory;
        DatasetProducer producer = new DatasetProducer();
        try {
            factory = producer.getFactory();
            dataset = factory.getDataset();
            TDBFactory.release(dataset);
            LOG.info("{} Dataset directory released", factory.getLocation());
        } catch(NamingException ex) {
            LOG.error("Could not release Dataset directory", ex);
        }
    }

}