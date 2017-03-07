package prototype.gui;

import prototype.Config;
import prototype.generator.Generator;
import prototype.graph.Graph;

import java.util.Observable;

public class GeneratorModel extends Observable
{
	private Generator generator;

	public GeneratorModel()
	{
	}

	public void generate()
	{
		generator = new Generator(new Graph(
			Config.getInt(Config.Key.WORLD_WIDTH),
			Config.getInt(Config.Key.WORLD_WIDTH))
		);
		generator.generate();

		setChanged();
		notifyObservers();
	}

	public Graph getGraph()
	{
		return generator.getGraph();
	}
}
