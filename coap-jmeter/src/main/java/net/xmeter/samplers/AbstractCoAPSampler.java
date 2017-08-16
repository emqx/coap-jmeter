package net.xmeter.samplers;

import org.apache.jmeter.samplers.AbstractSampler;

import net.xmeter.CoAPConstants;

public abstract class AbstractCoAPSampler extends AbstractSampler implements CoAPConstants {
    private static final long serialVersionUID = 7163793218595455807L;

    public String getServer() {
        return getPropertyAsString(SERVER, DEFAULT_SERVER);
    }

    public void setServer(String server) {
        setProperty(SERVER, server);
    }

    public String getPort() {
        return getPropertyAsString(PORT, DEFAULT_PORT);
    }

    public void setPort(String port) {
        setProperty(PORT, port);
    }
/*
    public String getConnTimeout() {
        return getPropertyAsString(CONN_TIMEOUT, DEFAULT_CONN_TIME_OUT);
    }

    public void setConnTimeout(String connTimeout) {
        setProperty(CONN_TIMEOUT, connTimeout);
    }
*/
    public String getProtocol() {
        return getPropertyAsString(PROTOCOL, DEFAULT_PROTOCOL);
    }

    public void setProtocol(String protocol) {
        setProperty(PROTOCOL, protocol);
    }

    public boolean isDualSSLAuth() {
        return getPropertyAsBoolean(DUAL_AUTH, false);
    }

    public void setDualSSLAuth(boolean dualSSLAuth) {
        setProperty(DUAL_AUTH, dualSSLAuth);
    }

    public String getKeyStoreFilePath() {
        return getPropertyAsString(CERT_FILE_PATH1, "");
    }

    public void setKeyStoreFilePath(String certFile1) {
        setProperty(CERT_FILE_PATH1, certFile1);
    }

    public String getClientCertFilePath() {
        return getPropertyAsString(CERT_FILE_PATH2, "");
    }

    public void setClientCertFilePath(String certFile2) {
        setProperty(CERT_FILE_PATH2, certFile2);
    }

    public String getKeyStorePassword() {
        return getPropertyAsString(KEY_FILE_PWD1, "");
    }
    
    public void setKeyStorePassword(String keyStorePassword) {
        this.setProperty(KEY_FILE_PWD1, keyStorePassword);
    }

    public String getClientCertPassword() {
        return getPropertyAsString(KEY_FILE_PWD2, "");
    }

    public void setClientCertPassword(String clientCertPassword) {
        this.setProperty(KEY_FILE_PWD2, clientCertPassword);
    }
    
    public String getClienIdPrefix() {
        return getPropertyAsString(CONN_CLIENT_ID_PREFIX, DEFAULT_CONN_PREFIX);
    }

    public void setClienIdPrefix(String connPrefix) {
        setProperty(CONN_CLIENT_ID_PREFIX, connPrefix);
    }
    public boolean isClientIdSuffix() {
        return getPropertyAsBoolean(CONN_CLIENT_ID_SUFFIX, DEFAULT_ADD_CLIENT_ID_SUFFIX);
    }
    
    public void setClientIdSuffix(boolean clientIdSuffix) {
        setProperty(CONN_CLIENT_ID_SUFFIX, clientIdSuffix);
    }
    
    public String getUserNameAuth() {
        return getPropertyAsString(USER_NAME_AUTH, DEFAULT_USERNAME);
    }

    public void setUserNameAuth(String userName) {
        setProperty(USER_NAME_AUTH, userName);
    }
    
    public String getPasswordAuth() {
        return getPropertyAsString(PASSWORD_AUTH, DEFAULT_PASSWORD);
    }

    public void setPasswordAuth(String password) {
        setProperty(PASSWORD_AUTH, password);
    }
    
    public boolean isKeepTimeShow() {
        return false;
    }
    
    public String getCoapMessageType() {
        return getPropertyAsString(COAP_MESSAGE_TYPE, DEFAULT_COAP_MESSAGE_TYPE);
    }

    public void setCoapMessageType(String coapMessageType) {
        setProperty(COAP_MESSAGE_TYPE, coapMessageType);
    }
    
    public String getMethodType() {
        return getPropertyAsString(METHOD_TYPE, DEFAULT_PUB_METHOD_TYPE);
    }

    public void setMethodType(String methodType) {
        setProperty(METHOD_TYPE, methodType);
    }
    
    public String getMessageId() {
        return getPropertyAsString(MESSAGE_ID, "");
    }

    public void setMessageId(String messageId) {
        setProperty(MESSAGE_ID, messageId);
    }
    
    public String getToken() {
        return getPropertyAsString(TOKEN, DEFAULT_TOKEN);
    }

    public void setToken(String token) {
        setProperty(TOKEN, token);
    }

    public String getResourcePath() {
        return getPropertyAsString(RESOURCE_PATH, DEFAULT_RESPURCE_PATH);
    }

    public void setResourcePath(String resourcePath) {
        setProperty(RESOURCE_PATH, resourcePath);
    }
}
