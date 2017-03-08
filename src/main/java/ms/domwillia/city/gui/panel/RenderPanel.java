package ms.domwillia.city.gui.panel;

import ms.domwillia.city.Config;
import ms.domwillia.city.graph.Graph;
import ms.domwillia.city.gui.GeneratorModel;

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
		image.setMinimumSize(getBounds().getSize());

		resetImage();

		JScrollPane scroll = new JScrollPane(image,
			ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
			ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		add(scroll, BorderLayout.CENTER);
	}

	private void resetImage()
	{
		Graph placeholder = new Graph(Config.getInt(Config.Key.WORLD_WIDTH), Config.getInt(Config.Key.WORLD_HEIGHT));
		image.setIcon(new ImageIcon(placeholder.render()));
		image.setText("Nothing to see here");
		image.setHorizontalTextPosition(SwingConstants.CENTER);
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
