package general;

/**
 * Class for a vehicle unit object
 * @author Koen Vermeulen, Marten Koole and Vivian Yeung
 *
 */
public class VehicleUnit {
	final private String name;
	final private VehicleGroup group;
	private double drivenKM;
	private int prevWash; // days since the previous wash (if washed today, then it is equal to 0)
	private Emplacement emp;
	
	public VehicleUnit(String name, VehicleGroup group, double drivenKM, int prevWash) {
		this.name = name;
		this.group = group;
		this.drivenKM = drivenKM;
		this.prevWash = prevWash;
	}
	// Accessor methods.
	public String getName() {return this.name;}
	public VehicleGroup getGroup() {return this.group;}
	public double getDrivenKM() { return this.drivenKM;}
	public int getPrevWash() {return this.prevWash;}
	public Emplacement getEmplacement() {return this.emp;}
	// Modification method (called in construction when all units are defined).
	public void setEmplacement(Emplacement emp) {this.emp=emp;}
}