package org.fiteagle.adapters.ACSclient;

import info.openmultinet.ontology.vocabulary.Omn_service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.fiteagle.abstractAdapter.AbstractAdapter;
import org.fiteagle.abstractAdapter.AdapterControl;
import org.fiteagle.abstractAdapter.dm.IAbstractAdapter;
import org.fiteagle.adapters.ACSclient.dm.ACSclientAdapterMDBSender;
import org.fiteagle.api.core.IMessageBus;
import org.fiteagle.api.core.OntologyModelUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

@Singleton
@Startup
public class ACSclientAdapterControl extends AdapterControl{
  
  @Inject
  protected ACSclientAdapterMDBSender mdbSender;
  
  private static final Logger LOGGER = Logger.getLogger(ACSclientAdapterControl.class.getName());
  
  private static final String URL= "url";
  private static final String DEVICE = "device";
  private static final String PARAMETER = "parameter";
  
  @PostConstruct
  public void initialize() {
	  propertiesName= "ACSclient";
    LOGGER.log(Level.INFO, "Starting ACSclientAdapter");
    this.adapterModel = OntologyModelUtil.loadModel("ontologies/ACSclient.ttl", IMessageBus.SERIALIZATION_TURTLE);
    this.adapterInstancesConfig = this.readConfig(propertiesName);
    this.createAdapterInstances();
    this.publishInstances();

  }
  
  
  @Override
  public AbstractAdapter createAdapterInstance(final Model tbox, final Resource abox) {
    final AbstractAdapter adapter = new ACSclientAdapter(tbox, abox);
    this.adapterInstances.put(adapter.getId(), adapter);
    return adapter;
  }
  
  @Override
  protected void parseConfig() {
    final String jsonProperties = this.adapterInstancesConfig.readJsonProperties();
    if (!jsonProperties.isEmpty()) {
      final JsonReader jsonReader = Json
      .createReader(new ByteArrayInputStream(jsonProperties.getBytes(StandardCharsets.UTF_8)));

      final JsonObject jsonObject = jsonReader.readObject();
      final JsonArray adaptInstances = jsonObject.getJsonArray(IAbstractAdapter.ADAPTER_INSTANCES);
      
      for (int i = 0; i < adaptInstances.size(); i++) {
        final JsonObject adaptInstObject = adaptInstances.getJsonObject(i);
        final String adapterInstance = adaptInstObject.getString(IAbstractAdapter.COMPONENT_ID);
        
        if (!adapterInstance.isEmpty()) {
          final Model model = ModelFactory.createDefaultModel();
          final Resource resource = model.createResource(adapterInstance);
          // parse possible additional values from config
          
          String url = adaptInstObject.getString(URL);
          if(url != null){
            resource.addProperty(model.createProperty(Omn_service.getURI(), URL), url);
          }
          
          String device = adaptInstObject.getString(DEVICE);
          if(device != null){
            resource.addProperty(model.createProperty(Omn_service.getURI(), DEVICE), device);
          }
          
          String param = adaptInstObject.getString(PARAMETER);
          if(param != null){
            resource.addProperty(model.createProperty(Omn_service.getURI(), PARAMETER), param);
          }
          
          this.createAdapterInstance(this.adapterModel, resource);
          }
        }
      }
    }

  @Override
  @SuppressWarnings("PMD.GuardLogStatementJavaUtil")
  protected void addAdapterProperties(final Map<String, String> adaptInstance) {
    LOGGER.warning("Not implemented. Input: " + adaptInstance);
  }
  
  
}
