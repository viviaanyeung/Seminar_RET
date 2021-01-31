package general;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Class for vehicle group object
 * @author Koen Vermeulen, Marten Koole and Vivian Yeung
 *
 */
public class VehicleGroup {
	final private int lengthInMeters;
	final private String vehicleGroup;
	final private int seriesID;
	final private HashMap<Emplacement, Integer> unitsAvailable;
	final private boolean bidirectional;
	private ArrayList<VehicleGroup> couplePossibilities;
	final private Color color;
	final private Maintenance[] maintenance;

	public VehicleGroup(int meters, String group, int ID, HashMap<Emplacement, Integer> avail, boolean b, Maintenance[] maintenance) {
		this.lengthInMeters=meters;
		this.color=new Color(this.lengthInMeters%100, this.lengthInMeters%100, this.lengthInMeters%100);
		this.vehicleGroup=group;
		this.seriesID=ID;
		this.unitsAvailable=avail;
		this.bidirectional=b;
		this.maintenance = maintenance;
	}
	// Accessor methods.
	public int getLength() {return this.lengthInMeters;}
	public Color getColor() {return this.color;}
	public String getGroup() {return this.vehicleGroup;}
	public int getID() {return this.seriesID;}
	public boolean isBidirectional() {return this.bidirectional;}
	public HashMap<Emplacement,Integer> getAvailability(){return this.unitsAvailable;}
	public ArrayList<VehicleGroup> getCouplePossibilities() {return this.couplePossibilities;}
	public Maintenance[] getMaintenance() { return maintenance;}
	// Modification method (called in construction when all units are defined).
	public void setCouplePossibilities(ArrayList<VehicleGroup> c) {this.couplePossibilities=c;}
}