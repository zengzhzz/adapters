package org.fiteagle.adapters;

import java.beans.PropertyChangeListener;
import java.io.InputStream;

public interface AbstractAdapter {

    public String getAdapterDescription(String serializationFormat);

    public boolean createInstance(int instanceID);

    public boolean terminateInstance(int instanceID);

    public String monitorInstance(int instanceID, String serializationFormat);

    public String getAllInstances(String serializationFormat);

    public String controlInstance(InputStream in, String serializationFormat);

    public boolean addChangeListener(PropertyChangeListener newListener);

}