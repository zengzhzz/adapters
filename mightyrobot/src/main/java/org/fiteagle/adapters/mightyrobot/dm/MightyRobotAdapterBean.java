package org.fiteagle.adapters.mightyrobot.dm;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MightyRobotAdapterBean {
/*
	@Inject
    private JMSContext jmsContext;
	//@Resource(mappedName = "java:/topic/core") //todo: get from api package
    @Resource(mappedName = "topic/core")
    private Topic topic;
	private static final Logger LOGGER = Logger.getLogger(MightyRobotAdapterBean.class
            .getName());

	public void sendMessage(Message message) throws JMSException {
		LOGGER.log(Level.INFO, "Submitting request to JMS...");
        System.out.println("Submitting request to JMS...");
		jmsContext.createProducer().send(topic, message);
	}*/
//
//	public Message createMessage() {
//		if (null == jmsContext)
//			LOGGER.log(Level.SEVERE, "jms context was not injected!");
//		return jmsContext.createMessage();
//	}
//
//	public void sendMessage(String string) throws JMSException {
//		if (null == jmsContext)
//			LOGGER.log(Level.SEVERE, "jms context was not injected!");
//		sendMessage(jmsContext.createTextMessage(string));
//	}
}
