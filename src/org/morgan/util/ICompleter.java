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

/**
  * This is a simple interface for lists of things that need to be completed.
  * For example, when you have a list of things and someone is typing in a
  * search, this interface can be used to complete that list for the search.
  *
  * @author Mark Morgan (mark@mark-morgan.org)
  */
public interface ICompleter<T>
{
	public T complete(String partial);
	public Iterable<T> completeAll(String partial);
	public Iterable<T> retrieveAll();
}
