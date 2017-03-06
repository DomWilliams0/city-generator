package prototype;


import prototype.generator.Generator;
import prototype.graph.Graph;

public class Main
{
	public static void main(String[] args)
	{
		Graph graph = new Graph(1200, 600);
		Generator g = new Generator(graph);

		g.generate();

		graph.export();
	}
}
