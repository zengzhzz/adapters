package org.fiteagle.adapters.motor;

import org.fiteagle.abstractAdapter.AbstractAdapter;
import org.fiteagle.abstractAdapter.AbstractAdapter.AdapterException;
import org.fiteagle.abstractAdapter.AbstractAdapter.InstanceNotFoundException;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class MotorAdapterTest {
  
  private static AbstractAdapter adapter;
  
  @BeforeClass
  public static void setup() {
    adapter = MotorAdapter.adapterInstances.values().iterator().next();
  }
  
  @Test
  public void testCreateAndTerminate() throws AdapterException, InstanceNotFoundException {
    String instanceURI = adapter.getAdapterInstance().getNameSpace()+"InstanceOne";  
    
    Model modelCreate = ModelFactory.createDefaultModel();
    Resource motor = modelCreate.createResource(instanceURI);
    motor.addProperty(RDF.type, adapter.getAdapterManagedResource());
    Property propertyRPM = modelCreate.createProperty(adapter.getAdapterManagedResource().getNameSpace()+"rpm");
    motor.addLiteral(propertyRPM, 42);
    
    adapter.createInstances(modelCreate);
    
    Model createdResourceModel = adapter.getInstance(instanceURI);
    Resource resource = createdResourceModel.getResource(instanceURI);
    Assert.assertEquals(42, resource.getProperty(propertyRPM).getInt());

    adapter.deleteInstances(modelCreate);
    StmtIterator iter = adapter.getAllInstances().listStatements();
    Assert.assertFalse(iter.hasNext());
  }
  
  @Test(expected=InstanceNotFoundException.class)
  public void testGetNonExistingInstance() throws InstanceNotFoundException{
    String instanceURI = adapter.getAdapterInstance().getNameSpace()+"InstanceOne";  
    adapter.getInstance(instanceURI);
  }
  
  @Test
  public void testMonitor() throws AdapterException, InstanceNotFoundException {
    String instanceURI = adapter.getAdapterInstance().getNameSpace()+"InstanceOne";  
    
    Model modelCreate = ModelFactory.createDefaultModel();
    Resource motorResource = modelCreate.createResource(instanceURI);
    motorResource.addProperty(RDF.type, adapter.getAdapterManagedResource());
    adapter.createInstances(modelCreate);
    
    Model monitorData = adapter.getInstance(instanceURI);
    Assert.assertFalse(monitorData.isEmpty());
    Assert.assertTrue(monitorData.containsAll(modelCreate));
    
    adapter.deleteInstance(instanceURI);
  }
  
  @Test
  public void testGetters() {
    Assert.assertNotNull(adapter.getAdapterManagedResource());
    Assert.assertTrue(adapter.getAdapterManagedResource() instanceof Resource);
    Assert.assertNotNull(adapter.getAdapterInstance());
    Assert.assertTrue(adapter.getAdapterInstance() instanceof Resource);
    Assert.assertNotNull(adapter.getAdapterType());
    Assert.assertTrue(adapter.getAdapterInstance() instanceof Resource);
    Assert.assertNotNull(adapter.getAdapterDescriptionModel());
    Assert.assertTrue(adapter.getAdapterDescriptionModel() instanceof Model);
  }
  
  @Test
  public void testConfigure() throws AdapterException, InstanceNotFoundException {
    String instanceURI = adapter.getAdapterInstance().getNameSpace()+"InstanceOne";
    
    Model modelCreate = ModelFactory.createDefaultModel();
    Resource motorResource = modelCreate.createResource(instanceURI);
    motorResource.addProperty(RDF.type, adapter.getAdapterManagedResource());
    adapter.createInstances(modelCreate);

    Model modelConfigure = ModelFactory.createDefaultModel();
    Resource motor = modelConfigure.createResource(instanceURI);
    motor.addProperty(RDF.type, adapter.getAdapterManagedResource());
    Property propertyRPM = modelConfigure.createProperty(adapter.getAdapterManagedResource().getNameSpace()+"rpm");
    motor.addLiteral(propertyRPM, 23);
    Property propertyManufacturer = modelConfigure.createProperty(adapter.getAdapterManagedResource().getNameSpace()+"manufacturer");
    motor.addLiteral(propertyManufacturer, "TU Berlin");
    
    Model updatedResourceModel = adapter.configureInstances(modelConfigure);
    
    Resource updatedResource = updatedResourceModel.getResource(instanceURI);
    Assert.assertEquals(23, updatedResource.getProperty(propertyRPM).getInt());
    Assert.assertEquals("TU Berlin", updatedResource.getProperty(propertyManufacturer).getString());
    
    adapter.deleteInstance(instanceURI);
  }
  
}
