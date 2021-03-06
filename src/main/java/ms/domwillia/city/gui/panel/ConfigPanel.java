package ms.domwillia.city.gui.panel;

import ms.domwillia.city.Config;
import ms.domwillia.city.gui.GeneratorModel;

import javax.swing.*;
import javax.swing.text.NumberFormatter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.Arrays;

class ConfigPanel extends JPanel
{
	private final GeneratorModel model;

	ConfigPanel(GeneratorModel model)
	{
		this.model = model;
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.anchor = GridBagConstraints.PAGE_START;

		Config.Section[] values = Config.Section.values();
		for (Config.Section section : values)
		{
			if (++c.gridx >= 2)
			{
				c.gridx = 0;
				c.anchor = GridBagConstraints.PAGE_END;
			}

			add(new ConfigSection(section), c);
		}
	}

	class ConfigSection extends JPanel
	{
		ConfigSection(Config.Section section)
		{
			setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

			setBorder(BorderFactory.createTitledBorder(section.getTitle()));
			Arrays.stream(section.getKeys())
				.filter(k -> k.getType() != Config.KeyType.COLOUR)
				.forEach(k -> add(new ConfigComponent(k)));
		}
	}

	class ConfigComponent extends JPanel
	{

		ConfigComponent(Config.Key key)
		{
			JLabel label = new JLabel(key.toString());
			JComponent input;

			NumberFormat format = NumberFormat.getInstance();
			NumberFormatter formatter = new NumberFormatter(format);
			formatter.setAllowsInvalid(false);
			formatter.setCommitsOnValidEdit(true);

			switch (key.getType())
			{
				case INTEGER:
					input = createNumberComponent(Integer.class, key);
					((JFormattedTextField) input).setValue(Config.getInt(key));
					break;
				case DOUBLE:
					input = createNumberComponent(Double.class, key);
					((JFormattedTextField) input).setValue(Config.getDouble(key));
					break;
				case BOOLEAN:
					input = new JCheckBox("", Config.getBoolean(key));
					JComponent finalInput = input;
					((JCheckBox) input).addActionListener(
						actionEvent -> Config.set(key, ((JCheckBox) finalInput).isSelected()));
					break;
				default:
					throw new IllegalArgumentException("Bad key type");
			}
			JPanel cont = new JPanel(new GridLayout(2, 1));
			cont.add(label);
			cont.add(input);

			setLayout(new BorderLayout());
			add(cont, BorderLayout.CENTER);
		}

		JComponent createNumberComponent(Class type, Config.Key key)
		{
			NumberFormat format = NumberFormat.getInstance();
			NumberFormatter formatter = new NumberFormatter(format);
			formatter.setAllowsInvalid(true);
			formatter.setCommitsOnValidEdit(true);
			formatter.setValueClass(type);

			JFormattedTextField input = new JFormattedTextField(formatter);
			input.addPropertyChangeListener("value", e -> Config.set(key, e.getNewValue()));

			input.addKeyListener(new KeyAdapter()
			{
				@Override
				public void keyPressed(KeyEvent keyEvent)
				{
					if (keyEvent.getKeyCode() == KeyEvent.VK_ENTER)
					{
						model.generate();
					}
				}
			});

			return input;
		}
	}

}
