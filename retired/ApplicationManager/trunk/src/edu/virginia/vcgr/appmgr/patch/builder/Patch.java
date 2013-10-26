package edu.virginia.vcgr.appmgr.patch.builder;

import java.awt.Dialog.ModalityType;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.virginia.vcgr.appmgr.patch.PatchOperationType;
import edu.virginia.vcgr.appmgr.patch.builder.planning.MakeDirectoryPlannedAction;
import edu.virginia.vcgr.appmgr.patch.builder.planning.PatchPlan;
import edu.virginia.vcgr.appmgr.patch.builder.planning.PatchPlanContext;
import edu.virginia.vcgr.appmgr.patch.builder.planning.WriteFilePlannedAction;
import edu.virginia.vcgr.appmgr.patch.builder.planning.WritePatchDescriptionPlannedAction;
import edu.virginia.vcgr.appmgr.patch.builder.tree.AtomBundle;
import edu.virginia.vcgr.appmgr.patch.builder.tree.PatchTree;
import edu.virginia.vcgr.appmgr.util.GUIUtilities;
import edu.virginia.vcgr.appmgr.util.plan.PlannedProgressBarDialog;

public class Patch
{
	private PatchDescription _description;
	private Collection<AtomBundle> _atomBundles;

	private void planDescription(PatchPlan plan) throws IOException
	{
		planMakeDirectory(plan, new File("META-INF/patch"));
		plan.addAction(new WritePatchDescriptionPlannedAction(_description));
	}

	private void planMakeDirectory(PatchPlan plan, File dir)
	{
		File pFile = dir.getParentFile();
		if (pFile != null)
			planMakeDirectory(plan, pFile);

		String name = String.format("%s/", dir.getPath());
		if (!plan.haveMadeDirectory(name)) {
			plan.addAction(new MakeDirectoryPlannedAction(name));
			plan.markDirectoryMade(name);
		}
	}

	private void planCommonFile(PatchPlan plan, String entry)
	{
		File targetFile = new File("common", entry);

		planMakeDirectory(plan, targetFile.getParentFile());

		plan.addAction(new WriteFilePlannedAction(entry));
	}

	private void planCommonFiles(PatchPlan plan)
	{
		for (AtomBundle bundle : _atomBundles) {
			if (bundle.getAtom().getOperationType() == PatchOperationType.WRITE)
				planCommonFile(plan, bundle.toString());
		}
	}

	public Patch(PatchTree tree)
	{
		_description = new PatchDescription(tree);
		_atomBundles = new Vector<AtomBundle>();

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) model.getRoot();

		Enumeration<?> patches = root.children();
		while (patches.hasMoreElements()) {
			DefaultMutableTreeNode patch = (DefaultMutableTreeNode) patches.nextElement();
			Enumeration<?> operations = patch.children();
			while (operations.hasMoreElements()) {
				DefaultMutableTreeNode operation = (DefaultMutableTreeNode) operations.nextElement();
				AtomBundle bundle = (AtomBundle) operation.getUserObject();
				_atomBundles.add(bundle);
			}
		}
	}

	public void generate(JFrame owner, File sourceDirectory, File targetJarFile) throws IOException
	{
		PatchPlanContext context = null;
		PatchPlan plan = new PatchPlan();

		planDescription(plan);
		planCommonFiles(plan);

		context = new PatchPlanContext(sourceDirectory, targetJarFile);
		PlannedProgressBarDialog<PatchPlanContext> dialog =
			new PlannedProgressBarDialog<PatchPlanContext>(owner, "Creating Patch", plan.getPlan());
		dialog.setModalityType(ModalityType.DOCUMENT_MODAL);
		dialog.pack();
		GUIUtilities.centerWindow(dialog);
		dialog.start(context);
	}
}