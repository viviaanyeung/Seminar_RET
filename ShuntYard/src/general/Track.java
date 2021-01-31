package general;

public class Track {
	final private int capacityInMeters;
	final private String name;
	final private String emplacement;
	final private boolean freeTrack;
	final private boolean washingTrack;
	final private boolean maintenanceTrack;
	
	/// !!!!! Check Table 3 definition 
	final private double timeToParkingTrack;//time to parking track in min
	final private double timeToMaintenanceTrack;//time to maintenance track in min
	final private double timeToWashingTrack;//time to washing track in min
	final private double timeFromPullIn;//time from pull in to this track
	final private double timeToPullOut;//time to pull out from this track
	public Track(int Q, String n, String e, boolean f, boolean w, boolean m) {
		this.capacityInMeters=Q;
		this.name=n;
		this.emplacement=e;
		this.freeTrack=f;
		this.washingTrack=w;
		this.maintenanceTrack=m;
		if(this.washingTrack) { // EXTRA 10 IF SIDE TRACK USED == > take into account with MetroMovement
			timeToParkingTrack = 10;
			timeToMaintenanceTrack = 10;
			timeToWashingTrack = -1;
			timeFromPullIn = 15;
			timeToPullOut = 0;
		} else if (this.maintenanceTrack) {
			timeToParkingTrack = 10;
			timeToMaintenanceTrack = -1;
			timeToWashingTrack = 15;
			timeFromPullIn = 10;
			timeToPullOut = 0;
		} else {
			timeToParkingTrack = 10;
			timeToMaintenanceTrack = 10;
			timeToWashingTrack = 15;
			timeFromPullIn = 0;
			timeToPullOut = 0;
		}
	}
	// Accessor methods.
	public double getTimeToParkingTrack() {
		return timeToParkingTrack;
	}

	public double getTimeToMaintenanceTrack() {
		return timeToMaintenanceTrack;
	}

	public double getTimeToWashingTrack() {
		return timeToWashingTrack;
	}

	public double getTimeFromPullIn() {
		return timeFromPullIn;
	}

	public double getTimeToPullOut() {
		return timeToPullOut;
	}
	public int getCapacity() {return this.capacityInMeters;}
	public String getName() {return this.name;}
	public String getEmplacement() {return this.emplacement;}
	public boolean isFree() {return this.freeTrack;}
	public boolean isWasher() {return this.washingTrack;}
	public boolean isMaintenance() {return this.maintenanceTrack;}
}