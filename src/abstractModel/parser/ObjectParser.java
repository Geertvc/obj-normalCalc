package abstractModel.parser;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import abstractModel.TexCoord2f;
import abstractModel.Triangle;
import abstractModel.Vector3f;
import abstractModel.Vertex;

/**
 * ObjectParser is used to parse .obj files.
 * An .obj file will be parsed and a Mesh will be created from it.
 * 
 * @author Geert Van Campenhout
 * @version 2.0
 */
public class ObjectParser {
	

	private static final String SPACE = new String(" ");
	
	List<Vertex> coordinates;
	List<Vector3f> normals;
	List<TexCoord2f> textureCoordinates;
	List<Triangle> triangles;
	String inputFileName;
	
	/**
	 * Creates a new ObjectParser for the file with the given name.
	 * 
	 * @param inputFileName	The name of the file that needs to be parsed.
	 */
	public ObjectParser(String inputFileName){
		this.coordinates = new ArrayList<Vertex>();
		this.normals = new ArrayList<Vector3f>();
		this.textureCoordinates = new ArrayList<TexCoord2f>();
		this.triangles = new ArrayList<Triangle>();
		this.inputFileName = inputFileName;
	}
	
	/**
	 * Parses the ObjectParser file to a Mesh with the given name.
	 * 
	 * @param outputFileName	The name to give to the parsed file.
	 * @return	Mesh
	 * 		The parsed file.
	 * @throws IOException 
	 */
	public void parse(String outputFileName) throws IOException{
		File outputFile = new File(outputFileName);
		//Create the file and see if it did not yet exist
		if(!outputFile.createNewFile()) {
			throw new IllegalArgumentException("The given output file [" + outputFileName + "] already exists.");
		}
		
		long calcStartTime = System.currentTimeMillis();
		System.out.println("Start parsing file: " + inputFileName);
		File file = new File(this.inputFileName);
	    FileInputStream fileInputStream = new FileInputStream(file);
	    DataInputStream in = new DataInputStream(fileInputStream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;
		//Read File Line By Line
		try {
			while ((strLine = br.readLine()) != null)   {
				parseLine(strLine);
			}
			//Close the input stream
			in.close();
		} catch (IOException e) {
			System.err.println("IOxception in ObjectParser.parse");
			e.printStackTrace();
		}
		
		/*We have here.
		 * coordinates = a list of the vertices coordinates.
		 * normals = empty in our case but should become a list of the normals coordinates.
		 * textureCoordinates = a list of the texture coordinates.
		 * 
		 * coordinatesIndices = a list of 
		 */
		
		long calcStopTime = System.currentTimeMillis();
		long duration = calcStopTime - calcStartTime;
		System.out.println("Parsing of " + inputFileName + " finished in " + duration + " msec.");
	    
		calcTriangleNormals();
	    normalizeVertexNormals();
	    
	    writeFile(outputFile);
	}
	
	private void writeFile(File outputFile) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(outputFile));
		writeVertexCoordinates(pw);
		writeTextureCoordinates(pw);
		writeVertexNormalsCoordinates(pw);
		writeTriangles(pw);
	 
		pw.close();
	}

	private void writeVertexCoordinates(PrintWriter pw) {
		String V = new String("v ");
		StringBuilder sb;
		pw.println("# Vertices");
		for (Vertex vertex : coordinates) {
			sb = new StringBuilder();
			sb.append(V);
			sb.append(vertex.coord.x);
			sb.append(SPACE);
			sb.append(vertex.coord.y);
			sb.append(SPACE);
			sb.append(vertex.coord.z);
			pw.println(sb.toString());
		}
	}
	
	private void writeTextureCoordinates(PrintWriter pw) {
		String VT = new String("vt ");
		StringBuilder sb;
		pw.println("# Texture coordinates");
		for (TexCoord2f texCoord : textureCoordinates) {
			sb = new StringBuilder();
			sb.append(VT);
			sb.append(texCoord.x);
			sb.append(SPACE);
			sb.append(texCoord.y);
			pw.println(sb.toString());
		}
	}
	
	private void writeVertexNormalsCoordinates(PrintWriter pw) {
		String VN = new String("vn ");
		StringBuilder sb;
		pw.println("# Vertex normals");
		for (Vertex vertex : coordinates) {
			sb = new StringBuilder();
			sb.append(VN);
			sb.append(vertex.normal.x);
			sb.append(SPACE);
			sb.append(vertex.normal.y);
			sb.append(SPACE);
			sb.append(vertex.normal.z);
			pw.println(sb.toString());
		}
	}
	
	private void writeTriangles(PrintWriter pw) {
		String F = new String("f ");
		String SLASH = new String("/");
		StringBuilder sb;
		pw.println("# Faces");
		for (Triangle triangle: triangles) {
			if(Float.isNaN(triangle.getNormal().x) || Float.isNaN(triangle.getNormal().y) || Float.isNaN(triangle.getNormal().z)) {
				System.out.println("INFO: Leaving out Triangle with NaN normal.");
			} else {
			
			
			
			sb = new StringBuilder();
			sb.append(F);
			int vertex1 = triangle.vertex[0] + 1;
			sb.append(vertex1);
			sb.append(SLASH);
			sb.append(triangle.textVertex[0] + 1);
			sb.append(SLASH);
			sb.append(vertex1);
			
			sb.append(SPACE);
			
			int vertex2 = triangle.vertex[1] + 1;
			sb.append(vertex2);
			sb.append(SLASH);
			sb.append(triangle.textVertex[1] + 1);
			sb.append(SLASH);
			sb.append(vertex2);
			
			sb.append(SPACE);
			
			int vertex3 = triangle.vertex[2] + 1;
			sb.append(vertex3);
			sb.append(SLASH);
			sb.append(triangle.textVertex[2] + 1);
			sb.append(SLASH);
			sb.append(vertex3);
			pw.println(sb.toString());
			}
		}
	}

	private void normalizeVertexNormals() {
		for (Vertex vertex : coordinates) {
			vertex.normalizeNormal();
		}
	}

	private void calcTriangleNormals() {
		/*Ignoring Smoothing Groups
	    In the case where smoothing groups are not checked there will be a single vertex normal for each vertex of the mesh. 
	    The vertex normal is the average of the face normals of each of the faces that share that vertex. 
	    The algorithm to compute such vertex normals is as follows:

	    First, allocate an array of normals, one for each vertex in the mesh, and initialize them to zero (Point3( 0,0,0)). 
	    Then for each face, compute its face normal, and add it into each of the three vertex normals that the face contributes to. 
	    For example, if you have a vertex normal shared by five faces, each face will add in its normal to that vertex, and thus the result will be average normal of those five faces. 
	    When all the faces in the mesh have been processed, the average normal vector has been computed. 
	    As a last step, all the normals in the array can be normalized.
	    */
		
		for (Triangle triangle : this.triangles) {
			Vector3f normal = triangle.getNormal();
			Vertex a = this.coordinates.get(triangle.vertex[0]);
			Vertex b = this.coordinates.get(triangle.vertex[1]);
			Vertex c = this.coordinates.get(triangle.vertex[2]);
			if(normal == null) {
				normal = triangle.calcNormalizedNormal(a, b, c);
				triangle.setNormal(normal);
			}
			if(Float.isNaN(normal.x) || Float.isNaN(normal.y) || Float.isNaN(normal.z)) {
				System.out.println("WARN: Triangle with NaN normal found, it will be left out.");
			} else {
				a.addToNormal(normal);
				b.addToNormal(normal);
				c.addToNormal(normal);
				if(Float.isNaN(a.normal.x) || Float.isNaN(a.normal.y) || Float.isNaN(a.normal.z) ||
						Float.isNaN(b.normal.x) || Float.isNaN(b.normal.y) || Float.isNaN(b.normal.z) ||
						Float.isNaN(c.normal.x) || Float.isNaN(c.normal.y) || Float.isNaN(c.normal.z)) {
					System.out.println("Vertex with NaN normal");
				}
			}
		}
	}

	/**
	 * Parses the given line.
	 * 
	 * @param strLine	The line to parse.
	 */
	public void parseLine(String strLine){
		String[] splittedString = strLine.split(" ");
		String cmd = splittedString[0];
		for (int i = 1; i < splittedString.length; i++) {
			if(splittedString[i].equals("")){
				for (int j = i; j < splittedString.length-1; j++) {
					splittedString[j] = splittedString[j+1];
				}
			}
		}
		if(cmd.equals("v")){
			//Vertex
			this.coordinates.add(new Vertex(
					Float.parseFloat(splittedString[1]),
					Float.parseFloat(splittedString[2]),
					Float.parseFloat(splittedString[3])));
		} else if(cmd.equals("vt")){
			//Textuurcoordinaat
			this.textureCoordinates.add(new TexCoord2f(
					Float.parseFloat(splittedString[1]), 
					Float.parseFloat(splittedString[2])));
		} else if(cmd.equals("vn")){
			//normaal
			this.normals.add(new Vector3f(
					Float.parseFloat(splittedString[1]),
					Float.parseFloat(splittedString[2]),
					Float.parseFloat(splittedString[3])));
		} else if(cmd.equals("f")){
			//Vlak
			if(splittedString.length-1 == 3){
				nbFaces++;
				this.triangles.add(parseTriangle(splittedString));
				
				
			} else if(splittedString.length-1 == 4){
				String[] triangle1 = new String[] {splittedString[0], splittedString[1], splittedString[2], splittedString[3]};
				String[] triangle2 = new String[] {splittedString[0], splittedString[1], splittedString[3], splittedString[4]};
				nbFaces++;
				this.triangles.add(parseTriangle(triangle1));
				nbFaces++;
				this.triangles.add(parseTriangle(triangle2));
			} else{
				throw new UnsupportedOperationException("Don't support faces with more than 4 vertices.");
			}
			
		} else if(cmd.equals("#")){
			//ignore Comment lines
		} else if(cmd.equals("g")){
			//Group start
			System.err.println("Implement groups");
		} else if(cmd.equals("mtllib")){
			//Mtllib start
			System.err.println("Implement mtllib");
		} else if(cmd.equals("o")){
			//objectname start
			System.err.println("Implement o");
		} else if(cmd.equals("usemtl")){
			//usemtl name material start
			System.err.println("Implement usemtl");
		} else if(cmd.equals("s")){
			//smoothgroup start
			System.err.println("Implement s");
		} else if(cmd.equals("")){
			//empty line
		} else{
			throw new UnsupportedOperationException("Lines starting with \"" + cmd + "\" not supported yet.");
		}
	}
	
	/**
	 * Parses a subString that came from a plane line of the .obj file.
	 * 
	 * @param subString	The subString to parse.
	 */
	public Triangle parseTriangle(String[] splittedString){
		int[] coords = new int[3];
		int[] textCoords = new int[3];
		for (int i = 1; i < splittedString.length; i++) {
			String[] splitSubString;
			splitSubString = splittedString[i].split("/");
			
			int arrayIndex = i-1;
			
			coords[arrayIndex] = Integer.parseInt(splitSubString[0])-1;
			if(splitSubString.length >1){
				if(!splitSubString[1].equals(""))
					textCoords[arrayIndex] = Integer.parseInt(splitSubString[1])-1;
			}
		}
		return new Triangle(coords, textCoords);
	}
	
	public int nbFaces = 0;
	
	/**
	 * Main method to test the ObjectParser.
	 * 
	 * @param args	The args to give to the main method.
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		ObjectParser objparser = new ObjectParser("OBJ/Assassin.obj");
		objparser.parse("OBJ/testOutput.obj");
		System.out.println("done parsing");
		System.out.println("Number of coordinates: " + objparser.coordinates.size());
		if(objparser.normals.size() > 0) {
			System.out.println("This OBJ file already contained normals (vn). New ones are calculated.");
		}
		System.out.println("Number of normals: " + objparser.normals.size());
		System.out.println("Number of texture coordinates: " + objparser.textureCoordinates.size());
		System.out.println("Number of faces: " + objparser.nbFaces);
		System.out.println("Number of triangles: " + objparser.triangles.size());
		
//		printVertices(objparser);
//		printTriangleNormals(objparser);
		
		calculateBoundaries(objparser);
	}

	private static void calculateBoundaries(ObjectParser objparser) {
		Vector3f max = new Vector3f(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
		Vector3f min = new Vector3f(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);
		for (Vertex vertex : objparser.coordinates) {
			if(vertex.coord.x > max.x) {
				max.x = vertex.coord.x;
			}
			if(vertex.coord.x < min.x) {
				min.x = vertex.coord.x;
			}
			
			if(vertex.coord.y > max.y) {
				max.y = vertex.coord.y;
			}
			if(vertex.coord.y < min.y) {
				min.y = vertex.coord.y;
			}
			
			if(vertex.coord.z > max.z) {
				max.z = vertex.coord.z;
			}
			if(vertex.coord.z < min.z) {
				min.z = vertex.coord.z;
			}
		}
		System.out.println("Boundary box = min (" + min.x + ", " + min.y + ", " + min.z + ") - max (" + max.x + ", " + max.y + ", " + max.z + ")");
	}
	

//	private static void printVertices(ObjectParser objparser) {
//		for (int i = 0; i < objparser.coordinates.size(); i++) {
//			Vertex vertex = objparser.coordinates.get(i);
//			System.out.println("Vertex " + i + ": " + vertex.coord + " - normal = " + vertex.normal);
//		}
//	}
//
//	private static void printTriangleNormals(ObjectParser objparser) {
//		for (Triangle triangle : objparser.triangles) {
//			System.out.println("Triangle (" + triangle.vertex[0] + "," + triangle.vertex[1] + "," + triangle.vertex[2] + ") = " + triangle.getNormal());
//		}
//	}
}
