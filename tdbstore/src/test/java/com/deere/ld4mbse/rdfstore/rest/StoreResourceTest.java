package com.deere.ld4mbse.rdfstore.rest;

import com.deere.ld4mbse.rdfstore.rest.StoreResource;
import com.deere.ld4mbse.rdfstore.services.RDFManager;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.WebContent;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.RDF;
import org.jboss.resteasy.core.Dispatcher;
import org.junit.Before;
import org.junit.Test;
import org.jboss.resteasy.mock.*;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import static org.junit.Assert.*;
import org.mockito.ArgumentMatchers;
import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Tests for {@link RDFManagerServlet}.
 * @author rherrera
 */
public class StoreResourceTest {

    private RDFManager manager;
    private Dispatcher dispatcher;
    private MockHttpRequest request;
    private MockHttpResponse response;
    private HttpServletRequest servletRequest;
    private HttpServletResponse servletResponse;
    /**
     * Convenient method to convert a String into a ServletInputStream.
     * @param content the content string to convert.
     * @return the corresponding input stream of {@code content}.
     * @throws IOException if some I/O exception occurs.
     */
    private ServletInputStream getServletInputStream(String content)
            throws IOException {
        final InputStream bytes = new ByteArrayInputStream(content.getBytes());
        ServletInputStream mock = mock(ServletInputStream.class);
        when(mock.read(ArgumentMatchers.<byte[]>any(), anyInt(), anyInt()))
                .thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocationOnMock)
                    throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                return bytes.read((byte[])args[0], (int)args[1], (int)args[2]);
            }
        });
        when(mock.read()).thenAnswer(new Answer<Integer>() {
            @Override
            public Integer answer(InvocationOnMock invocationOnMock)
                    throws Throwable {
                return bytes.read();
            }
        });
        return mock;
    }
    /**
     * Convenient method to convert a String into a ServletInputStream.
     * @param content the content string to convert.
     * @return the corresponding input stream of {@code content}.
     * @throws IOException if some I/O exception occurs.
     */
    private ServletOutputStream getServletOutputStream(
            final ByteArrayOutputStream os) throws IOException {
        ServletOutputStream mock = mock(ServletOutputStream.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock)
                    throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                os.write((byte[])args[0], (int)args[1], (int)args[2]);
                return null;
            }
        }).when(mock).write(ArgumentMatchers.<byte[]>any(), anyInt(), anyInt());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock)
                    throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                os.write((byte[])args[0]);
                return null;
            }
        }).when(mock).write(ArgumentMatchers.<byte[]>any());
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock)
                    throws Throwable {
                Object[] args = invocationOnMock.getArguments();
                os.write((int)args[0]);
                return null;
            }
        }).when(mock).write(anyInt());
        return mock;
    }
    /**
     * Convenient method to create a sample Apache Jena model.
     * @return the model.
     */
    private Model getModel() {
        Model model = ModelFactory.createDefaultModel();
        String me = "http://example.org/me";
        model.add(ResourceFactory.createResource(me), RDF.type, FOAF.Person);
        return model;
    }
    /**
     * Convenient method to create a sample Apache Jena model serialization.
     * @param lang the desired output {@link Lang language}.
     * @return the model serialization.
     */
    private String getModelSerialization(Lang language) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RDFDataMgr.write(baos, getModel(), language);
        return baos.toString();
    }

    @Before
    public void init() {
        StoreResource resource;
        Map<Class<?>, Object> ctx;
        manager = mock(RDFManager.class);
        resource = new StoreResource(manager);
        ctx = ResteasyProviderFactory.getContextDataMap();
        dispatcher = MockDispatcherFactory.createDispatcher();
        dispatcher.getRegistry().addSingletonResource(resource);
        servletRequest = mock(HttpServletRequest.class);
        servletResponse = mock(HttpServletResponse.class);
        response = new MockHttpResponse();
        ctx.put(HttpServletRequest.class, servletRequest);
        ctx.put(HttpServletResponse.class, servletResponse);
    }

    @Test
    public void testSet_MissingContentType() throws URISyntaxException {
        request = MockHttpRequest.post(StoreResource.PATH);
        dispatcher.invoke(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Missing Content-Type", response.getContentAsString());
    }

    @Test
    public void testSet_MissingSlugHeader() throws URISyntaxException {
        when(servletRequest.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        request = MockHttpRequest.post(StoreResource.PATH);
        dispatcher.invoke(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertEquals("Missing Slug header", response.getContentAsString());
    }

    @Test
    public void testSet_InvalidContentType() throws URISyntaxException {
        String responseBody = "Invalid MediaType. Use ";
        when(servletRequest.getContentType()).thenReturn(MediaType.TEXT_PLAIN);
        when(servletRequest.getHeader("Slug")).thenReturn("myLoad");
        request = MockHttpRequest.post(StoreResource.PATH);
        dispatcher.invoke(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        assertTrue(response.getContentAsString().startsWith(responseBody));
    }

    @Test
    public void testSet_InvalidRDFSyntax()
            throws URISyntaxException, IOException {
        String responseBody = "Invalid syntax: ";
        ServletInputStream inputStream = getServletInputStream("Hello World");
        when(servletRequest.getContentType()).thenReturn(WebContent.contentTypeTurtle);
        when(servletRequest.getHeader("Slug")).thenReturn("myLoad");
        when(servletRequest.getInputStream()).thenReturn(inputStream);
        request = MockHttpRequest.post(StoreResource.PATH);
        dispatcher.invoke(request, response);
        assertEquals(HttpServletResponse.SC_BAD_REQUEST, response.getStatus());
        System.out.println(response.getContentAsString());
        assertTrue(response.getContentAsString().startsWith(responseBody));
    }

    private void testSet(String mediaType, Lang language)
            throws URISyntaxException, IOException {
        String location, slug = "myLoad";
        String serialization = getModelSerialization(language);
        ServletInputStream inputStream = getServletInputStream(serialization);
        when(servletRequest.getHeader("Slug")).thenReturn(slug);
        when(servletRequest.getContentType()).thenReturn(mediaType);
        when(servletRequest.getInputStream()).thenReturn(inputStream);
        request = MockHttpRequest.post(StoreResource.PATH);
        dispatcher.invoke(request, response);
        location = response.getOutputHeaders().getFirst("Location").toString();
        assertEquals(HttpServletResponse.SC_CREATED, response.getStatus());
        assertEquals("/" + StoreResource.PATH + "/" + slug, location);
    }

    @Test
    public void testSet_turle() throws URISyntaxException, IOException {
        testSet(WebContent.contentTypeTurtle, Lang.TURTLE);
    }

    @Test
    public void testSet_jsonld() throws URISyntaxException, IOException {
        testSet(WebContent.contentTypeJSONLD, Lang.JSONLD);
    }

    @Test
    public void testSet_rdfxml() throws URISyntaxException, IOException {
        testSet(WebContent.contentTypeRDFXML, Lang.RDFXML);
    }

    @Test
    public void testGet_MissingModel() throws URISyntaxException {
        request = MockHttpRequest.get(StoreResource.PATH + "/myLoad");
        dispatcher.invoke(request, response);
        assertEquals(HttpServletResponse.SC_NOT_FOUND, response.getStatus());
    }

    @Test
    public void testGet_EmptyModel() throws URISyntaxException {
        Model model = ModelFactory.createDefaultModel();
        when(manager.getModel(ArgumentMatchers.<URI>any())).thenReturn(model);
        request = MockHttpRequest.get(StoreResource.PATH + "/myLoad");
        dispatcher.invoke(request, response);
        assertEquals(HttpServletResponse.SC_NO_CONTENT, response.getStatus());
    }

    private void testGet(String mediaType, Lang language)
            throws URISyntaxException, IOException {
        Model model = getModel();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ServletOutputStream sos = getServletOutputStream(output);
        when(manager.getModel(ArgumentMatchers.<URI>any())).thenReturn(model);
        when(servletRequest.getHeader(HttpHeaders.ACCEPT)).thenReturn(mediaType);
        when(servletResponse.getOutputStream()).thenReturn(sos);
        request = MockHttpRequest.get(StoreResource.PATH + "/myLoad");
        request.accept(mediaType);
        dispatcher.invoke(request, response);
        assertEquals(getModelSerialization(language), output.toString("UTF8"));
    }

    @Test
    public void testGet_turtle() throws URISyntaxException, IOException {
        testGet(WebContent.contentTypeTurtle, Lang.TURTLE);
    }

    @Test
    public void testGet_jsonld() throws URISyntaxException, IOException {
        testGet(WebContent.contentTypeJSONLD, Lang.JSONLD);
    }

    @Test
    public void testGet_rdfxml() throws URISyntaxException, IOException {
        testGet(WebContent.contentTypeRDFXML, Lang.RDFXML);
    }

}