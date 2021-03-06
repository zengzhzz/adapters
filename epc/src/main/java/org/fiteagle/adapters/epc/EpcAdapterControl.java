package org.fiteagle.adapters.epc;

import info.openmultinet.ontology.vocabulary.Epc;
import info.openmultinet.ontology.vocabulary.Omn_service;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
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
import org.fiteagle.adapters.epc.dm.EpcAdapterMDBSender;
import org.fiteagle.api.core.IMessageBus;
import org.fiteagle.api.core.OntologyModelUtil;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Created by dne on 15.06.15.
 */
@Singleton
@Startup
public class EpcAdapterControl extends AdapterControl {

	@Inject
	protected EpcAdapterMDBSender mdbSender;

	private static final Logger LOGGER = Logger
			.getLogger(EpcAdapterControl.class.getName());

	@PostConstruct
	public void initialize() {
		LOGGER.log(Level.INFO, "Starting EpcAdapter");

		// this.adapterModel = OntologyModelUtil.loadModel(
		// "ontologies/epc-adapter.ttl", IMessageBus.SERIALIZATION_TURTLE);

		this.adapterModel = OntologyModelUtil.loadModel("ontologies/epc.ttl",
				IMessageBus.SERIALIZATION_TURTLE);

		this.adapterInstancesConfig = this.readConfig("EpcAdapter");
		this.createAdapterInstances();
		this.publishInstances();
	}

	@Override
	public AbstractAdapter createAdapterInstance(final Model tbox,
			final Resource abox) {

		LOGGER.log(Level.WARNING,
				"createAdapterInstance, adding " + abox.getURI());

		final AbstractAdapter adapter = new EpcAdapter(tbox, abox);

		this.adapterInstances.put(adapter.getId(), adapter);

		LOGGER.log(Level.WARNING, "added adapter, new adapterInstances size: "
				+ adapterInstances.size());

		AbstractAdapter adapterInstance = null;
		Iterator<?> it = this.adapterInstances.entrySet().iterator();
		while (it.hasNext()) {
			@SuppressWarnings("unchecked")
			Map.Entry<String, AbstractAdapter> pair = (Map.Entry<String, AbstractAdapter>) it
					.next();
			LOGGER.log(Level.WARNING, "adapter instance key: " + pair.getKey());
			AbstractAdapter epcAdapter = pair.getValue();
			LOGGER.log(Level.WARNING, "adapter instance value: "
					+ epcAdapter.getAdapterABox().getURI());
			if (abox.getURI().equals(epcAdapter.getAdapterABox().getURI())) {
				adapterInstance = epcAdapter;
			}
		}

		// AbstractAdapter adapterInstance =
		// adapterInstances.get(abox.getURI());

		if (adapterInstance == null) {
			LOGGER.log(Level.WARNING,
					"createAdapterInstance method in EpcAdapterControl: adapterInstance is null");
		} else {
			LOGGER.log(Level.WARNING,
					"createAdapterInstance method in EpcAdapterControl: adapterInstance "
							+ adapterInstance.getAdapterABox().getURI());
		}
		return adapter;
	}

	@Override
	protected void parseConfig() {

		final String jsonProperties = this.adapterInstancesConfig
				.readJsonProperties();
		if (!jsonProperties.isEmpty()) {
			final JsonReader jsonReader = Json
					.createReader(new ByteArrayInputStream(jsonProperties
							.getBytes(StandardCharsets.UTF_8)));

			final JsonObject jsonObject = jsonReader.readObject();

			final JsonArray adaptInstances = jsonObject
					.getJsonArray(IAbstractAdapter.ADAPTER_INSTANCES);

			for (int i = 0; i < adaptInstances.size(); i++) {
				final JsonObject adaptInstObject = adaptInstances
						.getJsonObject(i);
				final String adapterInstance = adaptInstObject
						.getString(IAbstractAdapter.COMPONENT_ID);
				if (!adapterInstance.isEmpty()) {
					final Model model = ModelFactory.createDefaultModel();
					final Resource resource = model
							.createResource(adapterInstance);

					String pgwIp = adaptInstObject.getString("pgwIp");
					if (pgwIp != null) {
						resource.addProperty(
								model.createProperty(Epc.getURI(), "pgwIp"),
								pgwIp);
					}

					String pgwStart = adaptInstObject.getString("pgwStart");
					if (pgwStart != null) {
						resource.addProperty(
								model.createProperty(Epc.getURI(), "pgwStart"),
								pgwStart);
					}

					String pgwStop = adaptInstObject.getString("pgwStop");
					if (pgwStop != null) {
						resource.addProperty(
								model.createProperty(Epc.getURI(), "pgwStop"),
								pgwStop);
					}

					this.createAdapterInstance(this.adapterModel, resource);
				}
			}

		}
	}

	@Override
	protected void addAdapterProperties(final Map<String, String> adaptInstance) {
		LOGGER.warning("Not implemented. Input: " + adaptInstance);
	}

}
