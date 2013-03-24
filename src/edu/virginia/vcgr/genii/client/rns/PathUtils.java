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
package edu.virginia.vcgr.genii.client.rns;

import java.util.Stack;

class PathUtils
{
	static public String[] normalizePath(String currentPath, String path)
	{
		String fullPath;

		if (path.startsWith("/"))
			fullPath = path;
		else
			fullPath = currentPath + "/" + path;

		return normalizePath(fullPath);

	}

	static public String[] normalizePath(String path)
	{
		Stack<String> nPath = new Stack<String>();
		String[] ret = path.split("/");
		for (String s : ret) {
			if (s == null || s.length() == 0)
				continue;

			if (s.equals("..")) {
				if (!nPath.empty())
					nPath.pop();
			} else if (!s.equals(".")) {
				nPath.push(s);
			}
		}

		ret = new String[nPath.size()];
		nPath.toArray(ret);
		return ret;
	}

	static public String formPath(String[] pathElements)
	{
		StringBuilder builder = new StringBuilder();
		for (String element : pathElements) {
			builder.append('/');
			builder.append(element);
		}
		if (builder.length() == 0) {
			builder.append('/');
		}
		return builder.toString();
	}

	static private void print(String[] path)
	{
		System.out.print("{");
		for (int lcv = 0; lcv < path.length; lcv++) {
			if (lcv != 0)
				System.out.print(", ");
			System.out.print(path[lcv]);
		}
		System.out.println("}");
	}

	static public void main(String[] args)
	{
		print(normalizePath("/one/two/three", "../../../../../mark"));
		print(normalizePath("/", "home/foobar"));
		print(normalizePath("/one/two/three", "/home/foobar"));
	}
}
