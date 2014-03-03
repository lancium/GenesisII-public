package edu.virginia.vcgr.genii.gjt.gui.about;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTextArea;

import edu.virginia.vcgr.genii.gjt.gui.GridJobToolFrame;

public class AboutDialog extends JDialog {
	static final long serialVersionUID = 0L;

	static private final Dimension ICON_SIZE = new Dimension(128, 128);
	static private final String VERSION_STRING = "Version 1.0";

	public AboutDialog(JDialog owner) {
		super(owner);
		setTitle("About Grid Job Tool");

		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Container container = getContentPane();

		container.setLayout(new GridBagLayout());

		BufferedImage image = null;
		try {
			image = ImageIO.read(GridJobToolFrame.class
					.getResource("images/GenesisII-Icon.png"));
		} catch (IOException ioe) {
		}

		Image i = image.getScaledInstance(128, -1, 0);
		JLabel icon = new JLabel(new ImageIcon(i));
		icon.setPreferredSize(ICON_SIZE);
		icon.setMinimumSize(ICON_SIZE);
		icon.setMaximumSize(ICON_SIZE);

		JTextArea text = new JTextArea(
				"Grid Job Tool\n\n"
						+ VERSION_STRING
						+ "\n\n"
						+ "This software is licensed under the Apache 2.0 license and\n"
						+ "may be modified and/or distributed freely so long as this\n"
						+ "license agreement and the University of Virginia CS department\n"
						+ "logo and the Genesis II logo remain intact and displayed.\n\n"
						+ "This software product was developed by Mark Morgan.");
		text.setEditable(false);

		container.add(icon, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0,
				GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(
						5, 5, 5, 5), 5, 5));
		container.add(text, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(
						5, 5, 5, 5), 5, 5));
		container.add(new JButton(new OKAction()), new GridBagConstraints(0, 1,
				2, 1, 1.0, 1.0, GridBagConstraints.SOUTHEAST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 5, 5));
	}

	private class OKAction extends AbstractAction {
		static final long serialVersionUID = 0L;

		public OKAction() {
			super("OK");
		}

		@Override
		public void actionPerformed(ActionEvent event) {
			dispose();
		}
	}
}