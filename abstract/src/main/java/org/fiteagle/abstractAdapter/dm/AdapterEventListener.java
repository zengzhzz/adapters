package org.fiteagle.abstractAdapter.dm;

import com.hp.hpl.jena.rdf.model.Model;

public interface AdapterEventListener {

    void publishModelUpdate(Model eventRDF, String requestID, String methodType, String methodTarget);
    
}
