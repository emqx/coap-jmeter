package net.xmeter.gui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.jmeter.gui.util.HorizontalPanel;
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
import net.xmeter.samplers.CoAPSubSampler;

public class CoAPSubSamplerUI extends AbstractSamplerGui implements CoAPConstants, ChangeListener {
    //private static final Logger logger = LoggingManager.getLoggerForClass();
    private transient static Logger logger = LoggerFactory.getLogger(CoAPSubSamplerUI.class.getName());
    
    private CoAPCommonUI commonUI = new CoAPCommonUI();

    private JLabeledChoice coapMessageType;
    private JLabeledChoice methodType;
    private final JLabeledTextField messageId = new JLabeledTextField("Message ID:");
    private final JLabeledTextField token = new JLabeledTextField("Token:");
    private final JLabeledTextField resourcePath = new JLabeledTextField("Resource Path(Topic):");
    
    private JLabeledChoice sampleOnCondition;
    private final JLabeledTextField sampleConditionValue = new JLabeledTextField("");
    
    private JCheckBox debugResponse = new JCheckBox("Debug response");
    private JCheckBox timestamp = new JCheckBox("Pub payload includes timestamp");
    private static final long serialVersionUID = -1715399546099472610L;

    public CoAPSubSamplerUI() {
        this.init();
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

        mainPanel.add(createSubOption());
    }
    
    public JPanel createMessageOptions() {
        JPanel optsPanelCon = new VerticalPanel();
        optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Message options"));
        
        JPanel optsPanel0 = new HorizontalPanel();
        coapMessageType = new JLabeledChoice("Type:", new String[] { "NON", "CON" }, false, false);
        coapMessageType.addChangeListener(this);
        
        methodType = new JLabeledChoice("Method:", new String[] { "GET" }, true, false);
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
    
    private JPanel createSubOption() {
        JPanel optsPanelCon = new VerticalPanel();
        
        optsPanelCon.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Sub options"));
        JPanel optsPanel1 = new HorizontalPanel();
        sampleOnCondition = new JLabeledChoice("Sample on:", new String[] {SAMPLE_ON_CONDITION_OPTION1, SAMPLE_ON_CONDITION_OPTION2});
        sampleOnCondition.addChangeListener(this);
        optsPanel1.add(sampleOnCondition);
        optsPanel1.add(sampleConditionValue);
        sampleOnCondition.setToolTipText("When sub sampler should report out.");
        sampleConditionValue.setToolTipText("Please specify an integer value great than 0, other values will be ignored.");
        optsPanelCon.add(optsPanel1);

        JPanel optsPanel2 = new HorizontalPanel();
        optsPanel2.add(timestamp);
        optsPanel2.add(debugResponse);
        optsPanelCon.add(optsPanel2);

        return optsPanelCon;
    }
    
    @Override
    public String getStaticLabel() {
        return "CoAP Sub Sampler";
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        if(this.sampleOnCondition == e.getSource()) {
            if(SAMPLE_ON_CONDITION_OPTION1.equalsIgnoreCase(sampleOnCondition.getText())) {
                sampleConditionValue.setText(DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_SEC);
            } else if(SAMPLE_ON_CONDITION_OPTION2.equalsIgnoreCase(sampleOnCondition.getText())) {
                sampleConditionValue.setText(DEFAULT_SAMPLE_VALUE_COUNT);
            }
        }
    }
    
    @Override
    public TestElement createTestElement() {
        CoAPSubSampler sampler = new CoAPSubSampler();
        this.setupSamplerProperties(sampler);
        return sampler;
    }
    
    @Override
    public void configure(TestElement element) {
        super.configure(element);
        CoAPSubSampler sampler = (CoAPSubSampler) element;
        commonUI.configure(sampler);
        
        this.coapMessageType.setText(sampler.getCoapMessageType());
        this.methodType.setText(sampler.getMethodType());
        this.messageId.setText(sampler.getMessageId());
        this.token.setText(sampler.getToken());
        this.resourcePath.setText(sampler.getResourcePath());

        this.timestamp.setSelected(sampler.isAddTimestamp());
        this.debugResponse.setSelected(sampler.isDebugResponse());
        this.sampleOnCondition.setText(sampler.getSampleCondition());

        if(SAMPLE_ON_CONDITION_OPTION1.equalsIgnoreCase(sampleOnCondition.getText())) {
            this.sampleConditionValue.setText(sampler.getSampleElapsedTime());
        } else if(SAMPLE_ON_CONDITION_OPTION2.equalsIgnoreCase(sampleOnCondition.getText())) {
            this.sampleConditionValue.setText(sampler.getSampleCount());
        }
    }

    @Override
    public String getLabelResource() {
        return "";
    }

    @Override
    public void modifyTestElement(TestElement arg0) {
        CoAPSubSampler sampler = (CoAPSubSampler) arg0;
        this.setupSamplerProperties(sampler);
    }

    private void setupSamplerProperties(CoAPSubSampler sampler) {
        this.configureTestElement(sampler);
        commonUI.setupSamplerProperties(sampler);

        sampler.setCoapMessageType(this.coapMessageType.getText());
        sampler.setMethodType(this.methodType.getText());
        sampler.setMessageId(this.messageId.getText());
        sampler.setToken(this.token.getText());
        sampler.setResourcePath(this.resourcePath.getText());
        
        sampler.setSampleCondition(this.sampleOnCondition.getText());
        sampler.setAddTimestamp(this.timestamp.isSelected());
        sampler.setDebugResponse(this.debugResponse.isSelected());
        
        if(SAMPLE_ON_CONDITION_OPTION1.equalsIgnoreCase(sampleOnCondition.getText())) {
            sampler.setSampleElapsedTime(this.sampleConditionValue.getText());
        } else if(SAMPLE_ON_CONDITION_OPTION2.equalsIgnoreCase(sampleOnCondition.getText())) {
            sampler.setSampleCount(this.sampleConditionValue.getText());
        }
    }
    
    @Override
    public void clearGui() {
        super.clearGui();
        commonUI.clearUI();
        commonUI.clientIdPrefix.setText(DEFAULT_CONN_PREFIX_FOR_SUB);
        
        this.coapMessageType.setText(DEFAULT_COAP_MESSAGE_TYPE);
        this.methodType.setText(DEFAULT_SUB_METHOD_TYPE);
        this.messageId.setText("");
        this.token.setText("");
        this.resourcePath.setText(DEFAULT_RESPURCE_PATH);

        this.sampleOnCondition.setText(SAMPLE_ON_CONDITION_OPTION1);
        this.sampleConditionValue.setText(DEFAULT_SAMPLE_VALUE_ELAPSED_TIME_SEC);
        this.timestamp.setSelected(false);
        this.debugResponse.setSelected(false);
    }
}
