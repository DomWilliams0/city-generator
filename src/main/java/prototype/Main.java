package prototype;

import prototype.graph.Graph;
import prototype.graph.Vertex;

public class Main
{
	public static void main(String[] args)
	{
		Graph graph = new Graph(1200, 600);

		Vertex a = graph.addVertex(100, 100, RoadType.MAIN);
		Vertex b = graph.addVertex(200, 400, RoadType.MAIN);
		graph.addEdge(a, b);

		graph.export();
	}
}
