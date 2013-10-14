package edu.virginia.vcgr.appmgr.patch.builder;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import edu.virginia.vcgr.appmgr.util.GUIUtilities;

public class PatchBuilder
{
	static private void usage()
	{
		System.err.println("PatchBuilder [--patchrc=<patchrc-file>] <old-version> <new-version> <patch-file>");
		System.exit(1);
	}

	static private void categorize(PatchRC rc, Map<String, PatchAtom> atoms, Map<String, PatchAtom> ignores)
	{
		for (String path : rc.ignores()) {
			PatchAtom atom = atoms.remove(path);
			if (atom != null)
				ignores.put(path, atom);
		}
	}

	static public void addSplashScreenText(String text)
	{
		SplashScreen splash = SplashScreen.getSplashScreen();
		if (splash != null) {
			Graphics2D graphics = null;

			try {
				graphics = splash.createGraphics();
				Rectangle bounds = splash.getBounds();
				bounds.x = 0;
				bounds.y = 0;
				bounds.x = bounds.x + 50;
				bounds.y = bounds.y + bounds.height - 50 - 30;
				bounds.width = bounds.width - 100;
				bounds.height = 30;
				graphics.setClip(bounds.x, bounds.y, bounds.width, bounds.height);
				graphics.setComposite(AlphaComposite.Clear);
				graphics.setFont(graphics.getFont().deriveFont(18.0f));
				graphics.setBackground(Color.LIGHT_GRAY);
				graphics.setColor(Color.LIGHT_GRAY);
				graphics.setPaintMode();
				graphics.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
				graphics.setColor(Color.BLACK);
				graphics.drawString(text, bounds.x + 5, bounds.y + bounds.height - 5);
				splash.update();
			} finally {
				if (graphics != null)
					graphics.dispose();
			}
		}
	}

	static public void main(String[] args) throws Throwable
	{
		PatchRC rc;
		String patchrcOverride = null;
		File oldVersion = null;
		File newVersion = null;
		File patchFile = null;

		for (String arg : args) {
			if (arg.startsWith("--patchrc="))
				patchrcOverride = arg.substring(10);
			else if (oldVersion == null)
				oldVersion = new File(arg);
			else if (newVersion == null)
				newVersion = new File(arg);
			else if (patchFile == null)
				patchFile = new File(arg);
			else
				usage();
		}

		if (patchFile == null)
			usage();

		addSplashScreenText(String.format("Analyzing versions.", newVersion, oldVersion));

		if (patchrcOverride != null)
			rc = new PatchRC(new File(patchrcOverride));
		else
			rc = new PatchRC();

		Map<String, PatchAtom> atoms = DiffBuilder.buildDiffs(rc, oldVersion, newVersion);

		Map<String, PatchAtom> ignores = new HashMap<String, PatchAtom>();
		categorize(rc, atoms, ignores);

		PatchDialog dialog = new PatchDialog(rc, newVersion, patchFile, atoms);
		dialog.pack();
		GUIUtilities.centerWindow(dialog);
		dialog.setVisible(true);
	}
}