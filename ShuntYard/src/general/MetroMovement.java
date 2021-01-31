package general;

import java.util.Date;

public class MetroMovement implements Comparable<MetroMovement> {
	private Date time;
	private Track track;
	private boolean aSide;
	private Metro metro;
	private boolean arrival;
	private String name;
	// Different potential constructors.
	public MetroMovement(Date d, Track t, boolean s, Metro m, boolean a, String n) {
		this.time=d;
		this.track=t;
		this.aSide=s;
		this.metro=m;
		this.arrival=a;
		this.name=n;
	}
	// Accessor methods.
	public Date getTime() {return this.time;}
	public Track getTrack() {return this.track;}
	public boolean isASide() {return this.aSide;}
	public Metro getMetro() {return this.metro;}
	public boolean isArrival() {return this.arrival;}
	public String getName() {return this.name;}
	// Method for comparing based on track (first) and time (second): The first move is placed before the second move.
	@Override	public int compareTo(MetroMovement other) {
		if(!this.track.equals(other.track)) return this.track.hashCode()-other.track.hashCode();
		return (int) (this.time.getTime()-other.time.getTime());
	}
}
