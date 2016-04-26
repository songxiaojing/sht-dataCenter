package com.byw.stock.house.data.center.platform.services;

import com.byw.stock.house.platform.core.metrics.AbstractMetricMBean;
import com.byw.stock.house.platform.core.services.IPlatformService;
import com.byw.stock.house.platform.core.services.PlatformServiceInfo;
import com.byw.stock.house.platform.core.utils.Assert;
import com.byw.stock.house.platform.core.utils.IOUtils;
import com.byw.stock.house.platform.log.PlatformLogger;

import javax.management.DynamicMBean;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.HashMap;


@PlatformServiceInfo(description = "MessagePrepareService", configurationPath = "application/services/MessagePrepareService/")
public class MessagePrepareService extends AbstractMetricMBean implements IPlatformService, DynamicMBean {

    //
    // Logging Object
    //
    private static PlatformLogger theLogger = PlatformLogger.getLogger(MessagePrepareService.class);

    //
    // PRIVATE FINAL STATIC INSANCE VARIABLE
    //
    final private static String CLASS_NAME_TO_PATH_RULE_STR = "\\.";
    final private static String CLASS_NAME_TO_PATH_TARGET_STR = "/";
    private static final String SUFFIX = ".js";
    private static final String MESSAGE_PATH = "messages";

    private static HashMap<String, MessageFormat> _propertyFormatters = new HashMap<String, MessageFormat>();

    @Override
    public void start() throws Exception {

        theLogger.info("startUp");
    }

    @Override
    public void stop() throws Exception {

    }

    /**
     * according to the class and key ,format the messages, the configuration will be defined into property file which name is suffix with Res.properties
     * 
     * @param arguments
     * @return
     */
    public String formatMessage(String messageName, Object... arguments) {

        if (Assert.isEmptyString(messageName) == true) {
            return null;
        }

        MessageFormat messageFormatter = _propertyFormatters.get(messageName);

        // If the formatter doesn't exist then we want to create
        // a new one. First see if we loaded the bundle
        // and then create the formatter.
        if (messageFormatter == null) {
            // Load the resource Bundle.
            StringBuffer contentBuffer = IOUtils.getInputStream2StringBuffer(getMessageFileStream(messageName));
            if (contentBuffer == null) {
                return null;
            }
            // Create the message formatter and format the message.
            messageFormatter = new MessageFormat(contentBuffer.toString());
            _propertyFormatters.put(messageName, messageFormatter);
        }

        return messageFormatter.format(arguments);
    }

    /**
     * This method will use the supplied class to load the resource bundle and add it to the HashMap.
     * 
     * @return
     */
    private InputStream getMessageFileStream(String messageName) {

        return this.getClass().getClassLoader()
                .getResourceAsStream(this.getClass().getPackage().getName().replaceAll(CLASS_NAME_TO_PATH_RULE_STR, CLASS_NAME_TO_PATH_TARGET_STR) + CLASS_NAME_TO_PATH_TARGET_STR + MESSAGE_PATH + CLASS_NAME_TO_PATH_TARGET_STR + messageName + SUFFIX);
    }

}
