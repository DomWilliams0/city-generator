package prototype.gui;

import javax.swing.*;

public class GUI
{
	private void start()
	{
		JFrame frame = new JFrame("Generator");
		frame.setSize(600, 600);
		frame.setContentPane(new ContentPanel());
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args)
	{
		GUI gui = new GUI();
		gui.start();
	}
}
