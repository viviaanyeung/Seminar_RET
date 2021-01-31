package general;

import java.util.ArrayList;

public class Metro {
	ArrayList<VehicleUnit> metroComponents;
	public Metro(ArrayList<VehicleUnit> c) {
		this.metroComponents=c;
	}
	// Accessor methods.
	public ArrayList<VehicleUnit> getComposition() {return this.metroComponents;}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.metroComponents == null) ? 0 : this.metroComponents.hashCode());
		return result;
	}
}