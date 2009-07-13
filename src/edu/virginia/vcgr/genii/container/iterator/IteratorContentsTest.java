package edu.virginia.vcgr.genii.container.iterator;

import java.io.File;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

public class IteratorContentsTest
{
	static private void write(OutputStream out, String name) throws Throwable
	{
		ObjectOutputStream oos = new ObjectOutputStream(out);
		oos.writeObject(name);
		oos.close();
		out.close();
	}
	
	static private void read(InputStream in) throws Throwable
	{
		ObjectInputStream ois = new ObjectInputStream(in);
		System.out.println(ois.readObject());
		ois.close();
		in.close();
	}
	
	static public void main(String []args) throws Throwable
	{
		File file = new File("/Users/morgan/test.dat");
		
		IteratorContentsOutputFile oFile = new IteratorContentsOutputFile(file);
		
		write(oFile.addEntry(), "One");
		write(oFile.addEntry(), "Two");
		write(oFile.addEntry(), "Three");
		write(oFile.addEntry(), "Four");
		write(oFile.addEntry(), "Five");
		write(oFile.addEntry(), "Six");
		oFile.close();
		
		IteratorContentsInputFile iFile = new IteratorContentsInputFile(file);
		read(iFile.openEntry(3));
		read(iFile.openEntry(5));
		read(iFile.openEntry(0));
		read(iFile.openEntry(2));
		read(iFile.openEntry(1));
		read(iFile.openEntry(4));
		read(iFile.openEntry(3));
		read(iFile.openEntry(5));
		read(iFile.openEntry(0));
		read(iFile.openEntry(2));
		read(iFile.openEntry(1));
		read(iFile.openEntry(4));
		iFile.close();
	}
}