package org.fiteagle.adapters.motor;

import java.util.LinkedList;
import java.util.List;


public class Motor {
    

    private String manufacturer;
    private int rpm;
    private int maxRpm;
    private int throttle;
    protected MotorAdapter owningAdapter;
    protected List<String> updatedProperties = new LinkedList<String>();
    
    private String instanceName;
    
    public Motor(){
        super();
        
    }
    
    public Motor(MotorAdapter owningAdapter, String instanceName) {
        super();
        this.manufacturer = "Fraunhofer FOKUS";
        this.rpm = 0;
        this.maxRpm = 3000;
        this.throttle = 0;
        
        this.owningAdapter = owningAdapter;
        this.instanceName = instanceName;
    }
    
    
    public String toString() {
        
        return "Motor";
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getRpm() {
        return rpm;
    }

    public void setRpm(int rpm, List<String> updatedProperties) {
        this.rpm = rpm;
        updatedProperties.add("rpm");
    }

    public int getMaxRpm() {
        return maxRpm;
    }

    public void setMaxRpm(int maxRpm, List<String> updatedProperties) {
        this.maxRpm = maxRpm;
        updatedProperties.add("maxRpm");
    }

    public int getThrottle() {
        return throttle;
    }

    public void setThrottle(int throttle, List<String> updatedProperties) {
        this.throttle = throttle;
        updatedProperties.add("throttle");
    }

    public String getInstanceName() {
        return instanceName;
    }

}
