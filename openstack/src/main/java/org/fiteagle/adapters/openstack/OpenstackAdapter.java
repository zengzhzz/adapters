package org.fiteagle.adapters.openstack;

import com.hp.hpl.jena.rdf.model.*;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.woorea.openstack.nova.model.*;

import info.openmultinet.ontology.vocabulary.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Base64;

import org.fiteagle.abstractAdapter.AbstractAdapter;
import org.fiteagle.abstractAdapter.dm.AdapterEventListener;
import org.fiteagle.adapters.openstack.client.IOpenstackClient;
import org.fiteagle.adapters.openstack.client.OpenstackClient;
import org.fiteagle.adapters.openstack.client.model.Image;
import org.fiteagle.adapters.openstack.client.model.Images;
import org.fiteagle.adapters.openstack.client.model.Server;
import org.fiteagle.adapters.openstack.client.model.ServerForCreate;
import org.fiteagle.adapters.openstack.client.model.Servers;
import org.fiteagle.adapters.openstack.dm.OpenstackAdapterMDBSender;
import org.fiteagle.api.core.Config;
import org.fiteagle.api.core.IMessageBus;
import org.fiteagle.api.core.MessageBusOntologyModel;
import org.fiteagle.api.core.MessageUtil;
import org.fiteagle.api.core.OntologyModelUtil;

import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import javax.ejb.EJB;
import javax.enterprise.concurrent.ManagedThreadFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class OpenstackAdapter extends AbstractAdapter {
	
  Logger LOGGER = Logger.getLogger(this.getClass().getName());

  protected IOpenstackClient openstackClient;

  private OpenstackAdapterMDBSender listener;

	@EJB
	OpenstackAdapterControl openstackAdapterControler;
	
	private Map<String,ArrayList<String>> defaultFlavours = openstackAdapterControler.instancesDefaultFlavours.get(this.uuid);

	
	

private String floatingPool;
  private String keystone_auth_URL;
  private String net_name;
  private String nova_endpoint;
  private String keystone_password;
  private String keystone_endpoint;
  private String glance_endpoint;
  private String net_endpoint;
  private String tenant_name;
  private String keystone_username;
  private String default_flavor_id;
  private String default_image_id;


  
/*  private static void createDefaultAdapterInstance(Model adapterModel){
 //   Resource adapterInstance = adapterTBox.createResource(OntologyModelUtil.getResourceNamespace()+"Openstack-1");

//    adapter = adapterModel.createResource(Omn_domain_pc.VMServer);
//    adapter.addProperty(RDFS.subClassOf, MessageBusOntologyModel.classAdapter);
//    adapter.addProperty(Omn_lifecycle.implements_,Omn_domain_pc.VM);

    Resource adapterInstance = ModelFactory.createDefaultModel().createResource(OntologyModelUtil.getResourceNamespace()+"VMServer-1");
    adapterInstance.addProperty(Omn_lifecycle.childOf, ModelFactory.createDefaultModel().createResource("http://openstack.av.tu-berlin.de"));
    adapterInstance.addProperty(RDF.type, Omn_domain_pc.VMServer);
    adapterInstance.addProperty(RDF.type, OWL2.NamedIndividual);
    adapterInstance.addProperty(RDFS.comment, "An openstack vm server that can handle different VMs.");
    
    Resource testbed = adapterModel
        .createResource("http://federation.av.tu-berlin.de/about#AV_Smart_Communication_Testbed");
    adapterInstance.addProperty(Omn_federation.partOfFederation, testbed);
    
    adapterInstance.addProperty(Geo.lat,"52.5258083");
    adapterInstance.addProperty(Geo.long_,"13.3172764");
    adapterInstance.addProperty(Omn_resource.isExclusive,"false");


  }*/
  
  public OpenstackAdapter(Model adapterTBox, Resource adapterABox){
    this.uuid = UUID.randomUUID().toString();
    this.openstackClient = new OpenstackClient(this);
    this.adapterTBox = adapterTBox;
    this.adapterABox = adapterABox;
    Resource adapterType = Omn_domain_pc.VMServer;
    this.adapterABox.addProperty(RDF.type,adapterType);
    this.adapterABox.addProperty(RDFS.label,  this.adapterABox.getLocalName());
    this.adapterABox.addProperty(RDFS.comment, "Openstack server");

    Property longitude = adapterTBox.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#long");
    Property latitude = adapterTBox.createProperty("http://www.w3.org/2003/01/geo/wgs84_pos#lat");
    this.adapterABox.addProperty(latitude, "52.516377");
    this.adapterABox.addProperty(longitude, "13.323732");
    this.adapterABox.addProperty(Omn_lifecycle.canImplement, Omn_domain_pc.VM);

  }

  public void initFlavors() {
    Flavors flavors = openstackClient.listFlavors();
    readDefaultFlavours();
    for(Flavor flavor: flavors.getList()){
    	
    Resource vmResource = adapterABox.getModel().createResource(OntologyModelUtil.getResourceNamespace() + flavor.getName());
      vmResource.addProperty(RDFS.subClassOf, Omn_domain_pc.VM);
      vmResource.addProperty(Omn_domain_pc.hasCPU, flavor.getVcpus());
      vmResource.addProperty(Omn_lifecycle.hasID,flavor.getId());
      for(Resource r: getDiskImages()){
        vmResource.addProperty(Omn_domain_pc.hasDiskImage, r);
      }
      adapterABox.addProperty(Omn_lifecycle.canImplement, vmResource);
    }
    
  }
  
  public void readDefaultFlavours(){
	  if(openstackAdapterControler.instancesDefaultFlavours.get(this.uuid) != null){
		  
		defaultFlavours =openstackAdapterControler.instancesDefaultFlavours.get(this.uuid);
		  for (String s :  defaultFlavours.keySet()){
			    Resource flavourResource = adapterABox.getModel().createResource(OntologyModelUtil.getResourceNamespace() + s);
			    flavourResource.addProperty(RDFS.subClassOf, Omn_domain_pc.VM);
			    flavourResource.addProperty(Omn_domain_pc.hasDiskImage, defaultFlavours.get(s).get(0));
			    flavourResource.addProperty(Omn_lifecycle.hasID, defaultFlavours.get(s).get(1));


	    	  adapterABox.addProperty(Omn_lifecycle.canImplement, flavourResource);
	      }
	        
	    }else {
	    	LOGGER.log(Level.SEVERE, "Could not find default Flavours in the Config-File - Flavours from Server used instead");
	    }
  }

  protected List<Resource> getDiskImages() {
    List<Resource> diskimages = new ArrayList<>();
    Images images = openstackClient.listImages();
    for(Image image : images.getList()){
      Resource diskImage = adapterABox.getModel().createResource(OntologyModelUtil.getResourceNamespace() + "diskImage/" +image.getId() );
      diskImage.addProperty(RDF.type, Omn_domain_pc.DiskImage);
      diskImage.addProperty(Omn_domain_pc.hasDiskimageLabel,image.getName());
      diskImage.addProperty(Omn_domain_pc.hasDiskimageURI, image.getId());
      diskimages.add(diskImage);

    }
    return diskimages;
  }

  @Override
  public Model createInstance(String instanceURI, Model newInstanceModel)  {

    Resource requestedVM = newInstanceModel.getResource(instanceURI);
    LOGGER.log(Level.SEVERE, MessageUtil.serializeModel(requestedVM.getModel(), IMessageBus.SERIALIZATION_NTRIPLE));
    String typeURI = getRequestedTypeURI(requestedVM);
    String flavorId = getFlavorId(typeURI);
    String diskImageURI = new String("");
    
    if(defaultFlavours == null){
    	defaultFlavours =openstackAdapterControler.instancesDefaultFlavours.get(this.uuid);
    }
	  
    for (String s : defaultFlavours.keySet()){
		  if (typeURI.equals("http://localhost/resource/"+s)){
			  diskImageURI = defaultFlavours.get(s).get(0);
		  }
	  } 

    if(diskImageURI.isEmpty()){
    diskImageURI = getDiskImageId(requestedVM);
    }
    String username = getUsername(requestedVM);
    String publicKey = getPublicKey(requestedVM);
    Resource monitoringService =  getMonitoringService(newInstanceModel);

    ServerForCreate serverForCreate = new ServerForCreate(instanceURI,flavorId,diskImageURI,null);

    if(username != null && publicKey != null) {
      String userdata_not_encoded = "#cloud-config\n" +
              "users:\n" +
              "  - name: " + username + "\n" +
              "    sudo: ALL=(ALL) NOPASSWD:ALL\n" +
              "    shell: /bin/bash\n" +
              "    ssh-authorized-keys:\n" +
              "      - " + publicKey + "\n";

      String userdata_encoded = Base64.getEncoder().encodeToString(userdata_not_encoded.getBytes());

      serverForCreate.setUserData(userdata_encoded);
    }

    try {
      CreateVM createVM = new CreateVM(serverForCreate, this.listener, username);
      if(monitoringService != null){
        createVM.setMonitoringService(monitoringService);
      }
      ManagedThreadFactory threadFactory = (ManagedThreadFactory) new InitialContext().lookup("java:jboss/ee/concurrency/factory/default");
      Thread  createVMThread = threadFactory .newThread(createVM);
      createVMThread.start();
    } catch (NamingException e) {
      e.printStackTrace();
    }


    Model returnModel = ModelFactory.createDefaultModel();
    Resource resource =  returnModel.createResource(requestedVM.getURI());
    resource.addProperty(RDF.type, Omn_domain_pc.VM);
//    resource.addProperty(Omn_domain_pc.hasVMID, )
    
     Property property = returnModel.createProperty(Omn_lifecycle.hasState.getNameSpace(), Omn_lifecycle.hasState.getLocalName());
     property.addProperty(RDF.type, OWL.FunctionalProperty);
     resource.addProperty(property, Omn_lifecycle.Uncompleted);
    return returnModel;
  }
  


  private Resource getMonitoringService(Model newInstanceModel) {
    ResIterator resIterator = newInstanceModel.listSubjectsWithProperty(RDF.type, Omn_monitoring.MonitoringService.getURI());
    Resource omsp_service = null;
    while (resIterator.hasNext()){
      omsp_service = resIterator.nextResource();
    }

    return omsp_service;
  }

/*  private String addKeypairId(String username, String publicKey ) {
    String keyPairId = null;

    if (username != null && publicKey != null) {
        keyPairId = username + UUID.randomUUID();
        openstackClient.addKeyPair(keyPairId, publicKey);
    }


    return keyPairId;
  }*/

  private String getPublicKey(Resource requestedVM) {
    Statement publicKeyStatement = requestedVM.getProperty(Omn_service.publickey);
    if(publicKeyStatement == null){
      System.err.println("Warning: no public key found!");
      return "";
    }
    final RDFNode keyObject = publicKeyStatement.getObject();
    if (null == keyObject) {
    	System.err.println("Warning: no public key found!");
    	return "";
    } else {
    	return keyObject.toString();
    }
  }

  private String getUsername(Resource requestedVM) {
    Statement usernameStatement = requestedVM.getProperty(Omn_service.username);


    if(usernameStatement != null) {


      String userName = usernameStatement.getObject().toString();
      return userName;
    }
    return null;
  }


//  public void addListener(OpenstackAdapterMDBSender newListener) {
//    myListeners.add(newListener);
//  }
  private String getFlavorId(String typeURI) {
    String flavorId = null;

    Resource requestedFlavor = this.adapterABox.getModel().getResource(typeURI);
    if(requestedFlavor != null){
      Statement statement = requestedFlavor.getProperty(Omn_lifecycle.hasID);
      if(statement != null){
        RDFNode node = statement.getObject();
        flavorId = node.asLiteral().getString();
      }

    }
    return flavorId;
  }

  private String getDiskImageId(Resource requestedVM) {
    String diskImageURI = null;
    Statement requestedDiskImage = requestedVM.getProperty(Omn_domain_pc.hasDiskImage);
    if(requestedDiskImage != null){
      Resource diskImage = this.adapterABox.getModel().getResource(requestedDiskImage.getObject().asResource().getURI());


       diskImageURI = diskImage.getProperty(Omn_domain_pc.hasDiskimageURI).getString();
    }
    return diskImageURI;
  }

  private String getRequestedTypeURI(Resource requestedVM) {
    StmtIterator stmtIterator = requestedVM.listProperties(RDF.type);
    String requestedType = null;
    while (stmtIterator.hasNext()){
      Statement statement = stmtIterator.nextStatement();
      RDFNode one = statement.getObject();
      boolean isNode =  one.equals(Omn_resource.Node);
      if(!isNode){
        requestedType = statement.getObject().asResource().getURI();
        break;
      }
    }
    return requestedType;
  }

  @Override
  public void deleteInstance(String instanceURI) {

    Server instance = null;
    try {
      instance = getServerWithName(instanceURI);
    } catch (InstanceNotFoundException e) {
      Logger.getAnonymousLogger().log(Level.SEVERE, "Resource could not be deleted");
    }
    String id = instance.getId();
    openstackClient.deleteServer(id);
      return;
  }

  private Server getServerWithName(String instanceURI) throws InstanceNotFoundException {
    Servers servers = openstackClient.listServers();
    for(Server server : servers.getList()){
      if(server.getName().equals(instanceURI)){
        return server;
      }
    }
    throw new InstanceNotFoundException(instanceURI + " not found");
  }


  @Override
  public void updateAdapterDescription(){
    Images images = openstackClient.listImages();
    if(images != null){
     // openstackParser.addToAdapterInstanceDescription(images);
    }
  }
  
  @Override
  public Model updateInstance(String instanceURI, Model configureModel) {
    return  configureModel;

  }
  
  
  
  public String getImageAssociatedFlavor(String diskImageName){
 	try{
	  return (String) openstackAdapterControler.instancesDefaultFlavours.get(this.uuid).get(diskImageName);
	  
 	}catch(Exception e){
        LOGGER.log(Level.SEVERE, "Could not find default Flavour for "+diskImageName);
        return null;
 	}
  }
  

 /* public Resource getImageResource(){
    return adapterModel.getResource(getAdapterManagedResources().get(0).getNameSpace()+"OpenstackImage");
  }
*/

  @Override
  public Resource getAdapterABox() {
    return adapterABox;
  }

  @Override
  public Model getAdapterDescriptionModel() {
    return adapterABox.getModel();
  }
  


  @Override
  public Model getInstance(String instanceURI) throws InstanceNotFoundException {
    Servers servers = openstackClient.listServers();
    for(Server server : servers.getList()){
      if(server.getName().equals(instanceURI)){
        return ModelFactory.createDefaultModel();
      }
    }
    throw new InstanceNotFoundException("Instance "+instanceURI+" not found");
  }

  @Override
  public Model getAllInstances() throws InstanceNotFoundException {
    Model model = ModelFactory.createDefaultModel();
    Servers servers = openstackClient.listServers();
    for(Server server : servers.getList()){
      model.add(getInstance(server.getName()));
    }
    return model;
  }

    public String getFloatingPool() {
        return floatingPool;
    }

  /*public void setListener(OpenstackAdapterMDBSender listener) {
    this.listener = listener;
  }
*/
  public void setFloatingPool(String floatingPool) {
    this.floatingPool = floatingPool;
  }

    public String getKeystone_auth_URL() {
        return keystone_auth_URL;
    }

  public void setKeystone_auth_URL(String keystone_auth_URL) {
    this.keystone_auth_URL = keystone_auth_URL;
  }

    public String getNet_name() {
        return net_name;
    }

  public void setNet_name(String net_name) {
    this.net_name = net_name;
  }

    public String getNova_endpoint() {
        return nova_endpoint;
    }

  public void setNova_endpoint(String nova_endpoint) {
    this.nova_endpoint = nova_endpoint;
  }

    public String getKeystone_password() {
        return keystone_password;
    }

  public void setKeystone_password(String keystone_password) {
    this.keystone_password = keystone_password;
  }

    public String getKeystone_endpoint() {
        return keystone_endpoint;
    }

  public void setKeystone_endpoint(String keystone_endpoint) {
    this.keystone_endpoint = keystone_endpoint;
  }

    public String getGlance_endpoint() {
        return glance_endpoint;
    }

  public void setGlance_endpoint(String glance_endpoint) {
    this.glance_endpoint = glance_endpoint;
  }

    public String getNet_endpoint() {
        return net_endpoint;
    }

  public void setNet_endpoint(String net_endpoint) {
    this.net_endpoint = net_endpoint;
  }

    public String getTenant_name() {
        return tenant_name;
    }

  public void setTenant_name(String tenant_name) {
    this.tenant_name = tenant_name;
  }

    public String getKeystone_username() {
        return keystone_username;
    }

  public void setKeystone_username(String keystone_username) {
    this.keystone_username = keystone_username;
  }

    public String getDefault_flavor_id() {
        return default_flavor_id;
    }

  public void setDefault_flavor_id(String default_flavor_id) {
    this.default_flavor_id = default_flavor_id;
  }

    public String getDefault_image_id() {
        return default_image_id;
    }

  public void setDefault_image_id(String default_image_id) {
    this.default_image_id = default_image_id;
  }

@Override
public void refreshConfig() throws ProcessingException {
	// TODO Auto-generated method stub

}

  @Override
  public void shutdown() {

  }

  @Override
  public void configure(Config configuration) {

  }

    public void setListener(OpenstackAdapterMDBSender listener) {
        this.listener = listener;
    }

    private class CreateVM implements Runnable {

    private final ServerForCreate serverForCreate;
    private final OpenstackAdapterMDBSender parent;
    private final String username;
    private Resource monitoringService;

    public CreateVM(ServerForCreate serverForCreate, OpenstackAdapterMDBSender parent, String username){
      this.parent =  parent;
      this.serverForCreate = serverForCreate;
      this.username = username;

    }

    public Resource getMonitoringService() {
      return monitoringService;
    }

    public void setMonitoringService(Resource monitoringService) {
      this.monitoringService = monitoringService;
    }

    @Override
    public void run() {
      Server server = openstackClient.createServer(serverForCreate);
      FloatingIps floatingIps = openstackClient.listFreeFloatingIps();
      FloatingIp floatingIp = null;
      if(floatingIps != null){
        Iterator<FloatingIp> floatingIpIterator = floatingIps.iterator();
        while(floatingIpIterator.hasNext()) {

          FloatingIp tempfloatingIp = floatingIpIterator.next();
          if (tempfloatingIp.getInstanceId() == null) {
            floatingIp = tempfloatingIp;
          }
        }
      }
      try {
      if(floatingIp == null) {

          floatingIp = openstackClient.addFloatingIp();

        }
        openstackClient.allocateFloatingIpForServer(server.getId(),floatingIp.getIp());
      }catch (Exception e){
        throw  new RuntimeException();
      }

      Model model = parseToModel(server, floatingIp);
      parent.publishModelUpdate(model, UUID.randomUUID().toString(), IMessageBus.TYPE_INFORM, IMessageBus.TARGET_ORCHESTRATOR);



    }


    private Model parseToModel(Server server, FloatingIp floatingIp){
      //TODO: better check whether it's already an URI

      Model parsedServerModel =  ModelFactory.createDefaultModel();
      Resource parsedServer = parsedServerModel.createResource(server.getName());
      server = openstackClient.getServerDetails(server.getId());

      int retryCounter = 0;
      while(retryCounter < 10){
        if(!"ACTIVE".equalsIgnoreCase(server.getStatus())){
          try {
            Thread.sleep(1000);
            server = openstackClient.getServerDetails(server.getId());
          } catch (InterruptedException e) {
            throw  new RuntimeException();
          }
      }else {
          break;
        }
      }
      parsedServer.addProperty(Omn_domain_pc.hasVMID,server.getId());

      parsedServer.addProperty(RDF.type, Omn_domain_pc.VM);
      Property property = parsedServer.getModel().createProperty(Omn_lifecycle.hasState.getNameSpace(),Omn_lifecycle.hasState.getLocalName());
      property.addProperty(RDF.type, OWL.FunctionalProperty);
      parsedServer.addProperty(property, Omn_lifecycle.Started);


     if(floatingIp != null){


       Resource loginService = parsedServerModel.createResource(OntologyModelUtil
               .getResourceNamespace() + "LoginService" + UUID.randomUUID().toString());
       loginService.addProperty(RDF.type, Omn_service.LoginService);
       loginService.addProperty(Omn_service.authentication,"ssh-keys");
       loginService.addProperty(Omn_service.username, username);
       loginService.addProperty(Omn_service.hostname, floatingIp.getIp());
       loginService.addProperty(Omn_service.port,"22");
       parsedServer.addProperty(Omn.hasService, loginService);

     }
      if(monitoringService != null){
        parsedServer.addProperty(Omn_lifecycle.usesService,monitoringService);
        parsedServer.getModel().add(monitoringService.listProperties());
      }

      return parsedServer.getModel();
    }

  }
    
    public IOpenstackClient getOpenstackClient() {
    	return openstackClient;
    }

    protected void setOpenstackClient(IOpenstackClient openstackClient) {
    	this.openstackClient = openstackClient;
    }


}
