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
package org.morgan.util.test;

import org.morgan.util.math.RationalNumber;

import junit.framework.TestCase;

/**
 * @author Mark Morgan (mark@mark-morgan.org)
 */
public class RationalNumberTest extends TestCase
{
	public void testRationalNumber() throws Exception
	{
		RationalNumber alpha = new RationalNumber(5);
		RationalNumber negAlpha = new RationalNumber(-5);
		RationalNumber beta = new RationalNumber(1, 2);
		RationalNumber negBeta = new RationalNumber(-1, 2);
		RationalNumber gamma = new RationalNumber(5, 1, 2);
		RationalNumber negGamma = new RationalNumber(-5, 1, 2);
		
		TestCase.assertEquals(alpha, RationalNumber.fromString("5"));
		TestCase.assertEquals(negAlpha, RationalNumber.fromString("-5"));
		TestCase.assertEquals(beta, RationalNumber.fromString("1/2"));
		TestCase.assertEquals(negBeta, RationalNumber.fromString("-1/2"));
		TestCase.assertEquals(gamma, RationalNumber.fromString("5 1/2"));
		TestCase.assertEquals(negGamma, RationalNumber.fromString("-5 1/2"));
	}
}
