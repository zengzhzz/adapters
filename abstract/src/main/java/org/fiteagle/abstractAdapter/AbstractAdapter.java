package org.fiteagle.abstractAdapter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import org.fiteagle.api.core.MessageBusMsgFactory;
import org.fiteagle.api.core.MessageBusOntologyModel;

import org.fiteagle.abstractAdapter.AdapterEventListener;

/**
 * Abstract class defining the basics all the current adapters are following Extend this class and implement the abstract methods to get this to work
 */
public abstract class AbstractAdapter {

    public static final String PARAM_TURTLE = "TURTLE";
    public static final String PARAM_RDFXML = "RDF/XML";
    public static final String PARAM_NTRIPLE = "N-TRIPLE";

    private List<AdapterEventListener> listener = new ArrayList<AdapterEventListener>();
    protected HashMap<String, Object> instanceList = new HashMap<String, Object>();

    protected Model modelGeneral = ModelFactory.createDefaultModel();
    protected Resource adapterInstance;
    
    public abstract Resource getAdapterManagedResource();

    public String getAdapterDescription(String serializationFormat) {
        return MessageBusMsgFactory.serializeModel(modelGeneral);
    }

    public Model getAdapterDescriptionModel() {    
        Model newModel = ModelFactory.createDefaultModel();
        newModel.add(modelGeneral);
        newModel.setNsPrefixes(modelGeneral.getNsPrefixMap());
        return newModel;
    }

    public boolean createInstance(String instanceName, String requestID) {

        if (instanceList.containsKey(instanceName)) {
            return false;
        }

        // handling done by adapter, handleCreateInstance has to be implemented by all subclasses!
        Object newInstance = handleCreateInstance(instanceName);

        instanceList.put(instanceName, newInstance);

        notifyListeners(createInformRDF(instanceName), requestID);

        return true;
    }

    public boolean terminateInstance(String instanceName, String requestID) {

        if (instanceList.containsKey(instanceName)) {
            // TODO: release event message
            
            notifyListeners(createInformReleaseRDF(instanceName), requestID);
            // notifyListeners(instanceList.get(instanceID), "terminated:"+ instanceID + ";; " + " (ID: " + instanceID + ")", "" + instanceID, "null");
            instanceList.remove(instanceName);
            return true;
        }

        return false;
    }

    public String monitorInstance(String instanceName, String serializationFormat) {
        Model modelInstances = getSingleInstanceModel(instanceName);
        return MessageBusMsgFactory.serializeModel(modelInstances);
    }

    private Model getSingleInstanceModel(String instanceName) {
        Model modelInstances = ModelFactory.createDefaultModel();

        setModelPrefixes(modelInstances);

        if (instanceList.containsKey(instanceName)) {
            // handling done by adapter, handleMonitorInstance has to be implemented by all subclasses!
            modelInstances = handleMonitorInstance(instanceName, modelInstances);
        }

        return modelInstances;
    }

    public String getAllInstances(String serializationFormat) {

        return MessageBusMsgFactory.serializeModel(getAllInstancesModel());
    }
    
    public Model getAllInstancesModel() {
        Model modelInstances = ModelFactory.createDefaultModel();

        setModelPrefixes(modelInstances);

        // handling done by adapter, handleGetAllInstances has to be implemented by all subclasses!
        modelInstances = handleGetAllInstances(modelInstances);

        return modelInstances;
    }
    

    public void setModelPrefixes(Model model) {
        model.setNsPrefix("", "http://fiteagleinternal#");
        model.setNsPrefix(getAdapterSpecificPrefix()[0], getAdapterSpecificPrefix()[1]);
        model.setNsPrefix("fiteagle", "http://fiteagle.org/ontology#");
        model.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        model.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        model.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
    }
    
    public String getDiscoverAll(String serializationFormat){
        Model modelDiscover = getAdapterDescriptionModel();
        modelDiscover.add(getAllInstancesModel());
        
        return MessageBusMsgFactory.serializeModel(modelDiscover);
    }

    public String configureInstance(String controlInput, String serializationFormat, String requestID) {

        // create an empty model
        Model configureModel = ModelFactory.createDefaultModel();

        InputStream is = new ByteArrayInputStream(controlInput.getBytes());

        // read the RDF/XML file
        configureModel.read(is, null, serializationFormat);

        // handling done by adapter, handleControlInstance has to be implemented by all subclasses!handleControlInstance
        return handleConfigureInstance(configureModel, requestID);
    }
    
    public String configureInstance(Model configureModel, String requestID) {
        // handling done by adapter, handleControlInstance has to be implemented by all subclasses!
        return handleConfigureInstance(configureModel, requestID);
    }

    public void notifyListeners(Model eventRDF, String requestID) {
        for (AdapterEventListener name : listener) {
            name.rdfChange(eventRDF, requestID);
        }
    }

    public Model createInformRDF(String instanceName) {
        return getSingleInstanceModel(instanceName);
    }
    
    public Model createInformConfigureRDF(String instanceName, List<String> propertiesChanged) {
        Model modelPropertiesChanged = ModelFactory.createDefaultModel();
        setModelPrefixes(modelPropertiesChanged);
        
        Model wholeInstance = getSingleInstanceModel(instanceName);
        Resource currentInstance = wholeInstance.getResource("http://fiteagleinternal#" + instanceName);
        
        for (String currentPropertyString : propertiesChanged) {             
            Property currentProperty = wholeInstance.getProperty(getAdapterSpecificPrefix()[1] + currentPropertyString);
            StmtIterator iter2 = currentInstance.listProperties(currentProperty);
            Statement stmtToAdd = iter2.nextStatement();            
            modelPropertiesChanged.add(stmtToAdd);            
        }       
        
        return modelPropertiesChanged;
    }
    
    public Model createInformReleaseRDF(String instanceName) {
        
        Model modelInstances = ModelFactory.createDefaultModel();

        setModelPrefixes(modelInstances);
        
        Resource releaseInstance = modelInstances.createResource("http://fiteagleinternal#" + instanceName);
        modelInstances.add(adapterInstance, MessageBusOntologyModel.methodReleases, releaseInstance);
        
        return modelInstances;
    }
    

    public boolean addChangeListener(AdapterEventListener newListener) {
        listener.add(newListener);
        return true;
    }
    
    public void registerAdapter(){
        notifyListeners(getAdapterDescriptionModel(), "0");
    }

    public void deregisterAdapter(){
     //   Model messageModel = MessageBusMsgFactory.createMsgRelease();
     //   messageModel.add(adapterInstance.getProperty(RDF.type));  
        
        Model messageModel = ModelFactory.createDefaultModel();
        messageModel.add(adapterInstance, MessageBusOntologyModel.methodReleases, adapterInstance);
  
        notifyListeners(messageModel, "0");
    }
    
    /**
     * Needs to return a new instance of the class this adapter is supposed to handle
     * 
     * @return Object - the newly created instance
     */
    public abstract Object handleCreateInstance(String instanceName);


    /**
     * Returns a String containing the prefix that should be applied for the case "" for this specific adapter e.g. http://fiteagle.org/ontology/adapter/motor#
     * 
     * @return String containing the prefix to be applied
     */
    public abstract String[] getAdapterSpecificPrefix();

    /**
     * Handles the monitoring of resources specifically for this adapter
     */
    public abstract Model handleMonitorInstance(String instanceName, Model modelInstances);

    /**
     * Handles the getting of all instances for this adapter
     */
    public abstract Model handleGetAllInstances(Model modelInstances);

    
    public abstract String handleConfigureInstance(Model configureModel, String requestID);
   
    

}
