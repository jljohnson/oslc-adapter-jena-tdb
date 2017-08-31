package com.deere.ld4mbse.vnsm.model;

/**
 * Environment properties.
 * @author rherrera
 */
public interface Environment {
    /**
     * The naming of the TDB factory resource.
     */
    String TDB_NAMING_FACTORY = "${tdb.naming.factory}";
    /**
     * The relative path for JAX-RS services.
     */
    String SERVICES_PATH = "${path.services}";
}