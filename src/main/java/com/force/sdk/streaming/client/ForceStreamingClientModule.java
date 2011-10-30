package com.force.sdk.streaming.client;

import com.force.sdk.connector.ForceConnectionProperty;
import com.force.sdk.connector.ForceConnectorUtils;
import com.force.sdk.connector.ForceServiceConnector;
import com.force.sdk.streaming.exception.ForceStreamingException;
import com.force.sdk.streaming.util.ForceStreamingResource;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.sforce.ws.ConnectionException;
import org.eclipse.jetty.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * @author naamannewbold
 */
public class ForceStreamingClientModule extends AbstractModule {

    String connectionName;
    String persistenceUnit;
    int timeoutOption;

    private static final Logger LOGGER = LoggerFactory.getLogger(ForceStreamingClientModule.class);

    public ForceStreamingClientModule() {
        this(Defaults.CONNECTION_NAME.getValue(), Integer.valueOf(Defaults.TIMEOUT.getValue()));
    }

    public ForceStreamingClientModule(String connectionName, int timeoutOption) {
        this(connectionName, Defaults.PERSISTENCE_UNIT.getValue(), timeoutOption);
    }

    public ForceStreamingClientModule(String connectionName, String persistenceUnitName, int timeoutOption) {
        this.connectionName = connectionName;
        this.persistenceUnit = persistenceUnitName;
        this.timeoutOption = timeoutOption;
    }

    @Override
    protected void configure() {
        bind(PushTopicManager.class);
    }

    @Provides
    EntityManager provideEntityManager() {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory(persistenceUnit);
        return emf.createEntityManager();
    }

    @Provides
    ForceServiceConnector provideConnector() throws IOException, ConnectionException {
        ForceServiceConnector connector = new ForceServiceConnector(connectionName);
        return connector;
    }

    // TODO: client.start() could potentially have some side effects.
    // Need to understand more of what's going on under the hood.
    @Provides
    HttpClient provideHttpClient() {
        LOGGER.info("Providing http client for connection name " + connectionName);

        HttpClient client = new HttpClient();
        client.setConnectorType(HttpClient.CONNECTOR_SELECT_CHANNEL);
        client.setConnectTimeout(timeoutOption);
        client.setTimeout(timeoutOption);

        return client;
    }

    @Provides
    String provideBaseUrl() throws ForceStreamingException, IOException, ConnectionException {
        ForceServiceConnector connector = provideConnector();
        URL endpoint = new URL(connector.getConnection().getConfig().getServiceEndpoint());

        String baseUrl = ForceStreamingResource.PROTOCOL.getValue() + "://" + endpoint.getHost();
        baseUrl += ForceStreamingResource.RESOURCE_ENDPOINT.getValue();
        LOGGER.debug("Providing base URL: " + baseUrl);
        return baseUrl;
    }
}
