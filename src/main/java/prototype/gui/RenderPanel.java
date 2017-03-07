package prototype.gui;

import prototype.graph.Graph;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

class RenderPanel extends JPanel implements Observer
{
	private final GeneratorModel model;
	private final JLabel image;

	RenderPanel(GeneratorModel model)
	{
		super(new BorderLayout());

		this.model = model;
		this.image = new JLabel();
		image.setBounds(getBounds());

		resetImage();

		add(image, BorderLayout.CENTER);
	}

	private void resetImage()
	{
		image.setIcon(null);
		image.setText("Nothing to see here");
	}

	@Override
	public void update(Observable o, Object arg)
	{
		Graph graph = model.getGraph();
		BufferedImage render = graph.render();

		image.setText(null);
		image.setIcon(new ImageIcon(render));
	}
}
