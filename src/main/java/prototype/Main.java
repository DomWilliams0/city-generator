package prototype;


import prototype.generator.Generator;
import prototype.graph.Graph;

public class Main
{
	public static void main(String[] args)
	{
		// TODO multiple threads
		for (int i = 0; i < 10; i++)
		{
			Graph graph = new Graph(1200, 600);
			Generator g = new Generator(graph);

			g.generate();

			graph.export();
		}
	}
}
