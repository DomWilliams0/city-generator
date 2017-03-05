package prototype;

import prototype.graph.Vertex;

public class Main
{
	public static void main(String[] args)
	{
		World w = new World(1200, 600);

		Vertex a = w.addVertex(100, 100);
		Vertex b = w.addVertex(200, 400);
		w.addEdge(a, b);

		w.export();
	}
}
