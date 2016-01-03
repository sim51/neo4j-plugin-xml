package org.neo4j.plugin.xml;

import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.neo4j.harness.ServerControls;
import org.neo4j.harness.TestServerBuilders;

import java.io.IOException;

public class PluginUnitTest {

    /**
     * Graph database server.
     */
    protected ServerControls server;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    /**
     * Create an in memory graph database server.
     */
    public void initServer() throws IOException {
        this.server =TestServerBuilders
                .newInProcessBuilder()
                .withFixture("CREATE (:Person {name:'Benoit'})")
                .withExtension("/xml", PluginExtension.class)
                .newServer();
    }

    /**
     * Destroy the in memory graph database server.
     */
    protected void destroyServer() {
        this.server.close();
    }

}
