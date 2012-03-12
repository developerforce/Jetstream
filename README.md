# Jetstream
Jetstream is a toolkit for the Force.com Streaming API, a Bayeux implementation built on the Force.com platform.  Jetstream provides a simple way to configure a connection to the Streaming API.  For more information on the Force.com Streaming API, please review the [guide](http://wiki.developerforce.com/index.php/Getting_Started_with_the_Force.com_Streaming_API).

The following instructions assume some knowledge of the Force.com platform.  If you are unfamiliar with Force.com, please visit http://developer.force.com.

## Install
4. export FORCE_FORCEDATABASE_URL="force://instance.salesforce.com;user=username@org;password=password"
5. add the following to your own pom.xml

```xml
        <dependency>
            <groupId>com.force.sdk.streaming</groupId>
            <artifactId>streaming</artifactId>
            <version>0.13</version>
        </dependency>
```

```xml
        <repositories>
            <repository>
                <id>repo</id>
                <url>https://raw.github.com/naamannewbold/repo/master/releases/</url>
            </repository>
        </repositories>
```

## Java Usage
Jetstream uses Guice for dependency injection. If you wish to use the client directly, add Guice to your pom.xml:

```xml
        <dependency>
            <groupId>com.google.inject</groupId>
            <artifactId>guice</artifactId>
            <version>3.0</version>
        </dependency>
```

Write some Java to handle messages:

```java
        Injector injector = Guice.createInjector(new ForceStreamingClientModule());
        ForceBayeuxClient client = injector.getInstance(ForceBayeuxClient.class);
        PushTopicManager pushTopicManager = injector.getInstance(PushTopicManager.class);
        PushTopic topic = pushTopicManager.getTopicByName("products");
        client.subscribeTo(topic, new ClientSessionChannel.MessageListener() {
            public void onMessage(ClientSessionChannel channel, Message message) {
                System.out.println(message.getJSON());
            }
        });
```

## Java Web Project Usage
Due to cross-domain limitations, javascript cannot callout to the streaming API unless it's on salesforce.com. This servlet acts as a proxy, allowing javascript to interact with the streaming API.

Add this dependency to your pom.xml

```xml
        <dependency>
            <groupId>com.force.sdk.streaming</groupId>
            <artifactId>streaming</artifactId>
            <version>0.13</version>
        </dependency>
```

Add the cometd and streaming servlets to your web.xml

```xml
        <servlet>
            <servlet-name>cometd</servlet-name>
            <servlet-class>org.cometd.server.CometdServlet</servlet-class>
            <load-on-startup>1</load-on-startup>
            <init-param>
                <param-name>timeout</param-name>
                <param-value>20000</param-value>
            </init-param>
        </servlet>
        <servlet-mapping>
            <servlet-name>cometd</servlet-name>
            <url-pattern>/cometd/*</url-pattern>
        </servlet-mapping>
        <servlet>
            <servlet-name>initializer</servlet-name>
            <servlet-class>com.force.sdk.streaming.server.ForceStreamingResource</servlet-class>
            <load-on-startup>2</load-on-startup>
        </servlet>
```

Wire your javascript client to talk to /force/topicName, e.g.

```html
        <html>
            <head>
                <script type="text/javascript" src="/jquery/jquery-1.6.2.js"></script>
                <script type="text/javascript" src="/jquery/json2.js"></script>
                <script type="text/javascript" src="/org/cometd.js"></script>
                <script type="text/javascript" src="/jquery/jquery.cometd.js"></script>
                <script type="text/javascript">
                (function($)
        	{
        	    var cometd = $.cometd;
        
        	    $(document).ready(function()
        	    {
        		function _metaHandshake(handshake)
        		{
        		    if (handshake.successful === true)
        		    {
        			cometd.batch(function()
        			{
        			    cometd.subscribe('/force/products', function(messages) {
        				$("body").append('<pre>' + JSON.stringify(messages) + '</pre>');
        			    });
        			});
        		    }
        		}
        
        		// Disconnect when the page unloads
        		$(window).unload(function()
        		{
        		    cometd.disconnect(true);
        		});
        
        		var cometURL = location.protocol + "//" + location.host + "/cometd";
        		cometd.configure({
        		    url: cometURL
        		});
        
        		cometd.addListener('/meta/handshake', _metaHandshake);
        
        		cometd.handshake();
        	    });
        	})(jQuery);
            </script>
            </head>
            <body>
            </body>
        </html>
```
