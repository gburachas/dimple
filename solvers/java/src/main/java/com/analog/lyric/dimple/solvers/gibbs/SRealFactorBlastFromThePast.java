/*******************************************************************************
*   Copyright 2013 Analog Devices, Inc.
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

import java.util.Collection;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.model.core.Port;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.repeated.BlastFromThePastFactor;
import com.analog.lyric.dimple.model.values.IndexedValue;
import com.analog.lyric.dimple.model.values.Value;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.SBlastFromThePast;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactor;

public class SRealFactorBlastFromThePast extends SBlastFromThePast implements ISolverFactorGibbs
{
	private Value [] _inputMsgs;
	private Object[] _outputMsgs;
	private boolean _visited;
	
	public SRealFactorBlastFromThePast(BlastFromThePastFactor f)
	{
		super(f);
	}
	
	@Override
	public void createMessages(VariableBase var, Port port)
	{
		super.createMessages(var,port);
		getMessages();
	}
	
	private void getMessages()
	{
		VariableBase vb = (VariableBase)_portForOtherVar.node;
		int index = _portForOtherVar.index;
		Factor f = vb.getSibling(index);
		ISolverFactor sf = f.getSolver();
		int numEdges = f.getSiblingCount();
		_inputMsgs = new Value[numEdges];
		_outputMsgs = new Object[numEdges];
		for (int i = 0; i < numEdges; i++)
		{
			_inputMsgs[i] = (Value)sf.getInputMsg(i);
			_outputMsgs[i] = sf.getOutputMsg(i);
		}
	}
	
	@Override
	public void advance()
	{
		super.advance();
		getMessages();
	}

	@Override
	public double getPotential()
	{
	    int numPorts = _inputMsgs.length;
	    Object[] inPortMsgs = new Object[numPorts];
	    for (int port = 0; port < numPorts; port++)
	    	inPortMsgs[port] = _inputMsgs[port].getObject();
	    
	    return getPotential(inPortMsgs);
	}
	
	public double getPotential(Object[] inputs)
	{
		return getFactor().getFactorFunction().evalEnergy(inputs);
	}

	@Override
	public void updateNeighborVariableValue(int variableIndex, Value value)
	{
		throw DimpleException.unsupportedMethod(getClass(), "updateNeighborVariableValue");
	}

	@Override
	public void updateNeighborVariableValuesNow(Collection<IndexedValue> oldValue)
	{
		throw DimpleException.unsupportedMethod(getClass(), "updateNeighborVariableValuesNow");
	}

	@Override
	public void updateEdgeMessage(int portIndex)
	{
		// Do nothing
	}
	
	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _outputMsgs[portIndex];
	}

	@Override
	public boolean setVisited(boolean visited)
	{
		boolean changed = _visited ^ visited;
		_visited = visited;
		return changed;
	}
}
