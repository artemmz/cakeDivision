package birthday;

import java.util.*;

public class CakeDivisor{

	static final double EPS = 1e-10; // not needed ????
//	private static int vertexNumb; // total number of vertexes
	private static Polygon cake = new Polygon();
	private static double cakeArea;
	
	public static void main(String[] args){
		createCake();
		cakeArea = area(cake);
		System.out.println("area="+area(cake));
	}
	
	private static void createCake(){
		Scanner scan= new Scanner(System.in);
		try{
			int vertexNumb = scan.nextInt();
			for (int i=0;i<vertexNumb;i++){
				double x = scan.nextDouble();
				double y = scan.nextDouble();
				cake.add(new Point(x,y));
			}
		}catch(InputMismatchException ex){
			System.out.println("-1\nincorrect input"); 
		}
	}
	
	private static double area(Polygon pol){
		double res = 0;
		for (int i = 0; i < pol.vertexNumb-1; i++){
			res +=	(pol.get(i + 1).x - pol.get(i).x) * 
					(pol.get(i + 1).y + pol.get(i + 1).y);
		}
		return Math.abs(res)/2;
	}
	
//	private static double areaDiff(double angle){
//		Point
//	}
	
	private static Polygon halfCake(double angle){
		Polygon halfCake = new Polygon();
		Point[] bPoints = new Point[2]; // always 2 intersection points
		int lowNum = 0; //number of lowest vertex
		double lowSect = getYCoord(cake,lowNum,angle); 
		double highSect = lowSect; // highest and lowest sections
		double curSect;
		for (int i=1; i<cake.vertexNumb; i++){
			curSect = getYCoord(cake,i,angle);
			if (curSect > highSect){
				highSect = curSect;
			}else if (curSect < lowSect){
				lowSect = curSect;
			}
		}
		
		double sMin = 0;
		double sMax = cakeArea;
		while (Math.abs(sMax-sMin) > EPS*cakeArea){
			curSect = (lowSect + highSect)/2;
			double curY;
			double nextY;
			int interCount = 0; // number of intersections
			for (int i=0; i < cake.vertexNumb-1; i++){
				curY = getYCoord(cake,i,angle) - curSect;
				nextY = getYCoord(cake,i+1,angle) - curSect;
				if (curY == 0){ // if section goes through vertex
					Point intPoint = cake.get(i);
					intPoint.preVertex = i;
					bPoints[interCount] = intPoint;
					interCount++;
				}else if (curY*nextY < 0){ // if section crosses side
					Line side = new Line(cake.get(i),cake.get(i+1));
					Line section = new Line(angle,curSect);
					Point intPoint = linesInter(side,section);
					intPoint.preVertex = i;
					bPoints[interCount] = intPoint;
					interCount++;
				}
				if (interCount == 2) break; // found all intersections
			}
			
			halfCake = new Polygon();
			halfCake.add(bPoints[0]);
			for (int i=bPoints[0].preVertex; i<=bPoints[1].preVertex; i++){
				halfCake.add(cake.get(i));
			}
			halfCake.add(bPoints[1]);
			sMin = area(halfCake);
			sMax = cakeArea - sMin;
			if (lowNum > bPoints[0].preVertex && lowNum < bPoints[1].preVertex){
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
		halfCake.cutLine = new Line(bPoints[0],bPoints[1]);
		return halfCake;
	}
	
//	private static Point[] linePolInter(Line line,Polygon pol){
//		for (int i=0; i < pol.vertexNumb-1; i++){
//			curY = getYCoord(pol,i,angle) - curSect;
//			nextY = getYCoord(pol,i+1,angle) - curSect;
//			if (curY == 0){ // if section goes through vertex
//				Point intPoint = cake.get(i);
//				intPoint.preVertex = i;
//				bPoints[interCount] = intPoint;
//				interCount++;
//			}else if (curY*nextY < 0){ // if section crosses side
//				Line side = new Line(cake.get(i),cake.get(i+1));
//				Line section = new Line(angle,curSect);
//				Point intPoint = linesInter(side,section);
//				intPoint.preVertex = i;
//				bPoints[interCount] = intPoint;
//				interCount++;
//			}
//			if (interCount == 2) break; // found all intersections
//		}
//	}
	
	private static Point linesInter(Line lOne,Line lTwo){
		double denom = lOne.a*lTwo.b - lTwo.a*lOne.b;
		if (denom == 0){
			System.out.println("-1\nyour cake is not convex!");
			return null;
		}else{
			double x = (lOne.b*lTwo.c - lTwo.b*lOne.c)/denom;
			double y = (lOne.c*lTwo.a - lTwo.c*lOne.a)/denom;
			return new Point(x,y);
		}
	}
	
	private static double getYCoord(Polygon pol,int vert,double angle){
		double sinA = Math.sin(angle);
		double cosA = Math.cos(angle);
		return sinA*pol.get(vert).x + cosA*pol.get(vert).y; 
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
		public Line(Point pOne,Point pTwo){
			this.a = pOne.y - pTwo.y;
			this.b = pTwo.x - pOne.x;
			this.c = pOne.x*pTwo.y - pTwo.x*pOne.y;
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
				this.c = -perpend/Math.cos(angle);
			}
		}
		
		public double perpend(){
			return c/Math.sqrt(a*a+b*b)*Math.signum(-c/b);
		}
	}
	
	private static class Polygon{
		public List<Point> vertxs = new ArrayList<Point>();
		public Line cutLine;
		public int vertexNumb = 0;
		
		public void add(Point p){
			this.vertxs.add(p);
			vertexNumb++;
		}
		
		public Point get(int i){
			return this.vertxs.get(i);
		}
	}
}
