package visualisation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.text.SimpleDateFormat;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import general.Emplacement;
import general.MetroMovement;
import general.MetroParking;
import general.Track;
import general.VehicleUnit;
/**
 * This class is used to plot the solution in a simple pop-up.
 * @author Marten Koole
 *
 */
public class ShuntDraw extends JFrame {
	private static final long serialVersionUID = 1L;
	private Emplacement[] emplacements;
	private HashMap<Track, ArrayList<MetroMovement>> metroMoves;
	private HashMap<Track, ArrayList<MetroParking>> metroParkings;
	private HashMap<MetroParking, Rectangle2D> parkingBoxMap;
	private Date startDateTime;
	private Date endDateTime;
	private final JFrame frame;
	private JComponent trackHeader;
	private JComponent timeHeader;
	private int totalHeight;
	private int totalWidth;
	private int trackHeight;
	private SimpleDateFormat dateFormat;
	/**
	 * 
	 * @param t
	 * @param m
	 * @param metroMovements
	 * @param start
	 * @param end
	 */
	public ShuntDraw(Emplacement[] e, ArrayList<MetroMovement> metroMovements, Date start, Date end) {
		this.emplacements=e;
		this.metroMoves=new HashMap<>();
		this.metroParkings=new HashMap<>();
		// Make a HashMap of track to movement, where the lists are sorted by time.
		int maxCapacity=0; 
		int numberTracks=0;
		for(Emplacement emp: this.emplacements) {
			Track[] tracks=emp.getTracks();
			for(Track track : tracks) {
				if(track.getCapacity()>maxCapacity) maxCapacity=track.getCapacity();
				this.metroMoves.put(track, new ArrayList<>());
				this.metroParkings.put(track, new ArrayList<>());
				numberTracks++;
			}
		}
		for(MetroMovement m: metroMovements) this.metroMoves.get(m.getTrack()).add(m);
		for(Emplacement emp : this.emplacements) {
			for(Track track : emp.getTracks()) Collections.sort(this.metroMoves.get(track));
		}
		this.startDateTime=start;
		this.endDateTime=end;
		// 100 pixels for 10 minutes.
		this.totalWidth=(int) ((endDateTime.getTime()-startDateTime.getTime())/60000)*100;
		this.trackHeight=(int) Math.round(maxCapacity*1.2);
		this.totalHeight=numberTracks*(this.trackHeight);	// A bit of margin on each track.
		this.dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm");
		// Lay-out setting
		this.frame = new JFrame("Track Plan");
		this.frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		this.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
		this.frame.getContentPane().setBackground(Color.black);
		this.frame.getContentPane().setLayout(new BorderLayout());  
		MyPanel panel=new MyPanel(this.totalWidth, this.totalHeight);
		this.trackHeader=new TrackHeader(this.totalHeight,80);
		this.timeHeader=new TimeHeader(50, this.totalWidth);
		//Create the corners.
		JScrollPane scrollablePanel=new JScrollPane(panel);
		scrollablePanel.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);  
		scrollablePanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);  
		JScrollPane scrollableTrackHeader=new JScrollPane(this.trackHeader);
		scrollableTrackHeader.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);  
		scrollableTrackHeader.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
		scrollableTrackHeader.getVerticalScrollBar().setModel(scrollablePanel.getVerticalScrollBar().getModel());;
		JScrollPane scrollableTimeHeader=new JScrollPane(this.timeHeader);
		scrollableTimeHeader.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollableTimeHeader.getHorizontalScrollBar().setModel(scrollablePanel.getHorizontalScrollBar().getModel());
		scrollableTimeHeader.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);  
		this.frame.getContentPane().add(scrollablePanel, BorderLayout.CENTER);
		this.frame.getContentPane().add(scrollableTrackHeader, BorderLayout.WEST);
		this.frame.getContentPane().add(scrollableTimeHeader, BorderLayout.NORTH);
	}
	// Method to actually create a drawing.
	public void createDrawing() {
		this.frame.setVisible(true);
		repaint();
	}
	/**
	 * Nested class for generating and drawing in a panel.
	 * @author Marten Koole
	 *
	 */
	private class MyPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		/**
		 * This method draws in the panel.
		 * @param g The standard settings to draw that can be modified in the method.
		 */
		public MyPanel(int width, int height) {
			this.setBackground(Color.white);
			this.setPreferredSize(new Dimension(width,height));
		}
		public void paint(Graphics g)
		{
			super.paintComponent(g);
			Graphics2D g2=(Graphics2D) g;
			g2.setColor(Color.LIGHT_GRAY);
			// Make a grid.
			int trackHeightPixels=(int) Math.round(trackHeight/5.0);
			int currentHeight=trackHeightPixels;
			for(Emplacement emp: emplacements) {
				for(int i=0;i<emp.getTracks().length;i++) {
					g2.drawLine(0, currentHeight, totalWidth, currentHeight);
					currentHeight+=trackHeight/5.0;	// 0.5 pixel per meter.
				}
			}
			long endTime=endDateTime.getTime();
			long startTime=startDateTime.getTime();
			int currentTime=100;
			for(long time=startTime; time<endTime;time+=600000) {	// Steps of 2 minutes (i.e. 2*60*1000 milliseconds)
				g2.drawLine(currentTime, 0, currentTime, totalHeight);
				currentTime+=100;	// 100 pixels for 10 minutes.
			}
			// Draw track capacity.
			currentHeight=trackHeightPixels;
			g2.setColor(new Color(242,242,242));
			for(Emplacement emp: emplacements) {
				for(Track track: emp.getTracks()) {
					g2.fillRect(0, currentHeight-track.getCapacity()/5, totalWidth, track.getCapacity()/5);	// left-x coordinate, top-y coordinate, width, height.
					currentHeight+=trackHeight/5.0;	// 0.5 pixel per meter.
				}
			}
			// Draw metro movements. We draw each on the spot on the track where it should actually stand (that is, we draw it to scale).
			currentHeight=trackHeight/5;
			for(Emplacement emp: emplacements) {
				for(Track track : emp.getTracks()) {
					ArrayList<MetroParking> trackParkings=metroParkings.get(track);
					boolean[][] moreOnASide=new boolean[trackParkings.size()][trackParkings.size()];
					for(int i=0;i<trackParkings.size();i++) {
						MetroParking firstParking=trackParkings.get(i);
						for(int j=i+1;j<trackParkings.size();j++) {
							MetroParking secondParking=trackParkings.get(j);
							if(firstParking.getStartTime().getTime()>secondParking.getEndTime().getTime()) continue;	// No overlap.
							if(firstParking.getEndTime().getTime()<secondParking.getStartTime().getTime()) continue;	// No overlap.
							// We know the metros are on the same track and partially during the same time interval -> Determine which was first.
							boolean firstArrivalASide=firstParking.getArrival().isASide();
							boolean secondArrivalASide=secondParking.getArrival().isASide();
							if(firstArrivalASide&&!secondArrivalASide) {	// First arrives from A, second from B -> First more to A Side
								moreOnASide[i][j]=true;
							}
							else if(!firstArrivalASide&&secondArrivalASide) {
								moreOnASide[j][i]=true;
							}
							else {	// Now, it depends on the time
								if(firstArrivalASide) {	// Both on A side: Last to arrive is more towards A.
									if(firstParking.getStartTime().getTime()>secondParking.getStartTime().getTime()) {	// i arrives after j.
										moreOnASide[i][j]=true;
									}
									else {
										moreOnASide[j][i]=true;
									}
								}
								else {	// Reversed (both arrive on B side, last to arrive is less towards A).
									if(firstParking.getStartTime().getTime()>secondParking.getStartTime().getTime()) {	// i arrives after j.
										moreOnASide[j][i]=true;
									}
									else {
										moreOnASide[i][j]=true;
									}
								}
							}
						} 
					}
					// Precedences are now defined
					boolean heightsUpdated=false;
					do { heightsUpdated=false;
					for(int i=0;i<trackParkings.size();i++) {
						MetroParking firstParking=trackParkings.get(i);
						int firstLengthPixels=0;
						for(VehicleUnit k: firstParking.getMetro().getComposition()) firstLengthPixels+=k.getGroup().getLength();
						firstLengthPixels/=5;// 0.2 pixel per meter.
						for(int j=i+1;j<trackParkings.size();j++) {
							MetroParking secondParking=trackParkings.get(j);
							int secondLengthPixels=0;
							for(VehicleUnit k: secondParking.getMetro().getComposition()) secondLengthPixels+=k.getGroup().getLength();
							secondLengthPixels/=5;	// 0.2 pixel per meter.
							if(moreOnASide[i][j]) {
								// i below j: Starting height from bottom of track is 0 for i, j must be higher then where i starts + length i.
								if(secondParking.getStartingHeightDrawing()<(firstParking.getStartingHeightDrawing()+firstLengthPixels)){
									secondParking.setStartingHeightDrawing(firstParking.getStartingHeightDrawing()+firstLengthPixels);
									heightsUpdated=true;
								}
							}
							else if(moreOnASide[j][i]) {
								// j below i.
								if(firstParking.getStartingHeightDrawing()<(secondParking.getStartingHeightDrawing()+secondLengthPixels)){
									firstParking.setStartingHeightDrawing(secondParking.getStartingHeightDrawing()+secondLengthPixels);
									heightsUpdated=true;
								}
							}
						}
					}
					}while (heightsUpdated);
					// Now, we start drawing the rectangles and updating the parkingBoxMap.
					for(MetroParking parking: trackParkings) {	// Our currentHeight represents the bottom of the track.
						int lengthPixels=0;
						for(VehicleUnit k: parking.getMetro().getComposition()) lengthPixels+=k.getGroup().getLength();
						lengthPixels/=5;// 0.2 pixel per meter.		
						Rectangle2D box=new Rectangle(((int) (parking.getStartTime().getTime()-startTime)/600), currentHeight-(parking.getStartingHeightDrawing()+lengthPixels), (int) (parking.getEndTime().getTime()-parking.getStartTime().getTime())/600, lengthPixels); // left-x coordinate, top-y coordinate, width, height.
						parkingBoxMap.put(parking, box);
						g2.setColor(parking.getMetro().getComposition().get(0).getGroup().getColor());	// TODO: UPDATE TO HANDLE MULTIPLE COLORS FOR MULTIPLE UNITS (OR SOMETHING LIKE THAT).d
						g2.fill(box);
						g2.setColor(Color.black);
						if(parking.getArrival().isASide()) {	// Arrives from bottom.
							Point from = new Point((int) ((parking.getStartTime().getTime()-startTime)/600), currentHeight-(parking.getStartingHeightDrawing())+20);
							Point to= new Point((int) ((parking.getStartTime().getTime()-startTime)/600), currentHeight-(parking.getStartingHeightDrawing())-20);
							g2.fill(createArrow(from, to));
						}
						else {
							Point from = new Point((int) ((parking.getStartTime().getTime()-startTime)/600), currentHeight-parking.getStartingHeightDrawing()-lengthPixels-20);
							Point to= new Point((int) ((parking.getStartTime().getTime()-startTime)/600), currentHeight-parking.getStartingHeightDrawing()-lengthPixels+20);
							g2.fill(createArrow(from, to));
						}
						if(parking.getDeparture().isASide()) {
							Point from = new Point((int) ((parking.getEndTime().getTime()-startTime)/600), parking.getStartingHeightDrawing()-20);
									Point to= new Point((int) ((parking.getEndTime().getTime()-startTime)/600),currentHeight-(parking.getStartingHeightDrawing())+20);
									g2.fill(createArrow(from, to));
						}
						else {
							Point from = new Point((int) ((parking.getEndTime().getTime()-startTime)/600), currentHeight-parking.getStartingHeightDrawing()-lengthPixels+20);
									Point to= new Point((int) ((parking.getEndTime().getTime()-startTime)/600), currentHeight-parking.getStartingHeightDrawing()-lengthPixels-20);
									g2.fill(createArrow(from, to));
						}
					}
					currentHeight+=trackHeight/5.0;	// 0.2 pixel per meter.	Update towards next track.
				}
			}
		}
		/**
		 * 
		 * @param event
		 * @return
		 */
		@Override
		public String getToolTipText(MouseEvent event) {
			int y=event.getY();
			Point p=new Point(event.getX(), event.getY());
			// Define track.
			Track currentTrack=null;
			int currentHeight=0;
			outerloop:
				for(Emplacement emp: emplacements) {
					for(Track track : emp.getTracks()) {
						if(y>=currentHeight&&y<=currentHeight+trackHeight) {
							currentTrack=track; break outerloop;
						}
					}
				}
			if(currentTrack==null) return super.getToolTipText();
			for(MetroParking parking: metroParkings.get(currentTrack)) {
				String t=tooltipForMetroParking(p, parkingBoxMap.get(parking), parking);
				if(t!=null) return t;
			}
			return super.getToolTipText(event);
		}

		protected String tooltipForMetroParking(Point p, Rectangle2D rectangle, MetroParking parking) {
			// Test whether the point is inside the rectangle
			if(rectangle.contains(p)) {
				String arrivingMetro=parking.getArrival().getName();
				String departingMetro=parking.getDeparture().getName();
				String arrivalTime=dateFormat.format(parking.getStartTime());
				String departureTime=dateFormat.format(parking.getEndTime());
				return "<html><b>A</b>: " + arrivingMetro +"<br> <b>D</b>: "+departingMetro+
						"<br> <Time>: "+ arrivalTime+"-"+departureTime+"</html>";
			}
			return null;
		}
	}
	private class TrackHeader extends JPanel {
		private static final long serialVersionUID = 1L;
		public TrackHeader(int height, int width) {
			super();
			this.setPreferredSize(new Dimension(width, height));
			this.setBackground(Color.white);
		}
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int trackHeightPixels=(int) Math.round(trackHeight/5.0);
			int currentHeight=trackHeightPixels/2;
			Graphics2D g2=(Graphics2D) g;
			final int distanceLeft= 10;
			for(Emplacement emp: emplacements) {
				for(Track track: emp.getTracks()) {
					g2.setFont(new Font("Arial", Font.ITALIC, 12));
					g2.drawString(emp.getName(), distanceLeft, currentHeight-25);
					g2.setFont(new Font("Arial", Font.BOLD, 14));
					g2.drawString(track.getName(), distanceLeft, currentHeight);
					currentHeight+=trackHeightPixels;	// 0.2 pixel per meter.
				}
			}
		}
	}

	private class TimeHeader extends JPanel {
		private static final long serialVersionUID = 1L;
		public TimeHeader(int height, int width) {
			super();
			this.setPreferredSize(new Dimension(width, height));
			this.setBackground(Color.white);
		}
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			final int distanceTop=10;
			Graphics2D g2=(Graphics2D) g;
			g2.setFont(new Font("Arial", Font.PLAIN, 10));
			int currentTime=50;
			long endTime=endDateTime.getTime();
			long startTime=startDateTime.getTime();
			for(long time=startTime; time<endTime;time+=600000) {	// Steps of 2 minutes (i.e. 2*60*1000 milliseconds)
				g2.drawString(dateFormat.format(new Date(time)), currentTime, distanceTop);
				currentTime+=100;	// 100 pixels for 10 minutes.
			}
		}
	}
	public static Shape createArrow(Point fromPt, Point toPt) {
		Polygon arrowPolygon = new Polygon();
		arrowPolygon.addPoint(-6,1);
		arrowPolygon.addPoint(3,1);
		arrowPolygon.addPoint(3,3);
		arrowPolygon.addPoint(6,0);
		arrowPolygon.addPoint(3,-3);
		arrowPolygon.addPoint(3,-1);
		arrowPolygon.addPoint(-6,-1);


		Point midPoint = midpoint(fromPt, toPt);

		double rotate = Math.atan2(toPt.y - fromPt.y, toPt.x - fromPt.x);

		AffineTransform transform = new AffineTransform();
		transform.translate(midPoint.x, midPoint.y);
		double ptDistance = fromPt.distance(toPt);
		double scale = ptDistance / 12.0; // 12 because it's the length of the arrow polygon.
		transform.scale(scale, scale);
		transform.rotate(rotate);

		return transform.createTransformedShape(arrowPolygon);
	}

	private static Point midpoint(Point p1, Point p2) {
		return new Point((int)((p1.x + p2.x)/2.0), 
				(int)((p1.y + p2.y)/2.0));
	}
}