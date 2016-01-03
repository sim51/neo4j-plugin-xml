package org.neo4j.plugin.xml;

import org.apache.commons.lang.StringUtils;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.PropertyContainer;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

@Path("/")
public class PluginExtension {

    /**
     * The logger
     */
    private static final Logger log = LoggerFactory.getLogger(PluginExtension.class);

    /**
     * Graph database instance.
     */
    private final GraphDatabaseService database;

    /**
     * Constructor.
     *
     * @param database a {@link org.neo4j.graphdb.GraphDatabaseService} object.
     */
    public PluginExtension(@Context GraphDatabaseService database) {
        this.database = database;
        log.info("Initialize Plugin extension XML");
    }

    @GET
    @Path("/ping")
    public Response ping() throws IOException {
        return Response.ok("Pong", MediaType.TEXT_PLAIN).build();
    }

    @POST
    @Path("/cypher")
    public Response cypher(@FormParam("query") String query) throws IOException {
        String xml = "";
        if (!StringUtils.isEmpty(query)) {
            try (Transaction tx = database.beginTx()) {
                Result rs = database.execute(query);
                xml = this.resultToXml(rs);
                tx.success();
            }
        }
        log.debug("Cypher query is {} with result {}", query, xml);
        return Response.ok(xml, MediaType.APPLICATION_XML_TYPE).build();
    }

    /**
     * Convert a resultSet to an XML string.
     *
     * @param rs
     * @return
     */
    private String resultToXml(Result rs) {
        StringBuffer result = new StringBuffer();
        result.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
        result.append("<result>");
        if (rs != null) {
            while (rs.hasNext()) {
                result.append("<row>");
                Map<String, Object> row = rs.next();
                for (String col : rs.columns()) {
                    if (row.containsKey(col)) {
                        result.append("<").append(col).append(">");
                        result.append(this.neo4jObjectToXml(row.get(col)));
                        result.append("</").append(col).append(">");
                    } else {
                        result.append("<").append(col).append("/>");
                    }
                }
                result.append("</row>");
            }
        }
        result.append("</result>");
        return result.toString();
    }

    /**
     * Transform a node or relation to an XML Object.
     *
     * @param obj
     * @return
     */
    private String neo4jObjectToXml(Object obj) {
        StringBuffer xml = new StringBuffer();

        // Result is an Object
        if (obj instanceof PropertyContainer) {
            PropertyContainer container = (PropertyContainer) obj;
            for(Map.Entry<String, Object> entry :  container.getAllProperties().entrySet()) {
                xml.append("<").append(entry.getKey()).append(">");
                // Result item is an array
                if(entry.getValue().getClass().isArray()) {
                    Object[] objects = (Object[]) entry.getValue();
                    for(Object object : objects) {
                        xml.append("<item>");
                        xml.append(this.escapeXMLChar(object));
                        xml.append("</item>");
                    }
                }
                // Result item is just a property
                else {
                    xml.append(this.escapeXMLChar(entry.getValue()));
                }
                xml.append("</").append(entry.getKey()).append(">");
            }
        }
        // Result is just a property
        else {
            xml.append(this.escapeXMLChar(obj));
        }
        return xml.toString();
    }

    private Object escapeXMLChar(Object obj) {
        if(obj instanceof String) {
            String text = (String) obj;
            return text
                    .replaceAll("\"", "&quot;")
                    .replaceAll("\'", "&apos;")
                    .replaceAll("<", "&lt;")
                    .replaceAll(">", "&gt;")
                    .replaceAll("&", "&amp;");
        }
        else {
            return obj;
        }
    }

}
