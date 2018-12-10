package abstractModel;

import abstractModel.Vector3f;

/**
 * Class representing a triangle in the Triangle Mesh.
 * 
 * @author Geert Van Campenhout
 * @version 1.0
 */
public class Triangle {
	//Array with indices of vertices belonging to the triangle. always length 3
	public int[] vertex;
	//Array with indices of texture vertices belonging to the triangle. always length 3
	public int[] textVertex;
	
	private Vector3f normal;
	
	/** Initializes a triangle with his edges and vertices and normal.
	 * Vertices and triangleNBr both need to be of length 3.
	 * 
	 * @param vertices	Array with indices of his vertices.
	 */
	public Triangle(int[] vertex, int[] textVertex){
		this.vertex = vertex;
		this.textVertex = textVertex;
	}
	
	public Vector3f getNormal() {
		return this.normal;
	}
	
	public void setNormal(Vector3f normal) {
		this.normal = (Vector3f) normal.clone();
	}
	
	/**
	 * Calculates and returns the normalized normal for the given vertices of a triangle.
	 * Note: the vertices must be given in counter-clockwise order so the normal will point outwards the model.
	 *  
	 * @param a	The first vertex of the triangle.
	 * @param b The second vertex of the triangle.
	 * @param c The third vertex of the triangle.
	 * @return	Vector3f
	 * 		The normalized normal of the triangle.
	 */
	public Vector3f calcNormalizedNormal(Vertex a, Vertex b, Vertex c){
		Vector3f bMinA = new Vector3f(b.coord.x-a.coord.x,b.coord.y-a.coord.y,b.coord.z-a.coord.z);
		Vector3f cMinA = new Vector3f(c.coord.x-a.coord.x,c.coord.y-a.coord.y,c.coord.z-a.coord.z);
		Vector3f normal = new Vector3f();
		normal.cross(bMinA, cMinA);
		normal.scale(1/normal.length());
		return normal;
	}
	
}
