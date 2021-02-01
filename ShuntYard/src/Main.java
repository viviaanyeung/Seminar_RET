import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import general.Emplacement;
import general.MetroMovement;
import general.Track;
import general.VehicleGroup;
import general.VehicleTask;
import general.VehicleUnit;
import general.Instance;
import general.Maintenance;
import general.Metro;
import visualisation.ShuntDraw;

public class Main {
	public static void main(String[] args) throws Exception {
		System.out.println("Test merge Marten");
		System.out.println("Vivian");
		Instance inst = getData();
		//Draw output
		ArrayList<MetroMovement> movements=new ArrayList<>();
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		ShuntDraw drawer= new ShuntDraw(inst.getEmplacements(), movements, dateFormat.parse("2021-01-25 21:30") , dateFormat.parse("2021-02-27 23:59"));
		drawer.createDrawing();
	}

	/**
	 * Read in the data and create an instance with all the data
	 * @throws Exception 
	 */
	private static Instance getData() throws Exception {
		Emplacement[] emplacements = defineTracks();
		VehicleGroup[] vehicleGroups= defineVehicleGroups(emplacements);
		ArrayList<VehicleUnit> vehicleUnits = initialiseVehicleUnits(vehicleGroups);
		ArrayList<ArrayList<VehicleTask>> allVehicleTasks = new ArrayList<ArrayList<VehicleTask>>(4);
		allVehicleTasks.add(defineVehicleTasks(vehicleGroups, new File("CSV Files/Mon_Thu_Vehicle_tasks.csv")));
		allVehicleTasks.add(defineVehicleTasks(vehicleGroups, new File("CSV Files/Fri_Vehicle_tasks.csv")));
		allVehicleTasks.add(defineVehicleTasks(vehicleGroups, new File("CSV Files/Sat_Vehicle_tasks.csv")));
		allVehicleTasks.add(defineVehicleTasks(vehicleGroups, new File("CSV Files/Sun_Vehicle_tasks.csv")));
		Instance inst = new Instance(vehicleUnits, emplacements, vehicleGroups, allVehicleTasks);
		return inst;
	}

	/**
	 * Reads all vehicle tasks
	 * @return An array list containing array lists of all vehicle tasks on a day, in the order of Mon-Thu, Fri, Sat and Sun
	 * @throws Exception 
	 */
	private static ArrayList<VehicleTask> defineVehicleTasks(VehicleGroup[] vehicleGroups, File file) throws Exception{
		Scanner scan = new Scanner(file);
		ArrayList<VehicleTask> vehicleTasks = new ArrayList<VehicleTask>();
		scan.useLocale(Locale.ENGLISH);
		scan.useDelimiter(",|\\n");
		scan.nextLine(); // skip header
		// Name
		String name = scan.next();
		while(scan.hasNext()&&!name.equals("")) {
			// From
			String from = scan.next();
			// Start time
			Scanner timeScan = new Scanner(scan.next());
			timeScan.useDelimiter(":");
			int st_hour = timeScan.nextInt()%24;
			int st_min = -1;
			int st_sec = 0;
			if(timeScan.hasNextInt()) {
				st_min = timeScan.nextInt();
			} else { //Get the seconds 
				Scanner minScan = new Scanner(timeScan.next());
				minScan.useDelimiter(";");
				st_min = minScan.nextInt();
				st_sec = minScan.nextInt();
				minScan.close();
			}
			LocalTime startTime = LocalTime.of(st_hour, st_min, st_sec);
			timeScan.close();
			// End time
			timeScan = new Scanner(scan.next());
			timeScan.useDelimiter(":");
			int et_hour = timeScan.nextInt()%24;
			int et_min = -1;
			int et_sec = 0;
			if(timeScan.hasNextInt()) {
				et_min = timeScan.nextInt();
			} else { // Get the seconds
				Scanner minScan = new Scanner(timeScan.next());
				minScan.useDelimiter(";");
				et_min = minScan.nextInt();
				et_sec = minScan.nextInt();
				minScan.close();
			}
			LocalTime endTime = LocalTime.of(et_hour, et_min, et_sec);
			timeScan.close();
			// To
			String to = scan.next();
			//Distance
			double distance = scan.nextDouble();
			// Vehicle group
			String vehGrp = scan.next();
			VehicleGroup vehicleGroup;
			if(vehGrp.equals("MBOB")) {
				vehicleGroup = vehicleGroups[0];
			} else if (vehGrp.equals("MBOS")) {
				vehicleGroup = vehicleGroups[1];
			} else if (vehGrp.equals("MRSG")) {
				vehicleGroup = vehicleGroups[2];
			} else if (vehGrp.equals("MSG3")) {
				vehicleGroup = vehicleGroups[3];
			} else if (vehGrp.equals("HSG3")) {
				vehicleGroup = vehicleGroups[4];
			} else {
				throw new Exception("No vehicle group given for vehicle task");
			}
			//Pull-in or pull-out
			boolean pullIn = true;
			if(scan.next().equals("Pull-out")) {
				pullIn = false;
			}
			//Composition and order
			ArrayList<String> composition = new ArrayList<String>();
			String next = scan.next();
			if(next.substring(0,1).equals("\"")) { // if multiple together
				scan.useDelimiter("\"");
				composition.add(next.substring(1,next.length()));
				Scanner compScan = new Scanner(scan.next());
				compScan.useDelimiter(",");
				while(compScan.hasNext()) {
					composition.add(compScan.next());
				}
				compScan.close();
				scan.useDelimiter(",|\\n");
				scan.next();//skip "
			} else {
				composition.add(next);
			}
			//Direction
			String direction = scan.next();
			direction = direction.substring(0,direction.length()-1);// remove enter at the end
			VehicleTask vehicleTask = new VehicleTask(name, from, to, startTime, endTime, distance, pullIn, 
					direction, composition, vehicleGroup);
			vehicleTasks.add(vehicleTask);
			if(scan.hasNext()) {
				name = scan.next();
			} else {
				name = "";
			}
		}
		scan.close();
		return vehicleTasks;
	}

	/*
	 * Initialise, by reading the data file KM list, all vehicle units.
	 */
	private static ArrayList<VehicleUnit> initialiseVehicleUnits(VehicleGroup[] vehicleGroups) throws Exception{
		ArrayList<VehicleUnit> vehicleUnits = new ArrayList<VehicleUnit>(); //167 vehicles
		//Scan data file
		@SuppressWarnings("resource")
		Scanner scan = new Scanner(new File("CSV Files/KM list.csv"));
		scan.useLocale(Locale.ENGLISH);
		scan.useDelimiter(",");
		scan.nextLine();
		while(scan.hasNextLine()) {
			//Name
			String name = scan.next();
			//Group
			VehicleGroup group;
			int seriesID = scan.nextInt();
			if(seriesID==5300) {
				group = vehicleGroups[0];
			} else if(seriesID == 5400) {
				group = vehicleGroups[1];
			}else if(seriesID == 5500) {
				group = vehicleGroups[2];
			}else if(seriesID == 5600) {
				group = vehicleGroups[3];
			}else if(seriesID == 5700) {
				group = vehicleGroups[4];
			}else {
				throw new Exception("Vehicle unit does not belong to any group");
			}
			//Drive KM
			double drivenKM = (double) scan.nextInt();
			// Washing date; calculate by day, since we know 
			Calendar currDate = Calendar.getInstance();
			Scanner dateScan = new Scanner(scan.next());
			dateScan.useDelimiter("/");
			int currDate_day = dateScan.nextInt();
			int currDate_month = dateScan.nextInt();
			int currDate_year = dateScan.nextInt();
			currDate.set(currDate_year, currDate_month, currDate_day);
			dateScan.close();
			Calendar washDate = Calendar.getInstance();
			dateScan = new Scanner(scan.next());
			dateScan.useDelimiter("/");
			int washDate_day = dateScan.nextInt();
			int washDate_month = dateScan.nextInt();
			int washDate_year = dateScan.nextInt();
			washDate.set(washDate_year, washDate_month, washDate_day);
			dateScan.close();
			int prevWash =daysBetween(currDate, washDate);
			scan.nextLine(); //current line done scanning
			VehicleUnit vh = new VehicleUnit(name, group, drivenKM, prevWash);
			vehicleUnits.add(vh);
		}
		scan.close();
		return vehicleUnits;
	}

	/**
	 * Calculates the days between two calendar dates (copied from stackoverflow.com)
	 * @param startDate Date one
	 * @param endDate Date two
	 * @return The number of days between two calendar dates
	 */
	private static int daysBetween(Calendar startDate, Calendar endDate) {
		long end = endDate.getTimeInMillis();
		long start = startDate.getTimeInMillis();
		return (int) TimeUnit.MILLISECONDS.toDays(Math.abs(end - start));
	}

	// Hard coded method to define the vehicle groups.
	private static VehicleGroup[] defineVehicleGroups(Emplacement[] emp) throws FileNotFoundException {
		//Scan data file containing maintenance types data
		Scanner scan = new Scanner(new File("CSV Files/Series Maintenance Work.csv"));
		scan.useDelimiter(",");
		scan.useLocale(Locale.ENGLISH);
		scan.nextLine();
		int readGroup = scan.nextInt();

		VehicleGroup[] units=new VehicleGroup[5];
		// Group 5300
		HashMap<Emplacement, Integer> unitsAvailable=new HashMap<>();
		unitsAvailable.put(emp[2], 26); unitsAvailable.put(emp[1], 37);
		int[] afterKMs_53_54 = {12500, 25000, 50000, 100000, 200000, 400000, 500000};
		units[0]=new VehicleGroup(30, "MBOB", 5300, unitsAvailable, false, defineMaintenance(readGroup, 5300, afterKMs_53_54, scan));
		//Group 5400
		unitsAvailable=new HashMap<>();
		unitsAvailable.put(emp[2], 15); unitsAvailable.put(emp[1], 3);
		readGroup = 5400;
		units[1]=new VehicleGroup(30, "MBOS", 5400, unitsAvailable, false, defineMaintenance(readGroup, 5400, afterKMs_53_54, scan));
		//Group 5500
		unitsAvailable=new HashMap<>();
		unitsAvailable.put(emp[2], 0); unitsAvailable.put(emp[1], 22);
		int[] afterKMs_55_56_57 = {12500, 25000, 50000, 100000, 200000, 300000, 600000, 800000, 1200000, 2400000, 2550000};
		readGroup = 5500;
		units[2]=new VehicleGroup(43, "MR5G", 5500, unitsAvailable, true,defineMaintenance(readGroup, 5500, afterKMs_55_56_57, scan));
		//Group 5600
		unitsAvailable=new HashMap<>();
		unitsAvailable.put(emp[2], 27); unitsAvailable.put(emp[1], 15);
		readGroup = 5600;
		units[3]=new VehicleGroup(43, "M5G3", 5600, unitsAvailable, true, defineMaintenance(readGroup, 5600, afterKMs_55_56_57, scan));
		//Group 5700
		unitsAvailable=new HashMap<>();
		unitsAvailable.put(emp[2], 20); unitsAvailable.put(emp[1], 2);
		readGroup = 5700;
		units[4]=new VehicleGroup(43, "H5G3", 5700, unitsAvailable, true, defineMaintenance(readGroup, 5700, afterKMs_55_56_57, scan));
		ArrayList<VehicleGroup> couple=new ArrayList<VehicleGroup>();
		couple.add(units[1]); units[0].setCouplePossibilities(couple);
		couple=new ArrayList<>();
		couple.add(units[0]); units[1].setCouplePossibilities(couple);
		couple=new ArrayList<>();
		couple.add(units[2]); couple.add(units[3]); units[4].setCouplePossibilities(couple);
		couple=new ArrayList<>();
		couple.add(units[3]); couple.add(units[4]); units[2].setCouplePossibilities(couple);
		couple=new ArrayList<>();
		couple.add(units[4]); couple.add(units[2]); units[3].setCouplePossibilities(couple);
		return units;
	}

	/**
	 * Reads all maintenance for a vehicle group
	 * @param group The vehicle group we are reading from the file
	 * @param currGroup The vehicle group series ID
	 * @param afterKMs An array containing the number of KMs after which a maintenance has to happen for each maintenance
	 * @param scan The scanner of the maintenance data file
	 * @return An array containing all maintenance for the vehicle group
	 */
	private static Maintenance[] defineMaintenance(int group, int currGroup, int[] afterKMs, Scanner scan) {
		Maintenance[] maintenance = new Maintenance[afterKMs.length];
		int rank = 1;
		while(group==currGroup) {
			String name = scan.next();
			double duration = scan.nextDouble()*60;
			maintenance[rank-1]=new Maintenance(name, rank, duration, afterKMs[rank-1]);
			rank++;
			scan.nextLine();
			if(!scan.hasNextInt()) {
				break;
			}
			group = scan.nextInt();
		}
		return maintenance;	
	}

	// Hardcoded emplacement definitions (tracks in order of number).
	private static Emplacement[] defineTracks() {
		Emplacement[] emplacements=new Emplacement[3];
		Track[] tracks=new Track[15]; 
		tracks[3]=new Track(365, "Track 330", "GVW", true, false, false);
		tracks[4]=new Track(369, "Track 331","GVW", true, false, false);
		tracks[5]=new Track(363, "Track 332", "GVW", true, false, false);
		tracks[6]=new Track(363, "Track 333", "GVW", true, false, false);
		tracks[7]=new Track(363, "Track 334", "GVW", true, false, false);
		tracks[8]=new Track(364, "Track 335", "GVW", true, false, false);
		tracks[9]=new Track(365, "Track 336", "GVW", true, false, false);
		tracks[10]=new Track(365, "Track 337", "GVW", true, false, false);
		tracks[11]=new Track(402, "Track 361", "GVW", true, false, false);
		tracks[12]=new Track(403, "Track 362", "GVW", true, false, false);
		tracks[13]=new Track(339, "Track 363", "GVW", true, false, false);
		tracks[14]=new Track(339, "Track 364", "GVW", true, false, false);
		tracks[0]=new Track(65, "Track 322", "GVW", true, false, true);
		tracks[1]=new Track(65, "Track 324", "GVW", true, false, true);
		tracks[2]=new Track(45, "Track 329", "GVW", true, true, false);
		emplacements[2]=new Emplacement(tracks, "GVW");
		tracks=new Track[14];
		tracks[0]=new Track(345, "Track 121", "WHV", true, false, false);
		tracks[1]=new Track(345, "Track 122", "WHV", true, false, false);
		tracks[2]=new Track(329, "Track 123", "WHV", true, false, false);
		tracks[3]=new Track(331, "Track 124", "WHV", true, false, false);
		tracks[4]=new Track(324, "Track 125", "WHV", true, false, false);
		tracks[5]=new Track(325, "Track 126", "WHV", true, false, false);
		tracks[6]=new Track(320, "Track 127", "WHV", true, false, false);
		tracks[7]=new Track(320, "Track 128", "WHV", true, false, false);
		tracks[8]=new Track(347, "Track 129", "WHV", true, false, false);
		tracks[9]=new Track(350, "Track 130", "WHV", true, false, false);
		tracks[11]=new Track(125, "Track 133", "WHV", true, false, true);
		tracks[12]=new Track(125, "Track 134", "WHV", true, false, true);
		tracks[13]=new Track(125, "Track 135", "WHV", true, false, true);
		tracks[10]=new Track(45, "Track 132", "WHV", true, true, false);
		emplacements[1]=new Emplacement(tracks, "WHV");
		tracks=new Track[4];
		tracks[0]=new Track(280, "Track 1", "AKS", false, false, false);
		tracks[1]=new Track(280, "Track 2", "AKS", false, false, false);
		tracks[2]=new Track(280, "Track 3", "AKS", false, false, false);
		tracks[3]=new Track(280, "Track 4", "AKS", false, false, false);
		emplacements[0]=new Emplacement(tracks, "AKS");
		return emplacements;
	}
}