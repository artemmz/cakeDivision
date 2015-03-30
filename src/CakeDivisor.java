import java.util.*;

/**
* CakeDivisor class helps to divide a cake (convex polygon)
* into 4 equal parts with 2 perpendicular lines
*/
public class CakeDivisor{

	static final double EPS = 1e-9; 
	private static int vertexNumb; // total number of vertexes
	private static List<Point> cake = new ArrayList<Point>();
	private static Point center;
	private static double cakeArea;
	
	public static void main(String[] args){
		createCake();
		double minAngle = 0;
		double maxAngle = Math.PI/2;
		double curAngle = minAngle; 
		double maxDiff = areaDiff(maxAngle);
		double minDiff = areaDiff(minAngle);
		boolean success = false;
		if (Math.abs(minDiff) <= EPS*cakeArea){
			success = true;
		}
		while (!success && Math.abs(maxDiff - minDiff) > EPS*cakeArea){
			curAngle = (minAngle + maxAngle)/2;
			double curDiff = areaDiff(curAngle);
			if (curDiff == 0){
				success = true;
			}else if (	(minDiff > 0 && curDiff < 0) ||
						(minDiff < 0 && curDiff > 0)	){
				maxDiff = curDiff;
				maxAngle = curAngle;
			}else{
				minDiff = curDiff;
				minAngle = curAngle;
			}
		}
		
		System.out.printf("%f %f\n", center.x, center.y);
		System.out.printf("%f\n",curAngle*180/Math.PI);
	}
	
	private static void createCake(){
		Scanner scan= new Scanner(System.in);
		try{
			vertexNumb = scan.nextInt();
			if (vertexNumb < 3){
				exitBirthday();
			}
			for (int i=0; i<vertexNumb; i++){
				double x = scan.nextDouble();
				double y = scan.nextDouble();
				cake.add(new Point(x,y));
			}
			checkConvex(cake);
			cake.add(cake.get(0));
			cakeArea = area(cake);
			if (cakeArea == 0){
				exitBirthday();
			}
		}catch(InputMismatchException ex){
			exitBirthday();
		}
	}
	
	private static void checkConvex(List<Point> pol){
		if (pol.size() == 3) return;
		boolean sign = false;
		int n = pol.size();
		for (int i=0; i < n; i++){
			double dx1 = pol.get((i+2)%n).x-pol.get((i+1)%n).x;
		    double dy1 = pol.get((i+2)%n).y-pol.get((i+1)%n).y;
		    double dx2 = pol.get(i).x-pol.get((i+1)%n).x;
		    double dy2 = pol.get(i).y-pol.get((i+1)%n).y;
		    double crossproduct = dx1*dy2 - dy1*dx2;
		    if (i == 0){
		    	sign = crossproduct > 0;
		    }else{
		    	if (sign != (crossproduct > 0)){
		    		exitBirthday();
		    	}
		    }
		}
	}
	
	private static void exitBirthday(){
		System.out.println("-1");
		System.exit(0);
	}
	
	private static double area(List<Point> pol){
		double res = 0;
		for (int i = 0; i < pol.size()-1; i++){
			res +=	(pol.get(i+1).x - pol.get(i).x) * 
					(pol.get(i+1).y + pol.get(i).y);
		}
		return Math.abs(res)/2;
	}
	
	private static double sqrDistance(Point p1,Point p2){
		return Math.pow(p1.x - p2.x,2) + Math.pow(p1.y - p2.y,2);
	}
	
	private static double areaDiff(double angle){
		Point[] firSect = bisectionPoints(angle);
		Point[] secSect = bisectionPoints(angle+Math.PI/2);
		center = linesInter(new Line(firSect[0],firSect[1]),
							new Line(secSect[0],secSect[1]));
		int stop = 0; //needed to find 1/4 
		if (firSect[0].preVertex > secSect[0].preVertex){
			stop = 1;
		}else if(firSect[0].preVertex == secSect[0].preVertex){ 
			// if perpendicular sections cross the same side
			double firDist = sqrDistance(firSect[0],
										 cake.get(firSect[0].preVertex));
			double secDist = sqrDistance(secSect[0],
										 cake.get(secSect[0].preVertex));
			if (firDist > secDist){
				stop = 1;
			}
		}
		List<Point> quarterCake = new ArrayList<Point>();
		quarterCake.add(center);
		quarterCake.add(firSect[0]);
		for (int i=firSect[0].preVertex + 1; i <= secSect[stop].preVertex; i++){
			quarterCake.add(cake.get(i));
		}
		quarterCake.add(secSect[stop]);
		quarterCake.add(center);  
		return cakeArea/4 - area(quarterCake);
	}
	
	private static Point[] bisectionPoints(double angle){
		Point[] bPoints = new Point[2]; // always 2 intersection points
		int lowNum = 0; //number of lowest vertex
		double lowSect = getYCoord(cake.get(lowNum),angle); 
		double highSect = lowSect; // highest and lowest sections
		double curSect;
		for (int i=1; i<vertexNumb; i++){
			curSect = getYCoord(cake.get(i),angle);
			if (curSect > highSect){
				highSect = curSect;
			}else if (curSect < lowSect){
				lowSect = curSect;
				lowNum = i;
			}
		}
		
		double sMin = 0;
		double sMax = cakeArea;
		while (Math.abs(sMax-sMin) > EPS*cakeArea){
			curSect = (lowSect + highSect)/2;
			int interCount = 0;
			for (int i=0; i < vertexNumb; i++){
				double curY = getYCoord(cake.get(i),angle) - curSect;
				double nextY = getYCoord(cake.get(i+1),angle) - curSect;
				if (curY*nextY < 0){ // if section crosses side
					Line side = new Line(cake.get(i),cake.get(i+1));
					Line section = new Line(angle,curSect);
					Point intPoint = linesInter(side,section);
					intPoint.preVertex = i;
					bPoints[interCount] = intPoint;
					interCount++;
				}else if (curY == 0){ // if section goes through vertex
					Point intPoint = cake.get(i);
					intPoint.preVertex = i;
					bPoints[interCount] = intPoint;
					interCount++;
				}
				if (interCount == 2) break; // found all intersections
			}
			
			List<Point> halfCake = new ArrayList<Point>();
			halfCake.add(bPoints[0]);
			for (int i=bPoints[0].preVertex; i<=bPoints[1].preVertex; i++){
				halfCake.add(cake.get(i));
			}
			halfCake.add(bPoints[1]);
			halfCake.add(bPoints[0]); 
			sMin = area(halfCake);
			sMax = cakeArea - sMin;
			if (lowNum > bPoints[0].preVertex && lowNum <= bPoints[1].preVertex){
				if (sMin > sMax){
					highSect = curSect;
				}else{
					lowSect = curSect;
				}
			}else{
				if (sMin > sMax){
					lowSect = curSect;
				}else{
					highSect = curSect;
				}
			}
		}
		return bPoints;
	}
	
	private static Point linesInter(Line l1,Line l2){
		double denom = l1.a*l2.b - l2.a*l1.b;
		if (denom == 0){
			return null; //parallel
		}else{
			double x = (l1.b*l2.c - l2.b*l1.c)/denom;
			double y = (l1.c*l2.a - l2.c*l1.a)/denom;
			return new Point(x,y);
		}
	}
	
	private static double getYCoord(Point p,double angle){
		double sinA = Math.sin(angle);
		double cosA = Math.cos(angle);
		return sinA*p.x - cosA*p.y; 
	}
	
	private static class Point{
		public double x;
		public double y;
		public int preVertex; // nearest previous vertex 
		
		public Point(double x,double y){
			this.x = x;
			this.y = y;
		}
	}
	
	private static class Line{
		public double a;
		public double b;
		public double c; // ax+by+c=0
		
//		(y1-y2)x+(x2-x1)y+(x1y2-x2y1) = 0 
		public Line(Point p1,Point p2){
			this.a = p1.y - p2.y;
			this.b = p2.x - p1.x;
			this.c = p1.x*p2.y - p2.x*p1.y;
		}
		
//		y=kx+b --> -kx+y-b=0 --> ax+by+c=0
		public Line(double angle,double perpend){
			if (Math.abs(angle) == Math.PI/2){
				this.a = 1;
				this.b = 0;
				this.c = -perpend*Math.signum(angle);
			}else{
				this.a = -Math.tan(angle);
				this.b = 1;
				this.c = perpend/Math.cos(angle);
			}
		}
	}
}
