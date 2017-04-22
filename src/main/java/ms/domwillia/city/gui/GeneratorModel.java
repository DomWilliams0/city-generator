package ms.domwillia.city.gui;

import ms.domwillia.city.Config;
import ms.domwillia.city.generator.Generator;

import javax.swing.*;
import java.util.Observable;

public class GeneratorModel extends Observable
{
	private Generator generator;

	public GeneratorModel()
	{
	}

	public void generate()
	{
		try
		{
			generator = new Generator(
				Config.getInt(Config.Key.WORLD_WIDTH),
				Config.getInt(Config.Key.WORLD_HEIGHT)
			);
			generator.generate();
		} catch (RuntimeException e)
		{
			e.printStackTrace();
			JOptionPane.showMessageDialog(null,
			e.getMessage() == null ? e.toString() : e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
			return;
		}

		setChanged();
		notifyObservers();
	}

	public Generator getGenerator()
	{
		return generator;
	}
}
