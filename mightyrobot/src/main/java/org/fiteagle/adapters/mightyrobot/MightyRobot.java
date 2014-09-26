package org.fiteagle.adapters.mightyrobot;

import java.util.Random;


public class MightyRobot {


    private boolean dancing;
	private boolean exploded;
    private int headRotation;
    private String nickname;

    private MightyRobotAdapter owningAdapter;
    private String instanceName;
    
    private String[] nicknames = {"Mecha", "RD5000", "Robocop", "R2-D2", "C3-PO"};

	public MightyRobot(MightyRobotAdapter owningAdapter, String instanceName) {
    
        this.dancing = false;
        this.exploded = false;
        this.headRotation = 0;
        this.nickname = nicknames[new Random().nextInt(nicknames.length)];
        this.owningAdapter = owningAdapter;
        this.instanceName = instanceName;
    }
    
    public String toString(){
        String reply = "";
    	if (this.getExploded()){
    		reply = "Might Robot " + this.nickname + " exploded due to unforeseen circumstances. Sorry about that.";
    	} else {
    		reply = "Mighty Robot " + this.nickname + " bids you a fond welcome. The robot is currently " 
    				+ (this.getDancing() ? "" : "not ")
    				+ "dancing, with its head turned by " + this.getHeadRotation() + " Degrees.";
    	}
        return reply;
    }
 
    public boolean getExploded() {
		return exploded;
	}

	public void setExploded(boolean exploded) {
		this.exploded = exploded;
	}

	public boolean getDancing() {
		return dancing;
	}

	public void setDancing(Boolean dancing) {
		this.dancing = dancing;
	}

	public int getHeadRotation() {
		return headRotation;
	}

	public void setHeadRotation(int headRotation) {
		this.headRotation = headRotation % 360;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public MightyRobotAdapter getOwningAdapter() {
		return owningAdapter;
	}
	
    public String getInstanceName() {
		return instanceName;
	}

}

