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

package com.analog.lyric.dimple.solvers.gibbs.customFactors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.analog.lyric.dimple.factorfunctions.ExchangeableDirichlet;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.VariableBase;
import com.analog.lyric.dimple.solvers.core.parameterizedMessages.DirichletParameters;
import com.analog.lyric.dimple.solvers.gibbs.SRealFactor;
import com.analog.lyric.dimple.solvers.gibbs.SRealVariable;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.DirichletSampler;
import com.analog.lyric.dimple.solvers.gibbs.samplers.conjugate.IRealJointConjugateSamplerFactory;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;

public class CustomExchangeableDirichlet extends SRealFactor implements IRealJointConjugateFactor
{
	private Object[] _outputMsgs;
	private double _constantAlphaMinusOne;
	private SRealVariable _alphaVariable;
	private int _dimension;
	private int _numParameterEdges;
	private boolean _hasConstantParameters;
	private static final int PARAMETER_INDEX = 0;
	
	public CustomExchangeableDirichlet(Factor factor)
	{
		super(factor);
	}

	@Override
	public void updateEdgeMessage(int portNum)
	{
		if (portNum >= _numParameterEdges)
		{
			// Output port must be an output variable

			DirichletParameters outputMsg = (DirichletParameters)_outputMsgs[portNum];
			
			if (_hasConstantParameters)
				outputMsg.fillAlphaMinusOne(_constantAlphaMinusOne);
			else	// Variable parameters
				outputMsg.fillAlphaMinusOne(_alphaVariable.getCurrentSample() - 1);
		}
		else
			super.updateEdgeMessage(portNum);
	}
	
	
	@Override
	public Set<IRealJointConjugateSamplerFactory> getAvailableRealJointConjugateSamplers(int portNumber)
	{
		Set<IRealJointConjugateSamplerFactory> availableSamplers = new HashSet<IRealJointConjugateSamplerFactory>();
		if (isPortOutputVariable(portNumber))
			availableSamplers.add(DirichletSampler.factory);	// Output variables have Dirichlet distribution
		return availableSamplers;
	}
	
	public boolean isPortOutputVariable(int portNumber)
	{
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		return (portNumber >= _numParameterEdges);
	}

	
	
	@Override
	public void initialize()
	{
		super.initialize();
		
		// Determine what parameters are constants or edges, and save the state
		determineParameterConstantsAndEdges();
	}
	
	
	private void determineParameterConstantsAndEdges()
	{
		// Get the factor function and related state
		FactorFunction factorFunction = _factor.getFactorFunction();
		ExchangeableDirichlet specificFactorFunction = (ExchangeableDirichlet)factorFunction.getContainedFactorFunction();	// In case the factor function is wrapped
		_dimension = specificFactorFunction.getDimension();

		
		// Pre-determine whether or not the parameters are constant; if so save the value; if not save reference to the variable
		if (specificFactorFunction.hasConstantParameters())
		{
			_hasConstantParameters = true;
			_numParameterEdges = 0;
			_constantAlphaMinusOne = specificFactorFunction.getAlphaMinusOne();
			_alphaVariable = null;
		}
		else // Variable or constant parameter
		{
			_hasConstantParameters = factorFunction.isConstantIndex(PARAMETER_INDEX);
			if (_hasConstantParameters)
			{
				_numParameterEdges = 0;
				_constantAlphaMinusOne = (Double)factorFunction.getConstantByIndex(PARAMETER_INDEX) - 1;
				_alphaVariable = null;
			}
			else	// Parameter is a variable
			{
				_numParameterEdges = 1;
				_constantAlphaMinusOne = 0;
				List<? extends VariableBase> siblings = _factor.getSiblings();
				_alphaVariable = (SRealVariable)((siblings.get(PARAMETER_INDEX)).getSolver());
			}
		}
	}
	
	
	@Override
	public void createMessages()
	{
		super.createMessages();
		determineParameterConstantsAndEdges();	// Call this here since initialize may not have been called yet
		_outputMsgs = new Object[_numPorts];
		for (int port = _numParameterEdges; port < _numPorts; port++)	// Only output edges
			_outputMsgs[port] = new DirichletParameters(_dimension);
	}
	
	@Override
	public Object getOutputMsg(int portIndex)
	{
		return _outputMsgs[portIndex];
	}
	
	@Override
	public void moveMessages(ISolverNode other, int thisPortNum, int otherPortNum)
	{
		super.moveMessages(other, thisPortNum, otherPortNum);
		_outputMsgs[thisPortNum] = ((CustomExchangeableDirichlet)other)._outputMsgs[otherPortNum];
	}
}
