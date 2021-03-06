/*******************************************************************************
*   Copyright 2014 Analog Devices, Inc.
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
********************************************************************************/

package com.analog.lyric.dimple.model.values;

import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;
import com.analog.lyric.dimple.model.domains.IntDomain;

/**
 * Holder for {@code int} values.
 */
public class IntValue extends Value
{
	protected int _value;
	
	/*--------------
	 * Construction
	 */
	
	public IntValue()
	{
		this(0);
	}
	
	public IntValue(int value)
	{
		_value = value;
	}
	
	public IntValue(IntValue that)
	{
		this(that._value);
	}
	
	@Override
	public IntValue clone()
	{
		return new IntValue(this);
	}

	/*---------------
	 * Value methods
	 */
	
	@Override
	public void setFrom(Value other)
	{
		_value = other.getInt();
	}
	
	@Override
	public IntDomain getDomain()
	{
		return IntDomain.unbounded();
	}
	
	@Override
	public Object getObject()
	{
		return _value;
	}
	
	@Override
	public void setObject(Object value)
	{
		_value = FactorFunctionUtilities.toInteger(value);
	}
	
	@Override
	public double getDouble()
	{
		return _value;
	}
	
	/**
	 * {@inheritDoc}
	 * <p>
	 * Sets content by rounding {@code value}.
	 */
	@Override
	public void setDouble(double value)
	{
		// TODO: is this the semantics we want here?
		_value = (int)Math.round(value);
	}
	
	@Override
	public int getInt()
	{
		return _value;
	}
	
	@Override
	public void setInt(int value)
	{
		_value = value;
	}
	
	@Override
	public boolean valueEquals(Value other)
	{
		return _value == other.getInt();
	}
}
