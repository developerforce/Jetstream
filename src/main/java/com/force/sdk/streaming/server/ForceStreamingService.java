package com.force.sdk.streaming.server;

import com.force.sdk.streaming.client.Defaults;
import com.force.sdk.streaming.client.ForceBayeuxClient;
import com.force.sdk.streaming.client.ForceStreamingClientModule;
import com.force.sdk.streaming.client.PushTopicManager;
import com.force.sdk.streaming.exception.ForceStreamingException;
import com.force.sdk.streaming.model.PushTopic;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.cometd.bayeux.ChannelId;
import org.cometd.bayeux.Message;
import org.cometd.bayeux.client.ClientSessionChannel;
import org.cometd.bayeux.server.BayeuxServer;
import org.cometd.bayeux.server.ConfigurableServerChannel;
import org.cometd.bayeux.server.ServerChannel;
import org.cometd.bayeux.server.ServerSession;
import org.cometd.server.AbstractService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletConfig;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author naamannewbold
 */
// TODO: use annotations instead of extending
// http://cometd.org/documentation/2.x/cometd-java/server/services/annotated
public class ForceStreamingService extends AbstractService {

    ForceBayeuxClient client;
    PushTopicManager pushTopicManager;

    private static final Logger LOGGER = LoggerFactory.getLogger(ForceStreamingService.class);

    public ForceStreamingService(BayeuxServer bayeuxServer, ServletConfig config) {
        super(bayeuxServer, "force");
        LOGGER.info("Creating bayeux service at /service/force");
        Injector injector = Guice.createInjector(new ForceStreamingClientModule(
                getParam(config, Defaults.CONNECTION_NAME)
              , getParam(config, Defaults.PERSISTENCE_UNIT)
              , Integer.valueOf(getParam(config, Defaults.TIMEOUT))
        ));
        client = injector.getInstance(ForceBayeuxClient.class);

        pushTopicManager = injector.getInstance(PushTopicManager.class);
//        bayeuxServer.addExtension(new ForceStreamingServiceExtension());
        bayeuxServer.addListener(new ForceStreamingServiceListener(client));

        addService("/service/force", "processForce");
    }

    private String getParam(ServletConfig config, Defaults defaults) {
        String param = config.getInitParameter(defaults.name());
        if (param == null)
            param = defaults.getValue();
        return param;
    }

    public void processForce(final ServerSession remote, final Message message) throws InterruptedException {
        Map<String, Object> input = message.getDataAsMap();
        String name = (String)input.get("name");
        System.out.println("Ignoring " + name + " service request");
//        try {
//            validateName(name);
//            // TODO: figure out if a query is necessary for validation here
////            PushTopic topic = pushTopicManager.getTopicByName(name);
//            LOGGER.info("Processing " + name + " topic");
//            PushTopic topic = new PushTopic();
//            topic.setName(name);
//            client.subscribeTo(topic, new ClientSessionChannel.MessageListener() {
//                public void onMessage(ClientSessionChannel channel, Message message) {
//                    System.out.println("Received message on " + channel.toString());
//                    System.out.println("Delivering: " + message.getJSON());
//                    remote.deliver(getServerSession(), "/force", message, null);
//                }
//            });
//        } catch (ForceStreamingException e) {
//            LOGGER.debug("Invalid push topic, " + name);
//            ExceptionMessageProvider invalidPushTopicError = BayeuxExceptionMessage.INVALID_PUSH_TOPIC_NAME;
//            remote.deliver(getServerSession(), "/force", invalidPushTopicError.message(), null);
//        }
    }

    private void validateName(String name) throws ForceStreamingException {
        if (name.matches("^\\w"))
            throw new ForceStreamingException("Invalid push topic name.");
    }

    static class ForceStreamingMessageListener implements ClientSessionChannel.MessageListener {
        final ServerChannel serverChannel;
        public ForceStreamingMessageListener(final ServerChannel serverChannel) {
            this.serverChannel = serverChannel;
        }
        @Override
        public void onMessage(ClientSessionChannel channel, Message message) {
            System.out.println("Received message on " + channel.toString() + ": " + message.getJSON());

            serverChannel.publish(channel.getSession(), message, message.getId());
        }
    }

//    static class ForceStreamingServiceExtension implements BayeuxServer.Extension {
//
//        @Override
//        public boolean rcv(ServerSession from, ServerMessage.Mutable message) {
//            if (message.getChannelId().matches(new ChannelId("/meta/subscribe"))) {
//
//            }
//
//            System.out.println("Message received for " + message.getChannel() + ": " + message);
//            return true;  //To change body of implemented methods use File | Settings | File Templates.
//        }
//
//        @Override
//        public boolean rcvMeta(ServerSession from, ServerMessage.Mutable message) {
//            System.out.println("Message received for " + message.getChannel() + ": " + message);
//            return true;  //To change body of implemented methods use File | Settings | File Templates.
//        }
//
//        @Override
//        public boolean send(ServerSession from, ServerSession to, ServerMessage.Mutable message) {
//            return true;  //To change body of implemented methods use File | Settings | File Templates.
//        }
//
//        @Override
//        public boolean sendMeta(ServerSession to, ServerMessage.Mutable message) {
//            return true;  //To change body of implemented methods use File | Settings | File Templates.
//        }
//
//        private List<String> parseChannelName(ChannelId channelId) {
//            if (channelId.depth() == 2 && channelId.getSegment(0).equalsIgnoreCase("force")) {
//                // allow a user to provide a '|' delimited list of channels (i.e. push topics) to
//                // listen to
//                return Arrays.asList(channelId.getSegment(1).split("\\|"));
//            }
//            return null;
//        }
//
//    }
//
    static class ForceStreamingServiceListener implements BayeuxServer.ChannelListener {

        ForceBayeuxClient client;

        public ForceStreamingServiceListener(ForceBayeuxClient client) {
            this.client = client;
        }

        @Override
        public void channelAdded(final ServerChannel serverChannel) {
            List<String> channelNames = parseChannelName(serverChannel.getChannelId());

            if (channelNames != null) {
                for (String channelName : channelNames) {
                    System.out.println("Connecting to push topic " + channelName);
                    PushTopic pushTopic = new PushTopic();
                    pushTopic.setName(channelName);

                    try {

                        client.subscribeTo(pushTopic, new ForceStreamingMessageListener(serverChannel));
                    } catch (InterruptedException e) {
                        System.err.println("Interrupted exception caught on " + channelName);
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
                }
            }
        }

        private List<String> parseChannelName(ChannelId channelId) {
            if (channelId.depth() == 2 && channelId.getSegment(0).equalsIgnoreCase("force")) {
                // allow a user to provide a '|' delimited list of channels (i.e. push topics) to
                // listen to
                return Arrays.asList(channelId.getSegment(1).split("\\|"));
            }
            return null;
        }

        @Override
        public void channelRemoved(String s) {
            System.out.println("Removing channel " + s);
            // nothin
        }

        @Override
        public void configureChannel(ConfigurableServerChannel channel) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
