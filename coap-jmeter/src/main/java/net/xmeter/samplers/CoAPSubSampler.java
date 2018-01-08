package net.xmeter.samplers;

import java.io.File;
import java.io.FileOutputStream;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

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
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.Request;

import net.xmeter.SubBean;
import net.xmeter.Util;

public class CoAPSubSampler extends AbstractCoAPSampler implements ThreadListener {
    //private transient static Logger logger = LoggingManager.getLoggerForClass();
    private transient static Logger logger = LoggerFactory.getLogger(CoAPSubSampler.class.getName());

    private transient ConcurrentLinkedQueue<SubBean> batches = new ConcurrentLinkedQueue<>();
    private boolean printFlag = false;

    private transient Object lock = new Object();
    private transient AtomicBoolean threadFinished = new AtomicBoolean(false);
    private String clientId = "";
    private CoapClient coapClient;
    private boolean isFirstLoop = true;
    private String uri = "coap://" + getServer() + ":" + getPort();
    private String resourcePath = getResourcePath();
    private String encodedResPath = "";
    private Request request;
    private String query;
    private CoapHandler handler;
    /**
     * 
     */
    private static final long serialVersionUID = 2979978053740194951L;
    
    public String getMethodType() {
        return getPropertyAsString(METHOD_TYPE, DEFAULT_SUB_METHOD_TYPE);
    }
    
    public String getSampleCondition() {
        return getPropertyAsString(SAMPLE_CONDITION, SAMPLE_ON_CONDITION_OPTION1);
    }
    
    public void setSampleCondition(String option) {
        setProperty(SAMPLE_CONDITION, option);
    }
    
    public String getSampleCount() {
        return getPropertyAsString(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_COUNT);
    }
    
    public void setSampleCount(String count) {
        try {
            int temp = Integer.parseInt(count);
            if(temp < 0) {
                logger.info("Invalid sample message count value.");
                throw new IllegalArgumentException();
            }
            setProperty(SAMPLE_CONDITION_VALUE, count);
        } catch(Exception ex) {
            logger.info("Invalid count value, set to default value.");
            setProperty(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_COUNT);
        }
    }
    
    public String getSampleElapsedTime() {
        return getPropertyAsString(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_SEC);
    }
    
    public void setSampleElapsedTime(String elapsedTime) {
        try {
            int temp = Integer.parseInt(elapsedTime);
            if(temp <= 0) {
                throw new IllegalArgumentException();
            }
            setProperty(SAMPLE_CONDITION_VALUE, elapsedTime);
        }catch(Exception ex) {
            logger.info("Invalid elapsed time value, set to default value: {}" ,elapsedTime);
            setProperty(SAMPLE_CONDITION_VALUE, DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_SEC);
        }
    }

    public boolean isAddTimestamp() {
        return getPropertyAsBoolean(ADD_TIMESTAMP);
    }

    public void setAddTimestamp(boolean addTimestamp) {
        setProperty(ADD_TIMESTAMP, addTimestamp);
    }

    public boolean isDebugResponse() {
        return getPropertyAsBoolean(DEBUG_RESPONSE, false);
    }

    public void setDebugResponse(boolean debugResponse) {
        setProperty(DEBUG_RESPONSE, debugResponse);
    }

    public String getClienIdPrefix() {
        return getPropertyAsString(CONN_CLIENT_ID_PREFIX, DEFAULT_CONN_PREFIX_FOR_SUB);
    }

    @Override
    public SampleResult sample(Entry arg0) {
        final boolean sampleByTime = SAMPLE_ON_CONDITION_OPTION1.equals(getSampleCondition());
        final int sampleCount = Integer.parseInt(getSampleCount());
        SampleResult result = new SampleResult();
        
        try {
            
            if(isFirstLoop == true) {
                if(isClientIdSuffix()) {
                    clientId = Util.generateClientId(getClienIdPrefix());
                } else {
                    clientId = getClienIdPrefix();
                }
                uri = "coap://" + getServer() + ":" + getPort();
                query = "?c=" + clientId + "&u=" + getUserNameAuth() + "&p=" + getPasswordAuth();

                resourcePath = getResourcePath();

                coapClient = new CoapClient();
                
                request = Request.newGet();
                
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

                request.setURI(uri);
                
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

                request.getOptions().setUriQuery(query);
                request.setObserve();

                //CoapHandler
                handler = new CoapHandler() {
                    public void onLoad(CoapResponse response) {
                        byte[] payloadBytes = response.getPayload();
                        String payload = null;
                        if(payloadBytes != null) {
                            payload = new String(payloadBytes);
                        }
                        //System.out.println("onLoad "+resourcePath+" "+System.currentTimeMillis()+" Received resonse:the Code is " + response.getCode() + ", the Payload is " + payload);
                        try {
                            synchronized (lock) {
                                SubBean bean = null;
                                if(batches.isEmpty()) {
                                    //System.out.println("onLoad "+resourcePath+" batches are empty, will add new bean!");
                                    bean = new SubBean();
                                    batches.add(bean);
                                } else {
                                    //System.out.println("onLoad "+resourcePath+" get the last bean in batches!");
                                    SubBean[] beans = new SubBean[batches.size()];
                                    batches.toArray(beans);
                                    bean = beans[beans.length - 1];
                                }
        
                                if((!sampleByTime) && (bean.getReceivedCount() == sampleCount)) { //Create a new batch when latest bean is full.
                                    logger.info("The tail bean is full, will create a new bean for it.");
                                    bean = new SubBean();
                                    batches.add(bean);
                                }
                                if(payload != null) {
                                    if (isAddTimestamp()) {
                                        long now = System.currentTimeMillis();
                                        int index = payload.indexOf(TIME_STAMP_SEP_FLAG);
                                        if (index == -1 && (!printFlag)) {
                                            logger.info("Payload does not include timestamp: {}" ,payload);
                                            printFlag = true;
                                        } else if (index != -1) {
                                            long start = Long.parseLong(payload.substring(0, index));
                                            long elapsed = now - start;
            
                                            double avgElapsedTime = bean.getAvgElapsedTime();
                                            int receivedCount = bean.getReceivedCount();
                                            avgElapsedTime = (avgElapsedTime * receivedCount + elapsed) / (receivedCount + 1);
                                            bean.setAvgElapsedTime(avgElapsedTime);
                                        }
                                    }
                                    if (isDebugResponse()) {
                                        bean.getContents().add(payload);
                                    }

                                    bean.getContents().add(resourcePath);
                                    bean.setReceivedMessageSize(bean.getReceivedMessageSize() + payload.length());
                                }
                                bean.setReceivedCount(bean.getReceivedCount() + 1);
                                if(!sampleByTime) {
                                    //logger.info(System.currentTimeMillis() + ": need notify? receivedCount=" + bean.getReceivedCount() + ", sampleCount=" + sampleCount);
                                    if(bean.getReceivedCount() == sampleCount) {
                                        lock.notify();
                                    }
                                }
                            }
                            //isPubReceived = true;
                        } catch (Exception e) {
                            //logger.log(Priority.ERROR, e.getMessage(), e);
                            logger.error(e.getMessage(), e);
                        }
                    }
                    public void onError() { 
                        System.out.println("Error happened in coaphandler");
                    }
                };
                
                //CoapObserveRelation relation = coapClient.observeAndWait(handler);
                //coapClient.observeAndWait(handler);
                //coapClient.advanced(handler, request);
                coapClient.observe(request, handler);
 /*               
                String hostName = InetAddress.getLocalHost().getHostName();
                String path = "/home/xmeter/DClogs/" + hostName + "_coapsubclient.log";
                //String path = System.getProperty("user.dir")+"/coapsubclient.log";
                CaliforniumLogger.initialize(new FileOutputStream(new File(path)));
                CaliforniumLogger.setLevel(Level.WARNING);
*/
                isFirstLoop = false;
            }
            
            result.setSampleLabel(getName());
            
            result.sampleStart();
    
            synchronized (lock) {
                
                if(sampleByTime) {
                    try {
                        //System.out.println(System.currentTimeMillis()+" "+ resourcePath+ " Will Wait");
                        lock.wait();
                        //System.out.println(System.currentTimeMillis()+" "+ resourcePath+" Finish Wait");
                    } catch (InterruptedException e) {
                        //logger.info("Received exception when waiting for notification signal: " + e.getMessage());
                        logger.info("Received exception when waiting for notification signal: {}" ,e.getMessage());
                    }
                } else {
                    int receivedCount = (batches.isEmpty() ? 0 : batches.element().getReceivedCount());;
                    boolean needWait = false;
                    if(receivedCount < sampleCount) {
                        needWait = true;
                    }
                    
                    //logger.info(System.currentTimeMillis() + ": need wait? receivedCount=" + receivedCount + ", sampleCount=" + sampleCount);
                    if(needWait) {
                        try {
                            lock.wait();
                        } catch (InterruptedException e) {
                            //logger.info("Received exception when waiting for notification signal: " + e.getMessage());
                            logger.info("Received exception when waiting for notification signal: {}" ,e.getMessage());
                        }
                    }
                }
                
                SubBean bean = batches.poll();
                if(bean == null) { //In case selected with time interval
                    //System.out.println("sampler "+ resourcePath+" batches.poll returns null!"); //me
                    bean = new SubBean();
                }//else { //me
                    //System.out.println("sampler "+ resourcePath+" batches.poll returns bean.getcontents is "+bean.getContents()); //me
                //} //me
                int receivedCount = bean.getReceivedCount();
                List<String> contents = bean.getContents();
                String message = MessageFormat.format("Received {0} of message\n.", receivedCount);
                StringBuffer content = new StringBuffer("");
                if (isDebugResponse()) {
                    for (int i = 0; i < contents.size(); i++) {
                        content.append(contents.get(i) + " \n");
                    }
                }

                if(receivedCount == 0) {
                    //System.out.println(resourcePath + " receives NO response");
                    result = fillFailedResult(result, "No CoAP Publish message received!");
                    result.setEndTime(result.getStartTime());
                } else {
                    result = fillOKResult(result, bean.getReceivedMessageSize(), message, content.toString());
                    if (isAddTimestamp()) {
                        result.setEndTime(result.getStartTime() + (long) bean.getAvgElapsedTime());
                    } else {
                        result.setEndTime(result.getStartTime());   
                    }
                }
                result.setSampleCount(receivedCount);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private SampleResult fillFailedResult(SampleResult result, String message) {
        result.setResponseCode("500");
        result.setSuccessful(false);
        result.setResponseMessage(message);
        result.setResponseData("Failed.".getBytes());
        result.setEndTime(result.getStartTime());
        return result;
    }

    private SampleResult fillOKResult(SampleResult result, int size, String message, String contents) {
        result.setResponseCode("200");
        result.setSuccessful(true);
        result.setResponseMessage(message);
        result.setBodySize(size);
        result.setBytes(size);
        result.setResponseData(contents.getBytes());
        result.sampleEnd();
        return result;
    }

    @Override
    public void threadStarted() {
        //logger.info("*** in threadStarted");
        boolean sampleByTime = SAMPLE_ON_CONDITION_OPTION1.equals(getSampleCondition());
        if(!sampleByTime) {
            logger.info("Configured with sampled on message count, will not check message sent time.");
            return;
        }
        
        final long sampleElapsedTime = Long.parseLong(getSampleElapsedTime());
        
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.submit(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    try {
                        //logger.info(System.currentTimeMillis() + ", sampleElapsedTime = " + sampleElapsedTime);
                        if(threadFinished.get()) {
                            synchronized (lock) {
                                lock.notify();
                            }
                            break;
                        }
                        TimeUnit.MILLISECONDS.sleep(sampleElapsedTime);
                        synchronized (lock) {
                            lock.notify();
                        }
                    } catch (Exception e) {
                        //logger.log(Priority.ERROR, e.getMessage());
                        logger.error(e.getMessage(), e);
                    } 
                }
            }
        });
        executor.shutdown();
    }
    
    @Override
    public void threadFinished() {
        //logger.info(System.currentTimeMillis() + ", threadFinished");
        //System.out.println("Sub thread Finished!!!");
        threadFinished.set(true);
        //logger.info("*** in threadFinished");
    }
}
