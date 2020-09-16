package net.xmeter.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.HorizontalPanel;
import org.apache.jmeter.gui.util.JSyntaxTextArea;
import org.apache.jmeter.gui.util.JTextScrollPane;
import org.apache.jmeter.gui.util.VerticalPanel;
import org.apache.jmeter.samplers.gui.AbstractSamplerGui;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jorphan.gui.JLabeledChoice;
import org.apache.jorphan.gui.JLabeledTextField;
//import org.apache.jorphan.logging.LoggingManager;
//import org.apache.log.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.xmeter.CoAPConstants;
import net.xmeter.samplers.CoAPPubSampler;

public class CoAPPubSamplerUI extends AbstractSamplerGui implements CoAPConstants, ChangeListener {
    //private static final Logger logger = LoggingManager.getLoggerForClass();
    private transient static Logger logger = LoggerFactory.getLogger(CoAPPubSamplerUI.class.getName());
    
    private CoAPCommonUI commonUI = new CoAPCommonUI();

    private static final long serialVersionUID = 2479085966683186422L;

    private JCheckBox timestamp = new JCheckBox("Add timestamp in payload");

    private JLabeledChoice coapMessageType;
    private JLabeledChoice methodType;
    private final JLabeledTextField messageId = new JLabeledTextField("Message ID:");
    private final JLabeledTextField token = new JLabeledTextField("Token:");
    private final JLabeledTextField resourcePath = new JLabeledTextField("Resource Path(Topic):");

    private JLabeledChoice payloadTypes;
    private final JSyntaxTextArea sendPayload = JSyntaxTextArea.getInstance(10, 50);
    private final JTextScrollPane payloadPanel = JTextScrollPane.getInstance(sendPayload);
    private JLabeledTextField stringLength = new JLabeledTextField("Length:");

    public CoAPPubSamplerUI() {
        init();
    }

    private void init() {
        logger.info("Initializing the UI.");
        setLayout(new BorderLayout());
        setBorder(makeBorder());

        add(makeTitlePanel(), BorderLayout.NORTH);
        JPanel mainPanel = new VerticalPanel();
        add(mainPanel, BorderLayout.CENTER);

        mainPanel.add(commonUI.createConnPanel());
        mainPanel.add(commonUI.createProtocolPanel());
        mainPanel.add(commonUI.createAuthentication());

        mainPanel.add(createMessageOptions());
        mainPanel.add(createPayload());
    }
    
    public JPanel createMessageOptions() {
        JPanel optsPanelCon = new VerticalPanel();
        optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Message options"));
        
        JPanel optsPanel0 = new HorizontalPanel();
        coapMessageType = new JLabeledChoice("Type:", new String[] { "NON", "CON" }, false, false);
        coapMessageType.addChangeListener(this);
        
        methodType = new JLabeledChoice("Method:", new String[] { "PUT", "POST" }, true, false);
        methodType.addChangeListener(this);
        
        optsPanel0.add(coapMessageType);
        optsPanel0.add(methodType);
        optsPanel0.add(messageId);
        optsPanel0.add(token);
        optsPanelCon.add(optsPanel0);
        
        JPanel optsPanel1 = new HorizontalPanel();
        optsPanel1.add(resourcePath);
        optsPanelCon.add(optsPanel1);
        
        return optsPanelCon;
    }

    private JPanel createPayload() {
        JPanel optsPanelCon = new VerticalPanel();
        optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Payloads"));
        
        JPanel horizon1 = new HorizontalPanel();
        payloadTypes = new JLabeledChoice("Message type:", new String[] { PAYLOAD_TYPE_STRING, PAYLOAD_TYPE_HEX_STRING, PAYLOAD_TYPE_RANDOM_STR_WITH_FIX_LEN }, false, false);
        payloadTypes.addChangeListener(this);
        payloadTypes.setSelectedIndex(0);
        
        horizon1.add(payloadTypes, BorderLayout.WEST);
        stringLength.setVisible(false);
        horizon1.add(stringLength);
        
        horizon1.add(timestamp);
        
        JPanel horizon2 = new VerticalPanel();
        payloadPanel.setVisible(false);
        horizon2.add(payloadPanel);
        
        optsPanelCon.add(horizon1);
        optsPanelCon.add(horizon2);
        return optsPanelCon;
    }

    @Override
    public String getStaticLabel() {
        return "CoAP Pub Sampler";
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(e.getSource() == this.payloadTypes) {
            int selectedIndex = this.payloadTypes.getSelectedIndex();
            if(selectedIndex == 0 || selectedIndex == 1) {
                stringLength.setVisible(false);
                payloadPanel.setVisible(true);
            } else if(selectedIndex == 2) {
                stringLength.setVisible(true);
                payloadPanel.setVisible(false);
            } else {
                logger.info("Unknown message type.");
            }
        }
    }

    @Override
    public String getLabelResource() {
        return "";
    }

    @Override
    public TestElement createTestElement() {
        CoAPPubSampler sampler = new CoAPPubSampler();
        this.setupSamplerProperties(sampler);
        return sampler;
    }

    @Override
    public void configure(TestElement element) {
        super.configure(element);
        CoAPPubSampler sampler = (CoAPPubSampler) element;
        
        commonUI.configure(sampler);
        
        this.coapMessageType.setText(sampler.getCoapMessageType());
        this.methodType.setText(sampler.getMethodType());
        this.messageId.setText(sampler.getMessageId());
        this.token.setText(sampler.getToken());
        this.resourcePath.setText(sampler.getResourcePath());
        
        this.timestamp.setSelected(sampler.isAddTimestamp());
        if(PAYLOAD_TYPE_STRING.equalsIgnoreCase(sampler.getPayloadType())) {
            this.payloadTypes.setSelectedIndex(0);  
            this.payloadPanel.setVisible(true);
        } else if(PAYLOAD_TYPE_HEX_STRING.equalsIgnoreCase(sampler.getPayloadType())) {
            this.payloadTypes.setSelectedIndex(1);
        } else if(PAYLOAD_TYPE_RANDOM_STR_WITH_FIX_LEN.equalsIgnoreCase(sampler.getPayloadType())) {
            this.payloadTypes.setSelectedIndex(2);
        }
        
        stringLength.setText(String.valueOf(sampler.getPayloadLength()));
        sendPayload.setText(sampler.getPayload());
    }

    @Override
    public void modifyTestElement(TestElement arg0) {
        CoAPPubSampler sampler = (CoAPPubSampler) arg0;
        this.setupSamplerProperties(sampler);
    }

    private void setupSamplerProperties(CoAPPubSampler sampler) {
        this.configureTestElement(sampler);
        commonUI.setupSamplerProperties(sampler);
        
        sampler.setCoapMessageType(this.coapMessageType.getText());
        sampler.setMethodType(this.methodType.getText());
        sampler.setMessageId(this.messageId.getText());
        sampler.setToken(this.token.getText());
        sampler.setResourcePath(this.resourcePath.getText());
        
        sampler.setAddTimestamp(this.timestamp.isSelected());
        sampler.setPayloadType(this.payloadTypes.getText());
        sampler.setPayloadLength(this.stringLength.getText());
        sampler.setPayload(this.sendPayload.getText());
    }

    @Override
    public void clearGui() {
        super.clearGui();
        commonUI.clearUI();
        commonUI.clientIdPrefix.setText(DEFAULT_CONN_PREFIX_FOR_PUB);
        
        this.coapMessageType.setText(DEFAULT_COAP_MESSAGE_TYPE);
        this.methodType.setText(DEFAULT_PUB_METHOD_TYPE);
        this.messageId.setText("");
        this.token.setText("");
        this.resourcePath.setText(DEFAULT_RESPURCE_PATH);
        
        this.timestamp.setSelected(false);      
        this.payloadTypes.setSelectedIndex(2);
        this.stringLength.setText(String.valueOf(DEFAULT_PAYLOAD_FIX_LENGTH));
        this.payloadTypes.setText("");
    }

}
