package general;

import java.util.Date;

public class MetroParking {
	private Date startTime;
	private Date endTime;
	private Metro metro;
	private MetroMovement arrivingMetro;
	private MetroMovement departingMetro;
	private Track track;
	private int startingHeightDrawing;
	public MetroParking(Date s, Date e, Metro m, Track t, MetroMovement a, MetroMovement d) {
		this.startTime=s;
		this.endTime=e;
		this.metro=m;
		this.track=t;
		this.arrivingMetro=a;
		this.departingMetro=d;
	}
	public MetroParking(Date s, Date e, Metro m, Track t, MetroMovement a, MetroMovement d, int shd) {
		this.startTime=s;
		this.endTime=e;
		this.metro=m;
		this.track=t;
		this.arrivingMetro=a;
		this.departingMetro=d;
		this.startingHeightDrawing=shd;
	}
	public MetroParking(MetroParking m, int shd) {
		this.startTime=m.getStartTime();
		this.endTime=m.getEndTime();
		this.metro=m.getMetro();
		this.track=m.getTrack();
		this.arrivingMetro=m.getArrival();
		this.departingMetro=m.getDeparture();
		this.startingHeightDrawing=shd;
	}
	// Accessor Methods
	public Date getStartTime() {return this.startTime;}
	public Date getEndTime() {return this.endTime;}
	public Metro getMetro() {return this.metro;}
	public Track getTrack() {return this.track;}
	public MetroMovement getArrival() {return this.arrivingMetro;}
	public MetroMovement getDeparture() {return this.departingMetro;}
	public int getStartingHeightDrawing() {return this.startingHeightDrawing;}
	// Modification method
	public void setStartingHeightDrawing(int shd) {this.startingHeightDrawing=shd;}
}
