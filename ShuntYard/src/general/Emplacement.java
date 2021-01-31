package general;

public class Emplacement {
	private Track[] tracks;
	private String name;
	public Emplacement(Track[] t, String n) {
		this.tracks=t;
		this.name=n;
	}
	public Track[] getTracks() {return this.tracks;}
	public String getName() {return this.name;}
}
