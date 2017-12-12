package net.xmeter.samplers;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.xml.bind.DatatypeConverter;

import org.apache.jmeter.samplers.Entry;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.ThreadListener;
//import org.apache.jorphan.logging.LoggingManager;
//import org.apache.log.Logger;
//import org.apache.log.Priority;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.eclipse.californium.core.CaliforniumLogger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import net.xmeter.Util;

public class CoAPPubSampler extends AbstractCoAPSampler implements ThreadListener {
    private static final long serialVersionUID = -4312341622759500786L;
    //private transient static Logger logger = LoggingManager.getLoggerForClass();
    private transient static Logger logger = LoggerFactory.getLogger(CoAPPubSampler.class.getName());
    private CoapClient coapClient;
    private String payload = null;
    private String clientId = "";
    private boolean isFirstLoop = true;
    private String uri;
    private String resourcePath;
    private String encodedResPath;
    private Request request;
    private String query;
    
    public String getMethodType() {
        return getPropertyAsString(METHOD_TYPE, DEFAULT_PUB_METHOD_TYPE);
    }

    public String getPayloadType() {
        return getPropertyAsString(PAYLOAD_TYPE, PAYLOAD_TYPE_RANDOM_STR_WITH_FIX_LEN);
    }

    public void setPayloadType(String payloadType) {
        setProperty(PAYLOAD_TYPE, payloadType);
    }

    public String getPayloadLength() {
        return getPropertyAsString(PAYLOAD_FIX_LENGTH, DEFAULT_PAYLOAD_FIX_LENGTH);
    }

    public void setPayloadLength(String length) {
        setProperty(PAYLOAD_FIX_LENGTH, length);
    }

    public String getPayload() {
        return getPropertyAsString(PAYLOAD_TO_BE_SENT, "");
    }

    public void setPayload(String message) {
        setProperty(PAYLOAD_TO_BE_SENT, message);
    }

    public boolean isAddTimestamp() {
        return getPropertyAsBoolean(ADD_TIMESTAMP);
    }

    public void setAddTimestamp(boolean addTimestamp) {
        setProperty(ADD_TIMESTAMP, addTimestamp);
    }

    public String getClienIdPrefix() {
        return getPropertyAsString(CONN_CLIENT_ID_PREFIX, DEFAULT_CONN_PREFIX_FOR_PUB);
    }

    public static byte[] hexToBinary(String hex) {
        return DatatypeConverter.parseHexBinary(hex);
    }
    
    @Override
    public SampleResult sample(Entry arg0) {
        SampleResult result = new SampleResult();
        //logger.info("Establish a publish sample");
        try {
            if(isFirstLoop == true) {
/*
                //String path = System.getProperty("user.dir")+"/coappubclient.log";
                String hostName = InetAddress.getLocalHost().getHostName();
                String path = "/home/xmeter/DClogs/" + hostName + "_coappubclient.log";
                CaliforniumLogger.initialize(new FileOutputStream(new File(path)));
                CaliforniumLogger.setLevel(Level.WARNING);
                //CaliforniumLogger.initialize();
*/
                if(isClientIdSuffix()) {
                    clientId = Util.generateClientId(getClienIdPrefix());
                } else {
                    clientId = getClienIdPrefix();
                }

                query = "?c=" + clientId + "&u=" + getUserNameAuth() + "&p=" + getPasswordAuth();
                isFirstLoop = false;
                coapClient = new CoapClient();
            }

            uri = "coap://" + getServer() + ":" + getPort();
            if(DEFAULT_PUB_METHOD_TYPE.equals(getMethodType())) {
                request = Request.newPut();
            } else {
                request = Request.newPost();
            }
            
            if(DEFAULT_COAP_MESSAGE_TYPE.equals(getMethodType())) {
                request.setConfirmable(false);
            } else {
                request.setConfirmable(true);
            }
            
            if(!getMessageId().equals("")) {
                try {
                    request.setMID(Integer.parseInt(getMessageId()));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            
            if(!getToken().equals("")) {
                request.setToken(getToken().getBytes());
            }
            
            request.getOptions().setContentFormat(MediaTypeRegistry.APPLICATION_OCTET_STREAM);
            request.setURI(uri);
            request.getOptions().setUriQuery(query);
            
            resourcePath = getResourcePath();
            
            if(resourcePath.startsWith("/")) {
                resourcePath = resourcePath.substring("/".length());
            }
            
            if(resourcePath.startsWith("mqtt/")) {
                request.getOptions().addUriPath("mqtt").addUriPath(resourcePath.substring("mqtt/".length()));
            } else if(resourcePath.startsWith("ps/")) {
                request.getOptions().addUriPath("ps").addUriPath(resourcePath.substring("ps/".length()));
            } else {
                request.getOptions().addUriPath(resourcePath);
            }
            
            if (PAYLOAD_TYPE_RANDOM_STR_WITH_FIX_LEN.equals(getPayloadType())) {
                payload = Util.generatePayload(Integer.parseInt(getPayloadLength()));
            }
            
            result.setSampleLabel(getName());

            byte[] toSend = new byte[]{};
            byte[] tmp = new byte[]{};

            if (PAYLOAD_TYPE_HEX_STRING.equals(getPayloadType())) {
                tmp = hexToBinary(getPayload());
            } else if (PAYLOAD_TYPE_STRING.equals(getPayloadType())) {
                tmp = getPayload().getBytes();
            } else if(PAYLOAD_TYPE_RANDOM_STR_WITH_FIX_LEN.equals(getPayloadType())) {
                tmp = payload.getBytes();
            }

            if (isAddTimestamp()) {
                byte[] timePrefix = (System.currentTimeMillis() + TIME_STAMP_SEP_FLAG).getBytes();
                toSend = new byte[timePrefix.length + tmp.length];
                System.arraycopy(timePrefix, 0, toSend, 0, timePrefix.length);
                System.arraycopy(tmp, 0, toSend, timePrefix.length , tmp.length);
            } else {
                toSend = new byte[tmp.length];
                System.arraycopy(tmp, 0, toSend, 0 , tmp.length);
            }
            
            request.setPayload(toSend);
            
            result.sampleStart();

            //CoapResponse response = coapClient.advanced(request);
            //coapClient = new CoapClient();
            coapClient.advanced(request);
            //System.out.println(resourcePath+" published "+System.currentTimeMillis());

            result.sampleEnd();
            result.setSuccessful(true);
            result.setResponseData((MessageFormat.format("Publish Successful by {0}.", clientId)).getBytes());
            result.setResponseMessage(MessageFormat.format("publish successfully to topic {0}.", getResourcePath()));
            result.setResponseCodeOK();
        }
        catch (Exception e) {
            //logger.log(Priority.ERROR, e.getMessage(), e);
            logger.error(e.getMessage(), e);
            result.sampleEnd();
            result.setSuccessful(false);
            result.setResponseMessage(MessageFormat.format("Publish failed to topic {0}.", getResourcePath()));
            result.setResponseData("Failed.".getBytes());
            result.setResponseCode("500");
        }
        return result;
    }

    @Override
    public void threadStarted() {
        //System.out.println("thread Started!!!");
    }

    @Override
    public void threadFinished() {
        //System.out.println("Pub thread Finished!!!");
    }

}
