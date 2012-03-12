package com.force.sdk.streaming.util;

/**
 * @author naamannewbold
 */
public enum ForceStreamingResource {
    CHANNEL_PREFIX ("/topic/")
  , RESOURCE_ENDPOINT ("/cometd/24.0")
    // TODO: parameterize http vs https -- need to be able to toggle for debugging
  , PROTOCOL ("https");

    private String value;

    ForceStreamingResource(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
