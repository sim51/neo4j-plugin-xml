package org.neo4j.plugin.xml;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.neo4j.test.server.HTTP;

import java.io.IOException;
import java.net.URLEncoder;

public class PluginExtensionTest extends PluginUnitTest {

    @Before
    public void prepare() throws IOException {
        initServer();
    }

    @Test
    public void ping_should_work() throws Exception {
        check("/xml/ping", "GET", null, 200, "text/plain", "Pong");
    }

    @Test
    public void cypher_should_work() throws Exception {
        check("/xml/cypher", "POST", "query=MATCH (n) RETURN n LIMIT 10", 200, "application/xml", "Benoit");
    }

    @After
    public void destroy() {
        destroyServer();
    }

    /**
     * Make all needed test on a request.
     *
     * @param uri Uri to test
     * @param method GET or POST
     * @param body Body of the request
     * @param code Return code that should be there
     * @param contentType ContentType that should be there
     * @param content Part of content that should be there
     * @return response of the request
     */
    private  HTTP.Response check(String uri, String method, String body, int code, String contentType, String content) {
        if(uri.startsWith("/")) {
            uri = uri.replaceFirst("/", "");
        }

        // When I access a none existing file
        HTTP.Builder builder =  HTTP.withBaseUri(server.httpURI().toString())
                .withHeaders("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");

        HTTP.Response response;
        if(method.equalsIgnoreCase("GET")){
            response = builder.GET(uri);
        }
        else {
            response = builder.POST(uri, HTTP.RawPayload.rawPayload(body));
        }

        // Then it should reply ok with good  type & content
        if(code > 0)
            Assert.assertEquals(code, response.status());
        if(contentType != null)
            Assert.assertEquals(contentType, response.header("content-type"));
        if(content != null)
            Assert.assertTrue(response.rawContent().contains(content));

        return response;
    }
}
