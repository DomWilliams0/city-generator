package prototype.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

class ControlPanel extends JPanel
{
	ControlPanel(GeneratorModel model)
	{
		super(new BorderLayout());

		JButton goButton = new JButton("Generate");
		goButton.addActionListener(new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.generate();
			}
		});
		add(goButton, BorderLayout.CENTER);
	}



}
