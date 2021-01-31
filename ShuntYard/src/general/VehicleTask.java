package general;

import java.time.LocalTime;
import java.util.ArrayList;

public class VehicleTask {
	final private String name;
	final private String from;
	final private String to;
	final private LocalTime startTime;
	final private LocalTime endTime;
	final private double distance; //in km or m? doesnt say in data; I think km
//	final private String vehicleGroup; // idk what this is or if we need this
	final private boolean pullIn; // true if pull-in, false if pull-out
	final private String direction;
	final private ArrayList<String> composition; //ordered
	final private VehicleGroup vehicleGroup;
	
	public VehicleTask(String name, String from, String to, LocalTime startTime, LocalTime endTime, double distance,
			boolean pullIn, String direction, ArrayList<String> composition, VehicleGroup vehicleGroup) {
		super();
		this.name = name;
		this.from = from;
		this.to = to;
		this.startTime = startTime;
		this.endTime = endTime;
		this.distance = distance;
		this.pullIn = pullIn;
		this.direction = direction;
		this.composition = composition;
		this.vehicleGroup = vehicleGroup;
	}

	
	public VehicleGroup getVehicleGroup() {
		return vehicleGroup;
	}

	public String getName() {
		return name;
	}

	public String getFrom() {
		return from;
	}

	public String getTo() {
		return to;
	}

	public LocalTime getStartTime() {
		return startTime;
	}

	public LocalTime getEndTime() {
		return endTime;
	}

	public double getDistance() {
		return distance;
	}

	public boolean isPullIn() {
		return pullIn;
	}

	public String getDirection() {
		return direction;
	}

	public ArrayList<String> getComposition() {
		return composition;
	}

}