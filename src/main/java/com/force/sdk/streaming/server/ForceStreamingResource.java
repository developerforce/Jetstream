package com.force.sdk.streaming.server;

import org.cometd.bayeux.server.BayeuxServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * @author naamannewbold
 */
public class ForceStreamingResource extends GenericServlet {

    private static final Logger LOGGER = LoggerFactory.getLogger(ForceStreamingResource.class);

    public void init() {
        LOGGER.debug("Initiating ForceStreamingResource");
        BayeuxServer bayeux = (BayeuxServer)getServletContext().getAttribute(BayeuxServer.ATTRIBUTE);
        new ForceStreamingService(bayeux);
    }

    @Override
    public void service(ServletRequest servletRequest, ServletResponse servletResponse) throws ServletException, IOException {
        throw new ServletException();
    }
}