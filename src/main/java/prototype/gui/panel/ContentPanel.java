package prototype.gui.panel;

import prototype.gui.GeneratorModel;

import javax.swing.*;
import java.awt.*;

public class ContentPanel extends JPanel
{
	public ContentPanel()
	{
		super(new BorderLayout());

		GeneratorModel model = new GeneratorModel();

		RenderPanel render = new RenderPanel(model);
		ControlPanel control = new ControlPanel(model);
		ConfigPanel config = new ConfigPanel();

		add(render, BorderLayout.CENTER);
		add(control, BorderLayout.SOUTH);
		add(config, BorderLayout.EAST);

		model.addObserver(render);
	}
}
