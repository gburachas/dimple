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

package com.analog.lyric.dimple.factorfunctions;

import com.analog.lyric.dimple.exceptions.DimpleException;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunctionUtilities;


/**
 * Parameterized Bernoulli distribution, which corresponds to p(x | p),
 * where p is the probability parameter
 * 
 * The conjugate prior for p is a Beta distribution.
 * Depending on the solver, it may or may not be necessary to use a
 * conjugate prior (for the Gibbs solver, for example, it is not).
 * 
 * The variables in the argument list are ordered as follows:
 * 
 * 1) p: Probability parameter
 * 2...) An arbitrary number of discrete output variable (MUST be zero-based integer values [e.g., Bit variable]) 	// TODO: remove this restriction
 * 
 * The parameter may optionally be specified as a constant in the constructor.
 * In this case, the parameter is not included in the list of arguments.
 */
public class Bernoulli extends FactorFunction
{
	private double _p;
	private boolean _parametersConstant;
	private int _firstDirectedToIndex;
	
	public Bernoulli()		// Variable parameter
	{
		super();
		_parametersConstant = false;
		_firstDirectedToIndex = 1;
	}
	public Bernoulli(double p)	// Constant parameter
	{
		super();
		_p = p;
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
		if (p < 0 || p > 1) throw new DimpleException("Invalid parameter value.  Must be in range [0, 1].");
	}

    @Override
	public double evalEnergy(Object... arguments)
    {
    	double p;
    	if (_parametersConstant)
    	{
    		p = _p;
    	}
    	else
    	{
    		p = FactorFunctionUtilities.toDouble(arguments[0]);			// First argument, if present, is parameter, p
    		if (p < 0 || p > 1) return Double.POSITIVE_INFINITY;
    	}
		
		int numZeros = 0;
		int length = arguments.length;
    	for (int i = _firstDirectedToIndex; i < length; i++)
    	{
    		int x = FactorFunctionUtilities.toInteger(arguments[i]);	// Remaining arguments are Discrete or Bit variables
    		if (x == 0) numZeros++;
    	}
		int N = length - _firstDirectedToIndex;							// Number of non-parameter variables
    	int numOnes = N - numZeros;
    	

    	if (p == 0)
    		if (numOnes > 0)
    			return Double.POSITIVE_INFINITY;
    		else
    			return 0;
    	else if (p == 1)
    		if (numZeros > 0)
    			return Double.POSITIVE_INFINITY;
    		else
    			return 0;
    	else
    		return -(numOnes * Math.log(p) + numZeros * Math.log(1-p));
	}
    
    @Override
    public final boolean isDirected() {return true;}
    @Override
	public final int[] getDirectedToIndices(int numEdges)
	{
    	// All edges except the parameter edges (if present) are directed-to edges
		return FactorFunctionUtilities.getListOfIndices(_firstDirectedToIndex, numEdges-1);
	}
    
    
    // Factor-specific methods
    public final boolean hasConstantParameters()
    {
    	return _parametersConstant;
    }
    public final double getParameter()
    {
    	return _p;
    }
}
