import org.neo4j.plugin.xml.PluginUnitTest;

import java.io.IOException;

public class Neo4jRunner {

    public void run() {
        new Thread() {

            @Override
            public void run() {
                try {
                    PluginUnitTest pluginTest = new PluginUnitTest();
                    pluginTest.initServer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public static void main(String[] args) {
        new Neo4jRunner().run();
    }
}
