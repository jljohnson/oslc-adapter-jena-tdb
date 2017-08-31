package com.deere.ld4mbse.vnsm.rest;

import com.deere.ld4mbse.vnsm.services.RDFManager;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFLanguages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A generic resource getter.
 * @author rherrera
 */
@Path("/")
public class EntityResource {
    /**
     * Logger of this class.
     */
    private static final Logger LOG = LoggerFactory.getLogger(EntityResource.class);;
    /**
     * The {@link RDFManager}.
     */
    @Inject
    private RDFManager manager;
    /**
     * Constructs an instance specifing the {@link RDFManager}.
     * This constructor is required for testing purposes.
     */
    EntityResource(RDFManager manager) {
        this.manager = manager;
    }
    /**
     * Constructs a default instance.
     */
    public EntityResource() {
        super();
    }
    /**
     * Writes a model on the http response.
     * @param model the model to write.
     * @param response the response to write into.
     * @param accept the Accept request header value (the desired language).
     * @return null.
     * @throws IOException if some I/O exception occurs.
     */
    private Response write(Model model, HttpServletResponse response,
            String accept) throws IOException {
        try (OutputStream output = response.getOutputStream()) {
            Lang language = RDFLanguages.contentTypeToLang(accept);
            if (language == null) {
                LOG.warn("{} was not recognized, using TTL", accept);
                language = Lang.TURTLE;
            }
            response.setContentType(language.getContentType().toString());
            response.setStatus(HttpServletResponse.SC_OK);
            RDFDataMgr.write(output, model, language);
            output.flush();
        }
        return null;
    }
    /**
     * Gets RDF from store in a given language.
     * @param request the http request object.
     * @param response the http response object.
     * @param type the type of the resource to retrieve.
     * @param id the id of the resource to retrieve.
     * @return the http response.
     */
    @GET
    @Path("{type}/{id}")
    @SuppressWarnings("UseSpecificCatch")
    public Response get(@Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @PathParam("type") String type, @PathParam("id") String id){
        Model model;
        String resource;
        URI store = null;
        try {
            resource = request.getParameter(StoreResource.PATH);
            if (resource != null)
                store = new URI(StoreResource.PATH + "/" + resource);
            resource = request.getRequestURL().toString();
            model = manager.getResource(new URI(resource), store);
            if (model == null)
                return Response.status(Response.Status.NOT_FOUND).build();
            else if (model.isEmpty())
                return Response.noContent().build();
            else
                return write(model, response, request.getHeader(HttpHeaders.ACCEPT));
        } catch (Exception ex) {
            LOG.error("Could not retrieve RDF", ex);
            return Response.serverError().entity("Unexpected error").build();
        }
    }

}