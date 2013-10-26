package edu.virginia.vcgr.appmgr.patch.builder;

import java.io.PrintStream;
import java.util.Enumeration;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import edu.virginia.vcgr.appmgr.os.OperatingSystemType;
import edu.virginia.vcgr.appmgr.os.ProcessorArchitecture;
import edu.virginia.vcgr.appmgr.patch.HostRestriction;
import edu.virginia.vcgr.appmgr.patch.PatchRestrictions;
import edu.virginia.vcgr.appmgr.patch.builder.tree.AtomBundle;
import edu.virginia.vcgr.appmgr.patch.builder.tree.PatchBundle;

public class PatchDescription
{
	private DefaultMutableTreeNode _root;

	static private void emit(PrintStream out, PatchRestrictions restrictions)
	{
		OperatingSystemType osType = restrictions.getOSType();
		String osVersion = restrictions.getOSVersion();
		ProcessorArchitecture archType = restrictions.getProcessorArchitecture();
		HostRestriction host = restrictions.getHostRestriction();

		if ((osType != null) || (archType != null) || (host != null) || ((osVersion != null) && (osVersion.length() > 0))) {
			out.println("\t\t<restriction>");

			if ((osType != null) || ((osVersion != null) && (osVersion.length() > 0))) {
				out.println("\t\t\t<jsdl:OperatingSystem>");

				if (osType != null) {
					out.println("\t\t\t\t<jsdl:OperatingSystemType>");
					out.format("\t\t\t\t\t<jsdl:OperatingSystemName>%s</jsdl:OperatingSystemName>\n", osType);
					out.println("\t\t\t\t</jsdl:OperatingSystemType>");
				}

				if ((osVersion != null) && (osVersion.length() > 0))
					out.format("\t\t\t\t<jsdl:OperatingSystemVersion>%s</jsdl:OperatingSystemVersion>", osVersion);

				out.println("\t\t\t</jsdl:OperatingSystem>");
			}

			if (archType != null) {
				out.println("\t\t\t<jsdl:CPUArchitecture>");
				out.format("\t\t\t\t<jsdl:CPUArchitectureName>%s</jsdl:CPUArchitectureName>\n", archType);
				out.println("\t\t\t</jsdl:CPUArchitecture>");
			}

			if (host != null)
				host.emit(out);
			out.println("\t\t</restriction>");
		}
	}

	static private void emit(PrintStream out, DefaultMutableTreeNode patch)
	{
		out.println("\t<patch>");

		PatchBundle bundle = (PatchBundle) patch.getUserObject();
		emit(out, bundle.getRestrictions());
		Enumeration<?> children = patch.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
			AtomBundle aBundle = (AtomBundle) child.getUserObject();
			aBundle.getAtom().emit(out);
		}

		out.println("\t</patch>");
	}

	public PatchDescription(JTree tree)
	{
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		_root = (DefaultMutableTreeNode) model.getRoot();
	}

	public void emit(PrintStream out)
	{
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		out.println("<patches\n");
		out.println("\txmlns=\"http://edu.virginia.vcgr/patch\"");
		out.println("\txmlns:patch=\"http://edu.virginia.vcgr/patch\"");
		out.println("\txmlns:jsdl=\"http://schemas.ggf.org/jsdl/2005/11/jsdl\">");

		Enumeration<?> children = _root.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
			emit(out, child);
		}

		out.println("</patches>");
	}
}