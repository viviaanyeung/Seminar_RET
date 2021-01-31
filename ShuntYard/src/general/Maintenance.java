package general;

/**
 * Class for a type of maintenance object
 * @author Koen Vermeulen, Marten Koole and Vivian Yeung
 *
 */
public class Maintenance {
	final private String name;
	final private int rank;
	final private double duration; // in minutes
	final private int afterKM; //every ... km
	
	public Maintenance(String name, int rank, double duration, int afterKM) {
		this.name = name;
		this.rank = rank;
		this.duration = duration;
		this.afterKM = afterKM;
	}

	public String getName() {
		return name;
	}

	public int getRank() {
		return rank;
	}

	public double getDuration() {
		return duration;
	}

	public int getAfterKM() {
		return afterKM;
	}

}