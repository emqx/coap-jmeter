package net.xmeter;

public interface CoAPConstants {
    public static final String SERVER = "coap.server";
    public static final String PORT = "coap.port";
    public static final String CONN_TIMEOUT = "coap.conn_timeout";
    
    public static final String PROTOCOL = "coap.protocol";
    public static final String DUAL_AUTH = "coap.dual_ssl_authentication";
    public static final String CERT_FILE_PATH1 = "coap.keystore_file_path";
    public static final String CERT_FILE_PATH2 = "coap.clientcert_file_path";
    public static final String KEY_FILE_PWD1 = "coap.keystore_password";
    public static final String KEY_FILE_PWD2 = "coap.clientcert_password";

    public static final String CONN_CLIENT_ID_PREFIX = "coap.client_id_prefix";
    public static final String CONN_CLIENT_ID_SUFFIX = "coap.client_id_suffix";
    public static final String USER_NAME_AUTH = "coap.user_name";
    public static final String PASSWORD_AUTH = "coap.password";

    public static final String COAP_MESSAGE_TYPE = "coap.message_type";
    public static final String METHOD_TYPE = "coap.message_type";
    public static final String MESSAGE_ID = "coap.message_id";
    public static final String TOKEN = "coap.token";
    public static final String RESOURCE_PATH = "coap.resource_path";
    
    public static final String PAYLOAD_TYPE = "coap.payload_type";
    public static final String PAYLOAD_FIX_LENGTH = "coap.payload_type_fixed_length";
    public static final String PAYLOAD_TO_BE_SENT = "coap.payload_to_sent";
    public static final String PAYLOAD_SIZE = "coap.payload_size";
    
    public static final String ADD_TIMESTAMP = "coap.add_timestamp";
    public static final String TIME_STAMP = "coap.time_stamp";
    public static final String TIME_STAMP_SEP_FLAG = "ts_sep_flag";
    
    public static final String SAMPLE_CONDITION_VALUE = "coap.sample_condition_value";
    public static final String SAMPLE_CONDITION = "coap.sample_condition";
    
    public static final String DEBUG_RESPONSE = "coap.debug_response";
    
    public static final String PAYLOAD_TYPE_RANDOM_STR_WITH_FIX_LEN = "Random string with fixed length";
    public static final String PAYLOAD_TYPE_HEX_STRING = "Hex string";
    public static final String PAYLOAD_TYPE_STRING = "String";
    
    public static final String SAMPLE_ON_CONDITION_OPTION1 = "specified elapsed time (ms)";
    public static final String SAMPLE_ON_CONDITION_OPTION2 = "number of received messages";
    
    public static final int MAX_CLIENT_ID_LENGTH = 23;
    
    public static final String DEFAULT_SERVER = "127.0.0.1";
    public static final String DEFAULT_PORT = "5683";
    public static final String DEFAULT_CONN_TIME_OUT = "10";
    public static final String DEFAULT_PROTOCOL = "UDP";
    
    public static final String DEFAULT_USERNAME = "coap_user";
    public static final String DEFAULT_PASSWORD = "coap_secret";
    
    public static final String JMETER_VARIABLE_PREFIX = "${";
    
    public static final String DEFAULT_RESPURCE_PATH = "/mqtt/coap_test_topic";
    public static final String DEFAULT_COAP_MESSAGE_TYPE = "CON";
    public static final String DEFAULT_PUB_METHOD_TYPE = "PUT";
    public static final String DEFAULT_SUB_METHOD_TYPE = "GET";
    public static final String DEFAULT_MESSAGE_ID = "1";
    public static final String DEFAULT_TOKEN = "";

    public static final String DEFAULT_CONN_PREFIX = "coap_";
    public static final String DEFAULT_CONN_PREFIX_FOR_PUB = "coap_pub_";
    public static final String DEFAULT_CONN_PREFIX_FOR_SUB = "coap_sub_";
    
    public static final String DEFAULT_SAMPLE_VALUE_COUNT = "1";
    public static final String DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_SEC = "1000";
    
    public static final boolean DEFAULT_ADD_TIMESTAMP = false;
    public static final String DEFAULT_PAYLOAD_FIX_LENGTH = "1024";
    
    public static final boolean DEFAULT_ADD_CLIENT_ID_SUFFIX = true;

}
