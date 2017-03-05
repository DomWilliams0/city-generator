package prototype.graph;

import prototype.RoadType;

import java.util.*;

public class Graph
{
	private List<Vertex> vertices;
	private Map<Vertex, Set<Vertex>> edges;

	public Graph()
	{
		vertices = new ArrayList<>();
		edges = new HashMap<>();
	}

	public Vertex addVertex(double x, double y, RoadType type)
	{
		// TODO return existing vertex ?

		Vertex v = new Vertex(x, y, type);
		vertices.add(v);
		return v;
	}

	public List<Vertex> getVertices()
	{
		return vertices;
	}

	public Map<Vertex, Set<Vertex>> getEdges()
	{
		return edges;
	}

	public void addEdge(Vertex a, Vertex b)
	{
		Set<Vertex> vertices = edges.computeIfAbsent(a, k -> new HashSet<>());
		vertices.add(b);
	}

	public void removeEdge(Vertex a, Vertex b)
	{
		Set<Vertex> vertices = edges.get(a);
		if (vertices != null)
			vertices.remove(b);

	}
}
