package abstractModel;

import abstractModel.Point3f;
import abstractModel.TexCoord2f;
import abstractModel.Vector3f;

/**
 * A Vertex represents a node in the Triangle Mesh.
 * The Vertex contains its coordinates, his normal, a texture coordinate.
 * 
 * @author Geert Van Campenhout
 */
public class Vertex {
	
	public Point3f coord;
	
	public Vector3f normal;
	
	/** Initializes a Vertex with the given index of any edge leaving the vertex.
	 * 
	 * @param float x-coordinate of the vertex
	 * @param float y-coordinate of the vertex
	 * @param float z-coordinate of the vertex
	 */
	public Vertex(float x, float y, float z){
		this.coord = new Point3f(x,y,z);
		this.normal = new Vector3f();
	}
	
	/** Initializes a Vertex with the given index of any edge leaving the vertex.
	 * 
	 * @param Vector3f The coordinates of the vertex in Vector3f format.
	 */
	public Vertex(Point3f coord){
		this.coord = coord;
		this.normal = new Vector3f();
	}
	
	/** Initializes a Vertex with the given index of any edge leaving the vertex.
	 * 
	 * @param Vector3f The coordinates of the vertex in Vector3f format.
	 */
	public Vertex(float coord[]){
		this.coord = new Point3f(coord);
		this.normal = new Vector3f();
	}
	
	/**
	 * Initializes a Vertex with the given index of any edge leaving the vertex,
	 * his coordinates, his normal vector and a texture coordinate.
	 * 
	 * @param coord	The coordinate of the vertex.
	 * @param normal	The vertex normal.
	 * @param texCoord	The texture coordinate of the vertex.
	 */
	public Vertex(Point3f coord, Vector3f normal, TexCoord2f texCoord){
		this.coord = coord;
		if(normal != null) {
			normal.scale(1/normal.length());
			this.normal = normal;
		} else {
			this.normal = new Vector3f();
		}
	}
	
	public void addToNormal(Vector3f vectorToAdd) {
		this.normal.add(vectorToAdd);
	}
	
	public void normalizeNormal() {
		float length = this.normal.length();
		if(length!= 0.0f) {
			this.normal.scale(1/length);
		}
	}
}