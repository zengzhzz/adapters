package org.fiteagle.abstractAdapter.dm;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Topic;

import org.fiteagle.abstractAdapter.AbstractAdapter;
import org.fiteagle.abstractAdapter.AdapterEventListener;
import org.fiteagle.api.core.IMessageBus;
import org.fiteagle.api.core.MessageBusOntologyModel;
import org.fiteagle.api.core.MessageUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.RDF;

public abstract class AbstractAdapterMDBSender {
    
    private static Logger LOGGER = Logger.getLogger(AbstractAdapterMDBSender.class.toString());
    
    @Inject
    private JMSContext context;
    @javax.annotation.Resource(mappedName = IMessageBus.TOPIC_CORE_NAME)
    private Topic topic;

    @PostConstruct
    public void initializeAdapter() {
      LOGGER.log(Level.INFO, this.getClass().getSimpleName() + ": Adding change listener for " + getAdapter().getAdapterInstance().getURI());
      getAdapter().addChangeListener(new AdapterEventListener() {

        @Override
        public void publishModelUpdate(Model eventRDF, String requestID) {
            sendInformMessage(eventRDF, requestID);                
        }
      });
      
      LOGGER.log(Level.INFO, this.getClass().getSimpleName() + ": Registering " + getAdapter().getAdapterInstance().getURI());
      getAdapter().registerAdapter();
      
      LOGGER.log(Level.INFO, this.getClass().getSimpleName() + ": Sending restore request message for " + getAdapter().getAdapterInstance().getURI());
      try {
        sendRestoreRequestMessage();
      } catch (JMSException e) {
        LOGGER.log(Level.SEVERE, e.getMessage());
      }
    }
    
    private void sendInformMessage(Model model, String requestID) {
      model.add(MessageBusOntologyModel.internalMessage, RDF.type, MessageBusOntologyModel.propertyFiteagleInform);
      final Message message = MessageUtil.createRDFMessage(model, IMessageBus.TYPE_INFORM, IMessageBus.SERIALIZATION_DEFAULT, requestID, context);
      
      context.createProducer().send(topic, message);
    }
    
    public void sendRestoreRequestMessage() throws JMSException {
      String query = "DESCRIBE ?resource "
          + "WHERE {?resource a <"+getAdapter().getAdapterManagedResource().getURI()+"> .  }";
      
      String requestModel = MessageUtil.createSerializedSPARQLQueryRestoresModel(query, getAdapter().getAdapterInstance());
      final Message request = MessageUtil.createRDFMessage(requestModel, IMessageBus.TYPE_REQUEST, IMessageBus.SERIALIZATION_TURTLE, null, context);
      context.createProducer().send(topic, request);
    }
    
    @PreDestroy
    public void contextDestroyed() {
      getAdapter().deregisterAdapter();
    }
    
    protected abstract AbstractAdapter getAdapter();
}
