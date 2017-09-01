package com.koneksys.ld4mbse.rdfstore.rest;

import com.koneksys.ld4mbse.rdfstore.services.RDFManager;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFLanguages;
import org.apache.jena.riot.RDFWriterRegistry;
import org.apache.jena.riot.RiotException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages generic RDF operations.
 * @author rherrera
 */
@Path(GraphResource.PATH)
public class GraphResource {
    /**
     * Logger of this class.
     */
    private static final Logger LOG;
    /**
     * A MIME types of supported RDF languages map.
     */
    private static final Map<String, Lang> MIME_RDF_TYPES;
    /**
     * The path part of this resource.
     */
    public static final String PATH = "graph";
    /**
     * Static initialization.
     */
    static {
        RDFFormat serialization;
        MIME_RDF_TYPES = new HashMap<>();
        List<Lang> all = new ArrayList<>(RDFLanguages.getRegisteredLanguages());
        for (Lang language : all) {
            serialization = RDFWriterRegistry.defaultSerialization(language);
            if (serialization != null && !language.getLabel().contains("null")) {
                MIME_RDF_TYPES.put(language.getContentType().toHeaderString(), language);
            }
        }
        LOG = LoggerFactory.getLogger(GraphResource.class);
    }
    /**
     * The {@link RDFManager}.
     */
    @Inject
    private RDFManager manager;
    /**
     * Constructs an instance specifing the {@link RDFManager}.
     * This constructor is required for testing purposes.
     */
    GraphResource(RDFManager manager) {
        this.manager = manager;
    }
    /**
     * Constructs a default instance.
     */
    public GraphResource() {
        super();
    }
    /**
     * Builds a BAD_REQUEST response.
     * @param message the content message.
     * @return the corresponding message builder.
     */
    private Response.ResponseBuilder badRequest(String message) {
        Response.ResponseBuilder builder;
        builder = Response.status(Response.Status.BAD_REQUEST);
        builder = builder.type(MediaType.TEXT_PLAIN);
        builder = builder.entity(message);
        return builder;
    }
    /**
     * Parses the request's input stream into a model.
     * @param content request's content stream.
     * @param type request's content type.
     * @return the parsed RDF model.
     */
    private Model getModel(InputStream content, String type)
            throws URISyntaxException {
        URI base = new URI(GraphResource.PATH);
        Lang language = MIME_RDF_TYPES.get(type);
        Model model = ModelFactory.createDefaultModel();
        RDFDataMgr.read(model, content, base.toString(), language);
        return model;
    }
    /**
     * Sets RDF from remote clients.
     * @param request the http request.
     * @return the http response.
     */
    @POST
    @SuppressWarnings("UseSpecificCatch")
    public Response set(@Context HttpServletRequest request) {
        URI load;
        Model model;
        String contentType, slug;
        Response.ResponseBuilder builder;
        Set<String> allowedTypes = MIME_RDF_TYPES.keySet();
        if ((contentType = request.getContentType()) == null)
            builder = badRequest("Missing Content-Type");
        else if ((slug = request.getHeader("Slug")) == null)
            builder = badRequest("Missing Slug header");
        else  {
            contentType = contentType.split(";")[0].trim();
            if (!allowedTypes.contains(contentType)) {
                builder = badRequest("Invalid MediaType. Use " + allowedTypes);
            } else {
                try {
                    load = new URI(GraphResource.PATH + "/" + slug);
                    model = getModel(request.getInputStream(), contentType);
                    manager.setModel(model, load);
                    builder = Response.created(load);
                } catch (RiotException ex) {
                    builder = badRequest("Invalid syntax: " + ex.getMessage());
                } catch (Exception ex) {
                    builder = Response.serverError().entity("Unexpected error");
                    LOG.error("Could not load RDF", ex);
                }
            }
        }
        return builder.build();
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
     * @param graph the name of the graph to retrieve.
     * @return the http response.
     */
    @GET
    @Path("{graph}")
    @SuppressWarnings("UseSpecificCatch")
    public Response get(@Context HttpServletRequest request,
            @Context HttpServletResponse response,
            @PathParam("graph") String graph){
        Model model;
        try {
            model = manager.getModel(new URI(GraphResource.PATH + "/" + graph));
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