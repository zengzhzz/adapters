package org.fiteagle.adapters.tosca.client;

import info.openmultinet.ontology.translators.tosca.jaxb.Definitions;

import org.fiteagle.abstractAdapter.AbstractAdapter.InstanceNotFoundException;
import org.fiteagle.abstractAdapter.AbstractAdapter.InvalidRequestException;
import org.fiteagle.abstractAdapter.AbstractAdapter.ProcessingException;

public interface IToscaClient {
  
  public abstract Definitions getAllDefinitions() throws ProcessingException;
  
  public abstract Definitions getAllTypes() throws ProcessingException;
  
  public abstract Definitions getDefinitions(String id) throws InstanceNotFoundException, ProcessingException;
  
  public abstract Definitions getSingleNodeDefinitions(String id) throws InstanceNotFoundException, ProcessingException;
  
  public abstract void deleteDefinitions(String id) throws ProcessingException;
  
  public abstract Definitions updateDefinitions(String id, String definitionsString) throws ProcessingException;
  
  public abstract Definitions createDefinitions(String definitionsString) throws ProcessingException, InvalidRequestException;
  
}