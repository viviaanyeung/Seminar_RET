package basicPlanner;

import java.util.ArrayList;
import java.util.HashMap;

import general.Metro;
import general.VehicleGroup;
import general.VehicleUnit;
import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;

public class ArrivalDepartureMatcher {
	// TODO: NOTE THAT THE ARRIVALS AND DEPARTURES ARE ASSUMED TO INCLUDE FICTIONAL ARRIVALS AND DEPARTURES FOR METROS THAT STAY ON THE SHUNT YARD.
	private ArrayList<Metro> arrivingVehicles;
	private ArrayList<Metro> departingVehicles;
	private HashMap<ArrayList<VehicleGroup>, Integer> supplyArrivingMetroCompositions;
	private HashMap<ArrayList<VehicleGroup>, Integer> demandDepartingMetroCompositions;
	private ArrayList<ArrayList<VehicleGroup>> intermediateCompositions;
	private boolean[][] arcsArrivalIntermediate;
	private boolean[][] arcsIntermediateDeparting;
	// Cplex objects
	private IloCplex cplex;
	private IloNumVar[][] flowArcsArrivalIntermediate;
	private IloNumVar[][] flowArcsIntermediateDeparture;

	public ArrivalDepartureMatcher(ArrayList<Metro> arrivals, ArrayList<Metro> departures, VehicleGroup[] vehicleGroups) throws IloException {
		this.cplex=new IloCplex();
		this.cplex.setOut(null); this.cplex.setWarning(null);
		// NOTE: A BETTER SOLUTION IS OFFERED IN https://dl.acm.org/doi/abs/10.1145/62212.62249 (better than cplex, though we work with a linear program.
		this.arrivingVehicles=arrivals;
		this.departingVehicles=departures;
		this.supplyArrivingMetroCompositions=new HashMap<>();
		this.demandDepartingMetroCompositions=new HashMap<>();
		this.intermediateCompositions=new ArrayList<>();
		ArrayList<VehicleGroup> composition=new ArrayList<>(3);
		// Singleton defined
		composition.add(vehicleGroups[0]);
		this.supplyArrivingMetroCompositions.put(composition,0);
		this.demandDepartingMetroCompositions.put(composition, 0);
		this.intermediateCompositions.add(composition);

		for(VehicleGroup firstUnit: vehicleGroups) {		// Seems inefficient, but we have 5 distinct types, so it's 125 times which is very very easy.
			for(VehicleGroup secondUnit: vehicleGroups) {
				composition=new ArrayList<>(3);
				composition.add(firstUnit);
				composition.add(secondUnit);
				this.supplyArrivingMetroCompositions.put(composition,0);
				this.demandDepartingMetroCompositions.put(composition, 0);
				this.intermediateCompositions.add(composition);				
				for(VehicleGroup thirdUnit: vehicleGroups) {
					composition=new ArrayList<>(3);
					composition.add(firstUnit);
					composition.add(secondUnit);
					composition.add(thirdUnit);
					this.supplyArrivingMetroCompositions.put(composition,0);
					this.demandDepartingMetroCompositions.put(composition, 0);
					this.intermediateCompositions.add(composition);			
				}
			}
		}
		for(Metro arrival: this.arrivingVehicles) {
			ArrayList<VehicleGroup> arrivalComposition=new ArrayList<>();
			for(VehicleUnit i: arrival.getComposition()) arrivalComposition.add(i.getGroup());
			this.supplyArrivingMetroCompositions.put(arrivalComposition, this.supplyArrivingMetroCompositions.get(arrivalComposition)+1);
		}
		for(Metro departure: this.departingVehicles) {
			ArrayList<VehicleGroup> departureComposition=new ArrayList<>();
			for(VehicleUnit i: departure.getComposition()) departureComposition.add(i.getGroup());
			this.supplyArrivingMetroCompositions.put(departureComposition, this.supplyArrivingMetroCompositions.get(departureComposition)+1);
		}
		for(ArrayList<VehicleGroup> i: this.intermediateCompositions) {
			if(this.supplyArrivingMetroCompositions.get(i)==0) this.supplyArrivingMetroCompositions.remove(i);
			if(this.demandDepartingMetroCompositions.get(i)==0) this.demandDepartingMetroCompositions.remove(i);
		}
		this.defineArcs();
		this.addObjective();
		this.addConstraints();
	}

	private void addConstraints() throws IloException {
		// Balance Constraints arriving metros
		int arrivalIndex=0;
		for(ArrayList<VehicleGroup> arrival: this.supplyArrivingMetroCompositions.keySet()) {
			IloNumExpr outflow=this.cplex.constant(0);
			for(int i=0;i<this.intermediateCompositions.size();i++) {
				if(this.arcsArrivalIntermediate[arrivalIndex][i]) {	// An arc exists
					outflow=this.cplex.sum(outflow, this.flowArcsIntermediateDeparture[arrivalIndex][i]);
				}
			}
			this.cplex.addEq(outflow, this.cplex.constant(this.supplyArrivingMetroCompositions.get(arrival)));
			arrivalIndex++;
		}
		// Balance Constraints departing metros
		int departureIndex=0;
		for(ArrayList<VehicleGroup> departure: this.demandDepartingMetroCompositions.keySet()) {
			IloNumExpr inflow=this.cplex.constant(0);
			for(int i=0;i<this.intermediateCompositions.size();i++) {
				if(this.arcsIntermediateDeparting[i][departureIndex]) {	// An arc exists
					inflow=this.cplex.sum(inflow, this.flowArcsIntermediateDeparture[i][departureIndex]);
				}
			}
			this.cplex.addEq(inflow, this.cplex.constant(this.demandDepartingMetroCompositions.get(departure)));
			departureIndex++;
		}
		// Balance Constraints intermediates
		for(int i=0;i<this.intermediateCompositions.size();i++) {
			if(this.intermediateCompositions.get(i).size()==1) continue;	// Singleton: Balance not preserved.
			IloNumExpr outflow=this.cplex.constant(0);
			IloNumExpr inflow=this.cplex.constant(0);
			for(int j=0;j<this.supplyArrivingMetroCompositions.keySet().size();j++) {
				if(this.arcsArrivalIntermediate[j][i]) {	// An arc exists
					inflow=this.cplex.sum(inflow, this.flowArcsArrivalIntermediate[j][i]);
				}
			}
			for(int j=0;j<this.demandDepartingMetroCompositions.keySet().size();j++) {
				if(this.arcsIntermediateDeparting[i][j]) {	// An arc exists
					outflow=this.cplex.sum(outflow, this.flowArcsIntermediateDeparture[i][j]);
				}
			}
			this.cplex.addEq(inflow,  outflow);
		}
	}

	private void addObjective() throws IloException {
		IloNumExpr obj=this.cplex.constant(0);
		int arrivalIndex=0; int intermediateIndex=0;
		for(ArrayList<VehicleGroup> arrival: this.supplyArrivingMetroCompositions.keySet()) {
			for(ArrayList<VehicleGroup> intermediate: this.intermediateCompositions) {
				if(this.arcsArrivalIntermediate[arrivalIndex][intermediateIndex]) {	//An arc exists
					int costs=Math.abs(arrival.size()-intermediate.size())+1;
					obj=this.cplex.sum(obj, this.cplex.prod(this.cplex.constant(costs), this.flowArcsArrivalIntermediate[arrivalIndex][intermediateIndex]));
				}
				intermediateIndex++;
			}
			arrivalIndex++;
		}
		// Note that we only consider splitting vehicles, not merging them, in the formulation of Lentink.
		this.cplex.addMinimize(obj);
	}

	/**
	 * Follow constraints by Lentink.
	 * @throws IloException 
	 */
	private void defineArcs() throws IloException {
		this.arcsArrivalIntermediate=new boolean[this.supplyArrivingMetroCompositions.keySet().size()][this.intermediateCompositions.size()];
		this.arcsIntermediateDeparting=new boolean[this.intermediateCompositions.size()][this.demandDepartingMetroCompositions.keySet().size()];
		// NOTE: THE FOLLOWING ARRAYS HAVE SOME NULL VALUES INCASE THE ARCS ARE NOT DEFINED.d
		this.flowArcsArrivalIntermediate=new IloNumVar[this.supplyArrivingMetroCompositions.keySet().size()][this.intermediateCompositions.size()];
		this.flowArcsIntermediateDeparture=new IloNumVar[this.intermediateCompositions.size()][this.demandDepartingMetroCompositions.keySet().size()];
		int arrivalIndex=0; int intermediateIndex=0;
		for(ArrayList<VehicleGroup> arrival: this.supplyArrivingMetroCompositions.keySet()) {
			for(ArrayList<VehicleGroup> intermediate: this.intermediateCompositions) {
				if(intermediate.size()==1) {
					this.arcsArrivalIntermediate[arrivalIndex][intermediateIndex]=true;	// Group of 1 unit.
					this.flowArcsArrivalIntermediate[arrivalIndex][intermediateIndex]=this.cplex.numVar(0, Double.POSITIVE_INFINITY);
				}
				else if(arrival.equals(intermediate)) {
					this.arcsArrivalIntermediate[arrivalIndex][intermediateIndex]=true;
					this.flowArcsArrivalIntermediate[arrivalIndex][intermediateIndex]=this.cplex.numVar(0, Double.POSITIVE_INFINITY);
				}
				else if(arrival.size()==3 && intermediate.size()==2) {
					if((arrival.get(0).equals(intermediate.get(0))&&arrival.get(1).equals(intermediate.get(1)))||((arrival.get(1).equals(intermediate.get(0))&&arrival.get(2).equals(intermediate.get(1))))){
						this.arcsArrivalIntermediate[arrivalIndex][intermediateIndex]=true;
						this.flowArcsArrivalIntermediate[arrivalIndex][intermediateIndex]=this.cplex.numVar(0, Double.POSITIVE_INFINITY);
					}
				}
				intermediateIndex++;	
			}
			arrivalIndex++;
		}
		int departureIndex=0; intermediateIndex=0;
		for(ArrayList<VehicleGroup> intermediate: this.intermediateCompositions) {
			for(ArrayList<VehicleGroup> departure: this.demandDepartingMetroCompositions.keySet()) {
				if(intermediate.size()==1) {
					this.arcsIntermediateDeparting[intermediateIndex][departureIndex]=true;	// Group of 1 unit.
					this.flowArcsIntermediateDeparture[intermediateIndex][departureIndex]=this.cplex.numVar(0, Double.POSITIVE_INFINITY);
				}
				else if(departure.equals(intermediate)) {
					this.arcsIntermediateDeparting[intermediateIndex][departureIndex]=true;
					this.flowArcsIntermediateDeparture[intermediateIndex][departureIndex]=this.cplex.numVar(0, Double.POSITIVE_INFINITY);
				}
				else if(intermediate.size()==3 && departure.size()==2) {
					if((departure.get(0).equals(intermediate.get(0))&&departure.get(1).equals(intermediate.get(1)))||((departure.get(0).equals(intermediate.get(1))&&departure.get(1).equals(intermediate.get(2))))){
						this.arcsIntermediateDeparting[intermediateIndex][departureIndex]=true;
						this.flowArcsIntermediateDeparture[intermediateIndex][departureIndex]=this.cplex.numVar(0, Double.POSITIVE_INFINITY);
					}
				}
				departureIndex++;	
			}
			intermediateIndex++;
		}
	}
	public boolean solve() throws IloException {return this.cplex.solve();}
	// Accessor methods
	public ArrayList<Metro> getArrivals() {return this.arrivingVehicles;}
	public ArrayList<Metro> getDepartures() {return this.departingVehicles;}
}
