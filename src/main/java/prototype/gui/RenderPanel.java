package prototype.gui;

import prototype.graph.Graph;

import javax.swing.*;
import java.util.Observable;
import java.util.Observer;

class RenderPanel extends JPanel implements Observer
{
	private final GeneratorModel model;

	RenderPanel(GeneratorModel model)
	{
		this.model = model;
	}

	@Override
	public void update(Observable o, Object arg)
	{
		Graph graph = model.getGraph();
		// TODO render
	}
}
