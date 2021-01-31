package general;

import java.util.ArrayList;

/**
 * Instances containing all data
 * @author Koen Vermeulen, Marten Koole and Vivian Yeung
 *
 */
public class Instance {
	//coupling 2 min, uncoulping 5 min
	final private ArrayList<VehicleUnit> vehicleUnits;
	final private Emplacement[] emplacements;
	final private VehicleGroup[] vehicleGroups;
	final private ArrayList<ArrayList<VehicleTask>> allVehicleTasks;

	public Instance(ArrayList<VehicleUnit> vehicleUnits, Emplacement[] emplacements, VehicleGroup[] vehicleGroups, 
			ArrayList<ArrayList<VehicleTask>> allVehicleTasks) {
		this.vehicleUnits = vehicleUnits;
		this.emplacements = emplacements;
		this.vehicleGroups = vehicleGroups;
		this.allVehicleTasks = allVehicleTasks;
	}

	public ArrayList<VehicleUnit> getVehicleUnits() {
		return vehicleUnits;
	}
	public ArrayList<ArrayList<VehicleTask>> getAllVehicleTasks() {
		return allVehicleTasks;
	}
	public Emplacement[] getEmplacements() {
		return this.emplacements;
	}
	public VehicleGroup[] getVehicleGroups() {
		return this.vehicleGroups;
	}

}