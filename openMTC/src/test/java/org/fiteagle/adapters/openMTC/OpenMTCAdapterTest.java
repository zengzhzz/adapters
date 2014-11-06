package org.fiteagle.adapters.openMTC;

import static org.junit.Assert.*;

import java.util.Iterator;

import org.fiteagle.api.core.MessageBusOntologyModel;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OpenMTCAdapterTest {
  
  private static OpenMTCAdapter adapter;
  
  @BeforeClass
  public static void loadModel(){
    Iterator<String> iter = OpenMTCAdapter.adapterInstances.keySet().iterator();
    if(iter.hasNext()){
        adapter = OpenMTCAdapter.getInstance(iter.next());
    }
  }
  
  @Test
  public void testAdapterModel() {
    assertTrue(adapter.getAdapterDescriptionModel().contains(adapter.getAdapterType(), RDFS.subClassOf, MessageBusOntologyModel.classAdapter));
  }
  
  @Test
  public void testAdapterPrefixes() {
    assertNotNull(adapter.getAdapterSpecificPrefix()[0]);
    assertNotNull(adapter.getAdapterManagedResourcePrefix()[0]);
    assertNotNull(adapter.getAdapterInstancePrefix()[0]);
    assertNotNull(adapter.getAdapterSpecificPrefix()[1]);
    assertNotNull(adapter.getAdapterManagedResourcePrefix()[1]);
    assertNotNull(adapter.getAdapterInstancePrefix()[1]);
  }
  
  @Test
  public void testAdapterResource() {
    assertTrue(adapter.getAdapterManagedResource().hasProperty(MessageBusOntologyModel.propertyFiteagleImplementedBy, adapter.getAdapterType()));
    assertTrue(adapter.getAdapterManagedResource().hasProperty(RDFS.subClassOf, MessageBusOntologyModel.classResource));
  }

  @Test
  public void testAdapterType() {
      assertTrue(adapter.getAdapterType().hasProperty(RDFS.subClassOf, MessageBusOntologyModel.classAdapter));
      assertTrue(adapter.getAdapterType().hasProperty(MessageBusOntologyModel.propertyFiteagleImplements, adapter.getAdapterManagedResource()));
  }
  
  @Test
  public void testAdapterInstance() {
      assertTrue(adapter.getAdapterInstance().hasProperty(RDF.type, adapter.getAdapterType()));
      assertTrue(adapter.getAdapterInstance().hasProperty(RDFS.comment));
      assertTrue(adapter.getAdapterInstance().hasProperty(RDFS.label)); 
  }
  
}