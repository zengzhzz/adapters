package org.fiteagle.adapters.openMTC;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fiteagle.abstractAdapter.AbstractAdapter;
import org.fiteagle.abstractAdapter.AdapterResource;
import org.fiteagle.adapters.openmtc.client.OpenMTCClient;
import org.fiteagle.api.core.IMessageBus;
import org.fiteagle.api.core.MessageBusOntologyModel;
import org.fiteagle.api.core.OntologyModels;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OpenMTCAdapter extends AbstractAdapter {

  private static final String[] ADAPTER_SPECIFIC_PREFIX = new String[2];
  private static final String[] ADAPTER_MANAGED_RESOURCE_PREFIX = new String[2];
  private final String[] ADAPTER_INSTANCE_PREFIX = new String[2];
  
  private OpenMTCClient openMTCClient;
  
  private static Resource adapter;
  private static Resource resource;
  public static List<Property> resourceInstanceProperties = new ArrayList<Property>();
  
  private Model adapterModel;
  private Resource adapterInstance;
  
  public static HashMap<String,OpenMTCAdapter> adapterInstances = new HashMap<>();
  
  public static OpenMTCAdapter getInstance(String URI){
    return adapterInstances.get(URI);
  }
  
  static {
    Model adapterModel = OntologyModels.loadModel("ontologies/openMTC.ttl", IMessageBus.SERIALIZATION_TURTLE);
    
    StmtIterator adapterIterator = adapterModel.listStatements(null, RDFS.subClassOf, MessageBusOntologyModel.classAdapter);
    if (adapterIterator.hasNext()) {
      adapter = adapterIterator.next().getSubject();
      ADAPTER_SPECIFIC_PREFIX[1] = adapter.getNameSpace();
      ADAPTER_SPECIFIC_PREFIX[0] = adapterModel.getNsURIPrefix(ADAPTER_SPECIFIC_PREFIX[1]);
    }
    
    StmtIterator resourceIterator = adapterModel.listStatements(adapter, MessageBusOntologyModel.propertyFiteagleImplements, (Resource) null);
    if (resourceIterator.hasNext()) {
      resource = resourceIterator.next().getObject().asResource();
      ADAPTER_MANAGED_RESOURCE_PREFIX[1] = resource.getNameSpace();
      ADAPTER_MANAGED_RESOURCE_PREFIX[0] = adapterModel.getNsURIPrefix(ADAPTER_MANAGED_RESOURCE_PREFIX[1]);
    }
    
    StmtIterator propertiesIterator = adapterModel.listStatements(null, RDFS.domain, resource);
    while (propertiesIterator.hasNext()) {
      Property p = adapterModel.getProperty(propertiesIterator.next().getSubject().getURI());
      resourceInstanceProperties.add(p);
    }
    
    //TODO: remove this creation of a static instance
    Resource adapterInstance = adapterModel.createResource("http://federation.av.tu-berlin.de/about#OpenMTC-1");
    adapterInstance.addProperty(RDF.type, adapter);
    adapterInstance.addProperty(RDFS.label, adapterModel.createLiteral("An OpenMTC Adapter instance"));
    adapterInstance.addProperty(RDFS.comment, adapterModel.createLiteral("An OpenMTC Adapter instance that can handle multiple OpenMTC as a Service instances", "en"));
    adapterInstance.addProperty(adapterModel.createProperty("http://open-multinet.info/ontology/omn#partOfGroup"),adapterModel.createResource("http://federation.av.tu-berlin.de/about#AV_Smart_Communication_Testbed"));
    
    OpenMTCAdapter openMTCAdapter = new OpenMTCAdapter(adapterInstance, adapterModel);
      
    adapterInstances.put(adapterInstance.getURI(), openMTCAdapter);
  }
  
  
  private OpenMTCAdapter(Resource adapterInstance, Model adapterModel){
    openMTCClient = OpenMTCClient.getInstance();
    
    this.adapterInstance = adapterInstance;
    this.adapterModel = adapterModel;
    
    ADAPTER_INSTANCE_PREFIX[1] = adapterInstance.getNameSpace();
    ADAPTER_INSTANCE_PREFIX[0] = adapterModel.getNsURIPrefix(ADAPTER_INSTANCE_PREFIX[1]);
  }
  
  @Override
  public Object handleCreateInstance(String instanceName, Map<String, String> properties) {
    openMTCClient.setUpConnection(properties);
    return null;
  }

  @Override
  public void handleTerminateInstance(String instanceName) {
    //TODO
  }
  
  @Override
  public String[] getAdapterSpecificPrefix() {
    return ADAPTER_SPECIFIC_PREFIX.clone();
  }
  
  @Override
  public String[] getAdapterManagedResourcePrefix() {
    return ADAPTER_MANAGED_RESOURCE_PREFIX.clone();
  }
  
  @Override
  public String[] getAdapterInstancePrefix() {
    return ADAPTER_INSTANCE_PREFIX.clone();
  }

  @Override
  public Model handleMonitorInstance(String instanceName, Model modelInstances) {
    AdapterResource openMTC = (AdapterResource) instanceList.get(instanceName);

    Resource instance = modelInstances.createResource(ADAPTER_INSTANCE_PREFIX[1]+instanceName);
    addPropertiesToResource(instance, openMTC, instanceName);

    return modelInstances;
  }

  @Override
  public Model handleGetAllInstances(Model modelInstances) {
    for(String key : instanceList.keySet()) {

      AdapterResource openMTC = (AdapterResource) instanceList.get(key);

      Resource openMTCInstance = modelInstances.createResource(key);
      addPropertiesToResource(openMTCInstance, openMTC, key);
    }
    return modelInstances;
  }
  
  @Override
  public void updateAdapterDescription(){
    //TODO
  }
  
  private void addPropertiesToResource(Resource openMTCInstance, AdapterResource openMTC, String instanceName) {
    openMTCInstance.addProperty(RDF.type, resource);
    openMTCInstance.addProperty(RDFS.label, instanceName);
    
    for(Property p : resourceInstanceProperties){
      if(openMTC.getProperty(p) != null){
        openMTCInstance.addLiteral(p, openMTC.getProperty(p));
      }
    }
  }

  @Override
  public List<String> configureInstance(Statement configureStatement) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Resource getAdapterManagedResource() {
    return resource;
  }

  @Override
  public Resource getAdapterInstance() {
    return adapterInstance;
  }

  @Override
  public Resource getAdapterType() {
    return adapter;
  }

  @Override
  public Model getAdapterDescriptionModel() {
    return adapterModel;
  }

}