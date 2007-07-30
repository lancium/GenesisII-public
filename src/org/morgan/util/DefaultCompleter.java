/*
 * Copyright 2006 University of Virginia
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package org.morgan.util;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class DefaultCompleter<T> implements ICompleter<T>
{
	static private class TrinaryNode<T>
	{
		public char character;
		public TrinaryNode<T> less;
		public TrinaryNode<T> equal;
		public TrinaryNode<T> greater;
		public T data;
		
		public TrinaryNode(char c)
		{
			character = c;
			less = equal = greater = null;
			data = null;
		}
	}
	
	private TrinaryNode<T> _head = null;
	
	private DefaultCompleter(Iterable<T> primer)
	{
		for (T item : primer)
		{
			char []tmp = item.toString().toCharArray();
			char []chars = new char[tmp.length + 1];
			System.arraycopy(tmp, 0, chars, 0, tmp.length);
			chars[chars.length - 1] = (char)0;
			int length = chars.length;
			TrinaryNode<T> node = _head;
			int cIndex = 0;
			
			while (true)
			{
				if (cIndex >= length)
					break;
				
				char c = chars[cIndex];
				
				if (node == null)
				{
					node = _head = new TrinaryNode<T>(chars[cIndex]);
					continue;
				}
				
				if (c < node.character)
				{
					if (node.less == null)
						node.less = new TrinaryNode<T>(c);
					node = node.less;
				} else if (c > node.character)
				{
					if (node.greater == null)
						node.greater = new TrinaryNode<T>(c);
					node = node.greater;
				} else
				{
					cIndex++;
					if (cIndex >= length)
					{
						node.data = item;
						break;
					}
					
					if (node.equal == null)
						node.equal = new TrinaryNode<T>(chars[cIndex]);
					node = node.equal;
				}
			}
		}
	}
	
	static public <T> ICompleter<T> buildCompleter(Iterable<T> primer)
	{
		return new DefaultCompleter<T>(primer);
	}
	
	static private <T> TrinaryNode<T> findNode(TrinaryNode<T> node, String str)
	{
		if (node == null)
			return null;
		
		if (str.length() == 0)
			return node;
		
		char c = str.charAt(0);
		if (c < node.character)
			return findNode(node.less, str);
		else if (c > node.character)
			return findNode(node.greater, str);
		else
			return findNode(node.equal, str.substring(1));
	}
	
	static private <T> T next(TrinaryNode<T> node)
	{
		if (node == null)
			return null;
		
		if (node.data != null)
			return node.data;
		
		T item;
		item = next(node.less);
		if (item != null)
			return item;
		item = next(node.equal);
		if (item != null)
			return item;
		
		return next(node.greater);
	}
	
	public T complete(String partial)
	{
		return next(findNode(_head, partial));
	}

	public Iterable<T> completeAll(String partial)
	{
		return retrieveAll(findNode(_head, partial), new ArrayList<T>());
	}
	
	private Iterable<T> retrieveAll(TrinaryNode<T> node, ArrayList<T> all)
	{
		if (node == null)
			return all;
		
		if (node.data != null)
			all.add(node.data);
		
		retrieveAll(node.less, all);
		retrieveAll(node.equal, all);
		retrieveAll(node.greater, all);
			
		return all;
	}
	
	public Iterable<T> retrieveAll()
	{
		ArrayList<T> all = new ArrayList<T>();
		return retrieveAll(_head, all);
	}
	
	static public void main(String []args)
	{
		HashSet<String> names = new HashSet<String>();
		names.add("Morgan");
		names.add("Martin");
		names.add("Grimshaw");
		names.add("");
		names.add("Wasson");
		names.add("Karpovich");
		names.add("Appleton");
		
		ICompleter<String> completer = DefaultCompleter.buildCompleter(names);
		for (String str : completer.retrieveAll())
		{
			System.err.println(str);
		}
		
		System.err.println("Matches for \"M\".");
		for (String str : completer.completeAll(""))
		{
			System.err.println("\t" + str);
		}
	}
}
