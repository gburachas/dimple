/*******************************************************************************
 *   Copyright 2012 Analog Devices, Inc.
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
 * Gamma distribution. The variables in the argument list are ordered as follows:
 * 
 * 1) Alpha: Alpha parameter of the Beta distribution (non-negative)
 * 2) Beta: Beta parameter of the Beta distribution (non-negative)
 * 3...) An arbitrary number of real variables
 * 
 * Alpha and Beta parameters may optionally be specified as constants in the constructor.
 * In this case, they are not included in the list of arguments.
 * 
 */
public class Beta extends FactorFunction
{
	protected double _alpha;
	protected double _beta;
	protected double _alphaMinusOne;
	protected double _betaMinusOne;
	protected double _logBetaAlphaBeta;
	protected boolean _parametersConstant = false;
	protected int _firstDirectedToIndex = 2;

	public Beta() {super();}
	public Beta(double alpha, double beta)
	{
		this();
		_alpha = alpha;
		_beta = beta;
		_alphaMinusOne = _alpha - 1;
		_betaMinusOne = _beta - 1;
		_logBetaAlphaBeta = org.apache.commons.math3.special.Beta.logBeta(_alpha, _beta);
		_parametersConstant = true;
		_firstDirectedToIndex = 0;
		if (_alpha < 0) throw new DimpleException("Negative alpha parameter. This must be a non-negative value.");
		if (_beta < 0) throw new DimpleException("Negative beta parameter. This must be a non-negative value.");
	}

	@Override
	public double evalEnergy(Object... arguments)
	{
		int index = 0;
		if (!_parametersConstant)
		{
			_alpha = FactorFunctionUtilities.toDouble(arguments[index++]);	// First input is alpha parameter (must be non-negative)
			_beta = FactorFunctionUtilities.toDouble(arguments[index++]);	// Second input is beta parameter (must be non-negative)
			_alphaMinusOne = _alpha - 1;
			_betaMinusOne = _beta - 1;
			_logBetaAlphaBeta = org.apache.commons.math3.special.Beta.logBeta(_alpha, _beta);
			if (_alpha < 0) return Double.POSITIVE_INFINITY;
			if (_beta < 0) return Double.POSITIVE_INFINITY;
		}
    	int length = arguments.length;
    	int N = length - index;			// Number of non-parameter variables
    	double sum = 0;
    	if (_alpha == 1 && _beta == 1)
    	{
    		for (; index < length; index++)
    		{
    			double x = FactorFunctionUtilities.toDouble(arguments[index]);				// Remaining inputs are Beta variables
        		if (x < 0 || x > 1)
        			return Double.POSITIVE_INFINITY;
    		}
    		return 0;	// Uniform within 0 <= x <= 1
    	}
    	else if (_alpha == 1)
    	{
    		for (; index < length; index++)
    		{
    			double x = FactorFunctionUtilities.toDouble(arguments[index]);				// Remaining inputs are Beta variables
    			sum += Math.log(1 - x);
    		}
    		return N * _logBetaAlphaBeta - sum * _betaMinusOne;
    	}
		else if (_beta == 1)
		{
    		for (; index < length; index++)
    		{
    			double x = FactorFunctionUtilities.toDouble(arguments[index]);				// Remaining inputs are Beta variables
    			sum += Math.log(x);
    		}
    		return N * _logBetaAlphaBeta - sum * _alphaMinusOne;
		}
		else
		{
    		for (; index < length; index++)
    		{
    			double x = FactorFunctionUtilities.toDouble(arguments[index]);				// Remaining inputs are Beta variables
    			sum += _alphaMinusOne * Math.log(x) + _betaMinusOne * Math.log(1 - x);
    		}
    		return N * _logBetaAlphaBeta - sum;
		}
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
    public final double getAlphaMinusOne()	// The natural additive parameter, alpha - 1
    {
    	return _alphaMinusOne;
    }
    public final double getBetaMinusOne()	// The natural additive parameter, beta - 1
    {
    	return _betaMinusOne;
    }
}
