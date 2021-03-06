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

package com.analog.lyric.dimple.solvers.sumproduct.customFactors;

import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.solvers.core.SFactorBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.NormalParameters;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.dimple.solvers.interfaces.ISolverVariable;

public abstract class GaussianFactorBase extends SFactorBase
{
	protected NormalParameters[] _inputMsgs;
	protected NormalParameters[] _outputMsgs;
	
	public GaussianFactorBase(Factor factor)
	{
		super(factor);
	}


	@Override
	public void resetEdgeMessages(int i)
	{
		_inputMsgs[i].setNull();
		_outputMsgs[i].setNull();
	}

	@Override
	public void createMessages()
	{
		final Factor factor = _factor;
		final int nVars = factor.getSiblingCount();
		
	    _inputMsgs = new NormalParameters[nVars];
	    _outputMsgs = new NormalParameters[nVars];
		// Messages are created in variable constructor, just save a reference here
	    for (int index = 0; index < nVars; ++index)
		{
			ISolverVariable sv = factor.getSibling(index).getSolver();
			Object[] messages = sv.createMessages(this);
			_outputMsgs[index] = (NormalParameters)messages[0];
			_inputMsgs[index] = (NormalParameters)messages[1];
		}
		
	}

	@Override
	public Object getInputMsg(int portIndex)
	{
		return _inputMsgs[portIndex];
	}

	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _outputMsgs[portIndex];
	}
	

	@Override
	public void moveMessages(ISolverNode other, int portNum, int otherPort)
	{
		GaussianFactorBase s = (GaussianFactorBase)other;
	
		_inputMsgs[portNum] = s._inputMsgs[otherPort];
		_outputMsgs[portNum] = s._outputMsgs[otherPort];
	}
}
