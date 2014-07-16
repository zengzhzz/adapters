package org.fiteagle.adapters.mightyrobot;

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;



import org.fiteagle.abstractAdapter.AbstractAdapter;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
//import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.SimpleSelector;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.sparql.modify.UpdateProcessRemote;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateRequest;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;
import org.fiteagle.adapters.mightyrobot.dm.MightyRobotAdapterBean;
import org.fiteagle.adapters.mightyrobot.dm.MightyRobotAdapterEJB;

import javax.ejb.*;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.jms.*;

import javax.annotation.Resource;
import javax.naming.InitialContext;

@ApplicationScoped
public class MightyRobotAdapter extends AbstractAdapter{

	private String adapterSpecificPrefix = "http://fiteagle.org/ontology/adapter/mightyrobot#";
    private static MightyRobotAdapter mightyRobotAdapterSingleton; 
    
    private com.hp.hpl.jena.rdf.model.Resource instanceClassResource;
    private String instanceClassResourceString = "MightyRobotResource";
    private com.hp.hpl.jena.rdf.model.Resource adapterResource;
    private String adapterResourceString = "MightyRobotAdapter";

    private Property mightyRobotPropertyDancing;
    private Property mightyRobotPropertyExploded;
    private Property mightyRobotPropertyHeadRotation;
    private Property mightyRobotPropertyNickname;    
    
    private List<Property> adapterControlProperties = new LinkedList<Property>();

    /*public static synchronized MightyRobotAdapter getInstance()
    { 
      if ( mightyRobotAdapterSingleton == null ) 
    	  mightyRobotAdapterSingleton = new MightyRobotAdapter();
      return mightyRobotAdapterSingleton;
    } */
    
    public MightyRobotAdapter() {
        modelGeneral = ModelFactory.createDefaultModel();

        modelGeneral.setNsPrefix("", this.getAdapterSpecificPrefix());
        modelGeneral.setNsPrefix("fiteagle", "http://fiteagle.org/ontology#");
        modelGeneral.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
        modelGeneral.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
        modelGeneral.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
        modelGeneral.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");

        // Property instantiatesProperty = new PropertyImpl("fiteagle:instantiates");

        instanceClassResource = modelGeneral.createResource(this.getAdapterSpecificPrefix() +  instanceClassResourceString);
        instanceClassResource.addProperty(RDF.type, OWL.Class);
        instanceClassResource.addProperty(RDFS.subClassOf, modelGeneral.createResource("http://fiteagle.org/ontology#Resource"));

        adapterResource = modelGeneral.createResource(this.getAdapterSpecificPrefix() +  adapterResourceString);
        adapterResource.addProperty(RDF.type, OWL.Class);
        adapterResource.addProperty(RDFS.subClassOf, modelGeneral.createResource("http://fiteagle.org/ontology#Adapter"));
        adapterResource.addProperty(modelGeneral.createProperty("http://fiteagle.org/ontology#instantiates"), instanceClassResource);

        // create the properties
        /*
        mightyRobotPropertyDancing = modelGeneral.createProperty(this.getAdapterSpecificPrefix() +  "dancing");
        mightyRobotPropertyDancing.addProperty(RDF.type, OWL.DatatypeProperty);
        mightyRobotPropertyDancing.addProperty(RDFS.domain, instanceClassResource);
        mightyRobotPropertyDancing.addProperty(RDFS.range, XSD.xboolean);
        adapterControlProperties.add(mightyRobotPropertyDancing);        

        mightyRobotPropertyExploded = modelGeneral.createProperty(this.getAdapterSpecificPrefix() +  "exploded");
        mightyRobotPropertyExploded.addProperty(RDF.type, OWL.DatatypeProperty);
        mightyRobotPropertyExploded.addProperty(RDFS.domain, instanceClassResource);
        mightyRobotPropertyExploded.addProperty(RDFS.range, XSD.xboolean);
        adapterControlProperties.add(mightyRobotPropertyExploded);        

        mightyRobotPropertyHeadRotation = modelGeneral.createProperty(this.getAdapterSpecificPrefix() +  "headRotation");
        mightyRobotPropertyHeadRotation.addProperty(RDF.type, OWL.DatatypeProperty);
        mightyRobotPropertyHeadRotation.addProperty(RDFS.domain, instanceClassResource);
        mightyRobotPropertyHeadRotation.addProperty(RDFS.range, XSD.integer);
        adapterControlProperties.add(mightyRobotPropertyHeadRotation);         
 
        mightyRobotPropertyNickname = modelGeneral.createProperty(this.getAdapterSpecificPrefix() +  "nickname");
        mightyRobotPropertyNickname.addProperty(RDF.type, OWL.DatatypeProperty);
        mightyRobotPropertyNickname.addProperty(RDFS.domain, instanceClassResource);
        mightyRobotPropertyNickname.addProperty(RDFS.range, XSD.xstring);
        adapterControlProperties.add(mightyRobotPropertyNickname); 
        */          

        mightyRobotPropertyDancing = generateProperty(modelGeneral.createProperty(this.getAdapterSpecificPrefix() +  "dancing"), XSD.xboolean);
        adapterControlProperties.add(mightyRobotPropertyDancing);

        mightyRobotPropertyExploded = generateProperty(modelGeneral.createProperty(this.getAdapterSpecificPrefix() +  "exploded"), XSD.xboolean);
        adapterControlProperties.add(mightyRobotPropertyExploded);

        mightyRobotPropertyHeadRotation = generateProperty(modelGeneral.createProperty(this.getAdapterSpecificPrefix() +  "headRotation"), XSD.integer);
        adapterControlProperties.add(mightyRobotPropertyHeadRotation);

        mightyRobotPropertyNickname = generateProperty(modelGeneral.createProperty(this.getAdapterSpecificPrefix() +  "nickname"), XSD.xstring);
        adapterControlProperties.add(mightyRobotPropertyNickname);

        com.hp.hpl.jena.rdf.model.Resource individualMightyRobotAdapter1 = modelGeneral.createResource(this.getAdapterSpecificPrefix() +  "individualMightyRobotAdapter1");
        individualMightyRobotAdapter1.addProperty(RDF.type, adapterResource);
        individualMightyRobotAdapter1.addProperty(RDFS.label, modelGeneral.createLiteral("MightyRobot Adapter 1", "en"));
        individualMightyRobotAdapter1.addProperty(RDFS.comment, modelGeneral.createLiteral("A MightyRobot Adapter 1", "en"));
        System.out.println(instanceClassResource);
        System.out.println(adapterResource);
        System.out.println(mightyRobotPropertyDancing);

    }
    
    private Property generateProperty(Property template, com.hp.hpl.jena.rdf.model.Resource XSDType){
    	template.addProperty(RDF.type, OWL.DatatypeProperty);
    	template.addProperty(RDFS.domain, instanceClassResource);
    	template.addProperty(RDFS.range, XSDType);
    	return template;
    }


   /* @Resource(mappedName = "topic/core")
    private Topic topic;*/
    @Inject
    private JMSContext context;
    @Inject
    private MightyRobotAdapterBean mightyRobotAdapterBean;


    @Override
    public Object handleCreateInstance(int instanceID){

        try{
            final Message message= this.context.createMessage();
            message.setStringProperty("request",
                    "resources");
             mightyRobotAdapterBean.sendMessage(message);


        }catch(Exception e){
            System.out.println("ERROR" + e.toString());
            e.printStackTrace();
        }

        MightyRobot temp = new MightyRobot(this, instanceID);
        postSparqlEntry(temp);
        return temp;
    }
   
    private boolean postSparqlEntry(MightyRobot currentRobot){
/* 
Post the entry
INSERT DATA { 
	<http://fiteagle.org/ontology/adapter/mightyrobot#MightyRobotResource> 
		<http://fiteagle.org/ontology/adapter/mightyrobot#InstanceID> "1" ; 
		<http://fiteagle.org/ontology/adapter/mightyrobot#dancing> "false" ; 
		<http://fiteagle.org/ontology/adapter/mightyrobot#exploded> "false" ; 
		<http://fiteagle.org/ontology/adapter/mightyrobot#headRotation> "0" ; 
		<http://fiteagle.org/ontology/adapter/mightyrobot#nickname> "Mecha" ; 
		<http://fiteagle.org/ontology/adapter/mightyrobot#owningAdapter> "org.fiteagle.adapters.mightyrobot.MightyRobotAdapter@13ad3f4" .
} 

Get the entry
PREFIX mr: <http://fiteagle.org/ontology/adapter/mightyrobot#>
SELECT ?InstanceID ?dancing ?exploded ?headRotation ?nickname ?owningAdapter
WHERE
{
	?mightyrobot mr:InstanceID ?InstanceID .
	?mightyrobot mr:dancing ?dancing .
	?mightyrobot mr:exploded ?exploded .
	?mightyrobot mr:headRotation ?headRotation .
	?mightyrobot mr:nickname ?nickname .
	?mightyrobot mr:owningAdapter ?owningAdapter .
}

Change the nickname
PREFIX mr: <http://fiteagle.org/ontology/adapter/mightyrobot#>
DELETE { ?mightyrobot mr:nickname "Mecha" }
INSERT { ?mightyrobot mr:nickname "Toni" }
WHERE { ?mightyrobot mr:nickname "Mecha" } 

Delete the entry
DELETE
{
?mightyrobot ?property ?value 
}
WHERE
{
?mightyrobot ?property ?value ; <http://fiteagle.org/ontology/adapter/mightyrobot#InstanceID> "1"
}	
*/		
    	
    	try {

			String postString = 
					"INSERT DATA { <"+ instanceClassResource + currentRobot.getInstanceID() + ">" 
					+ " \n<" + getAdapterSpecificPrefix() + "InstanceID> \"" + currentRobot.getInstanceID() + "\" ;"
					+ " \n<" + mightyRobotPropertyDancing + "> \"" + currentRobot.getDancing() + "\" ;"
					+ " \n<" + mightyRobotPropertyExploded + "> \"" + currentRobot.getExploded() + "\" ;"
					+ " \n<" + mightyRobotPropertyHeadRotation + "> \"" + currentRobot.getHeadRotation() + "\" ;"
					+ " \n<" + mightyRobotPropertyNickname + "> \"" + currentRobot.getNickname() + "\" ;"
	                + " \n<" + getAdapterSpecificPrefix() + "owningAdapter> \"" + currentRobot.getOwningAdapter() + "\" .}";			
	 
			String updateURL = "http://localhost:3030/ds/update";
			System.out.println("Posting \n\n" + postString + "\n\nto " + updateURL);
			UpdateRequest updateRequest = UpdateFactory.create(postString);
			UpdateProcessRemote uPR = (UpdateProcessRemote) 
					UpdateExecutionFactory.createRemote(updateRequest, updateURL);
			uPR.execute();
			return true;
 		} catch (Exception e){
 			e.printStackTrace();
 		}
 		return false;
    }
    
    @Override
    public void handleTerminateInstance(int instanceID){
/*
Delete the entry
DELETE
{
?mightyrobot ?property ?value 
}
WHERE
{
?mightyrobot ?property ?value ; <http://fiteagle.org/ontology/adapter/mightyrobot#InstanceID> "5"
}	
 */ 
    	try {
	    	String postString = 
	    			"DELETE { ?mightyrobot ?property ?value } \n" +
	    					"WHERE { ?mightyrobot ?property ?value ; " +
	    					"<http://fiteagle.org/ontology/adapter/mightyrobot#InstanceID> \"" +
	    					instanceID + 
	    					"\"}";

			String updateURL = "http://localhost:3030/ds/update";
			System.out.println("Posting \n\n" + postString + "\n\nto " + updateURL);
			UpdateRequest updateRequest = UpdateFactory.create(postString);
			UpdateProcessRemote uPR = (UpdateProcessRemote) 
					UpdateExecutionFactory.createRemote(updateRequest, updateURL);
			uPR.execute();
    	} catch (Exception e){
    		e.printStackTrace();
    	}
    }
    
    @Override
    public Model handleMonitorInstance(int instanceID, Model modelInstances){
    	MightyRobot currentMightyRobot = (MightyRobot) instanceList.get(instanceID);

        com.hp.hpl.jena.rdf.model.Resource mightyRobotInstance = modelInstances.createResource(this.getAdapterSpecificPrefix() +  "m" + instanceID);
        mightyRobotInstance.addProperty(RDF.type, instanceClassResource);
        mightyRobotInstance.addProperty(RDFS.label, "" + instanceID);
        mightyRobotInstance.addProperty(RDFS.comment, modelGeneral.createLiteral("MightyRobot in da house " + instanceID, "en"));

        mightyRobotInstance.addLiteral(mightyRobotPropertyDancing, currentMightyRobot.getDancing());
        mightyRobotInstance.addLiteral(mightyRobotPropertyExploded, currentMightyRobot.getExploded());
        mightyRobotInstance.addLiteral(mightyRobotPropertyHeadRotation, currentMightyRobot.getHeadRotation());
        mightyRobotInstance.addLiteral(mightyRobotPropertyNickname, currentMightyRobot.getNickname());       

        return modelInstances;
    }

	@Override
	public Model handleGetAllInstances(Model modelInstances) {
		for (Integer key : instanceList.keySet()) {
        	
        	MightyRobot currentMightyRobot= (MightyRobot) instanceList.get(key);

            com.hp.hpl.jena.rdf.model.Resource mightyRobotInstance = modelInstances.createResource(this.getAdapterSpecificPrefix() +  "m" + key);
            mightyRobotInstance.addProperty(RDF.type, instanceClassResource);
            mightyRobotInstance.addProperty(RDFS.label, "" + key);
            mightyRobotInstance.addProperty(RDFS.comment, modelGeneral.createLiteral("MightyRobot in da house " + key, "en"));

            mightyRobotInstance.addLiteral(mightyRobotPropertyDancing, currentMightyRobot.getDancing());
            mightyRobotInstance.addLiteral(mightyRobotPropertyExploded, currentMightyRobot.getExploded());
            mightyRobotInstance.addLiteral(mightyRobotPropertyHeadRotation, currentMightyRobot.getHeadRotation());
            mightyRobotInstance.addLiteral(mightyRobotPropertyNickname, currentMightyRobot.getNickname());  
        }
		return modelInstances;
	}

	@Override
	public String handleControlInstance(Model model2) {
        StringWriter sw = new StringWriter();

        StmtIterator iter = model2.listStatements(new SimpleSelector(null, RDF.type, instanceClassResource));
        while (iter.hasNext()) {
            com.hp.hpl.jena.rdf.model.Resource currentResource = iter.nextStatement().getSubject();
            // sw.write(currentResource.getProperty(RDFS.label).getObject().toString());
            int key = Integer.parseInt(currentResource.getProperty(RDFS.label).getObject().toString());
            if (instanceList.containsKey(key)) {
            	MightyRobot currentMightyRobot = (MightyRobot) instanceList.get(key);

                for (Property currentProperty : adapterControlProperties) {
                    StmtIterator iter2 = currentResource.listProperties(currentProperty);

                    while (iter2.hasNext()) {

                    	String newValue = "";
                        if (currentProperty == mightyRobotPropertyDancing) {
                        	
                        	boolean value = iter2.nextStatement().getObject().asLiteral().getBoolean();
                        	currentMightyRobot.setDancing(value);
                        	newValue = value + "";
                        	
                        } else if (currentProperty == mightyRobotPropertyExploded) {
                        	
                        	boolean value = iter2.nextStatement().getObject().asLiteral().getBoolean();
                        	currentMightyRobot.setExploded(value);
                        	newValue = value + "";
                        	 
                        } else if (currentProperty == mightyRobotPropertyHeadRotation) {
                        	
                        	int value = (int) iter2.nextStatement().getObject().asLiteral().getLong();
                        	currentMightyRobot.setHeadRotation(value);
                        	newValue = value + "";
                        	
                        } else if (currentProperty == mightyRobotPropertyNickname) {

                        	String value = iter2.nextStatement().getObject().asLiteral().getString();
                        	currentMightyRobot.setNickname(value);
                        	newValue = value + "";
                        	
                        }

                        sw.write("Changed mightyrobot instance " + key + " property " + currentProperty.toString() + " to value " + newValue + "\n");
                    }
                }

            }
        }

        return sw.toString(); 
	}
	
	@Override
	public String getInstanceClassName() {
		return MightyRobot.class.getName();
	}

	@Override
	public String getAdapterSpecificPrefix() {
		return adapterSpecificPrefix;
	}

}
