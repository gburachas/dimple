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

package com.analog.lyric.dimple.solvers.gibbs;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import net.jcip.annotations.NotThreadSafe;

import com.analog.lyric.collect.IKeyed;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;

/**
 * @since 0.05
 */
@NotThreadSafe
public final class SFactorUpdate implements IKeyed<ISolverFactorGibbs>
{
	/*-------
	 * State
	 */
	
	private final ISolverFactorGibbs _sfactor;
	private Set<IndexedValue> _updates;
	private final int _incrementalUpdateThreshold;

	static enum Equal implements Comparator<SFactorUpdate>
	{
		INSTANCE;
		
		@Override
		public int compare(SFactorUpdate o1, SFactorUpdate o2)
		{
			return 0;
		}
	}
	
	/*--------------
	 * Construction
	 */
	
	SFactorUpdate(ISolverFactorGibbs sfactor)
	{
		_sfactor = sfactor;
		int nEdges = sfactor.getModelObject().getSiblingCount();
		_incrementalUpdateThreshold = sfactor.getModelObject().getFactorFunction().updateDeterministicLimit(nEdges);
		_updates = _incrementalUpdateThreshold > 0 ? new HashSet<IndexedValue>() : null;
	}
	
	/*----------------
	 * IKeyed methods
	 */
	
	@Override
	public final ISolverFactorGibbs getKey()
	{
		return _sfactor;
	}

	/*-----------------------
	 * SFactorUpdate methods
	 */
	
	void addVariableUpdate(int variableIndex, Value oldValue)
	{
		if (_updates != null)
		{
			_updates.add(new IndexedValue(variableIndex, oldValue));
			if (_updates.size() > _incrementalUpdateThreshold)
			{
				// Once we have exceeded the threshold, there is no point in
				// saving entries.
				_updates = null;
			}
		}
	}
	
	void performUpdate()
	{
		_sfactor.updateNeighborVariableValuesNow(_updates);
	}
	
	ISolverFactorGibbs sfactor()
	{
		return _sfactor;
	}
}
