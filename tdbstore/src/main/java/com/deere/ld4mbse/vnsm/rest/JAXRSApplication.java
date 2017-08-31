package com.deere.ld4mbse.vnsm.rest;

import com.deere.ld4mbse.vnsm.model.Environment;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * This REST application definition.
 * @author rherrera
 */
@ApplicationPath(Environment.SERVICES_PATH)
public class JAXRSApplication extends Application {

}