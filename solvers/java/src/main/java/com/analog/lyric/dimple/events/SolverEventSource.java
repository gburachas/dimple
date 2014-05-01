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

package com.analog.lyric.dimple.events;

import com.analog.lyric.collect.BitSetUtil;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverFactorGraph;
import com.analog.lyric.dimple.solvers.interfaces.ISolverNode;
import com.analog.lyric.options.AbstractOptionHolder;
import com.analog.lyric.util.misc.Internal;

/**
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public abstract class SolverEventSource extends AbstractOptionHolder implements ISolverEventSource, ISolverNode
{
	/*-----------
	 * Constants
	 */
	
	/**
	 * Bits in {@link #_flags} reserved by this class.
	 */
	protected static final int RESERVED_FLAGS = 0xF0000000;
	
	/*-------
	 * State
	 */
	
	/**
	 * Temporary flags that can be used to mark the node during the execution of various algorithms
	 * or to mark non-static attributes of the node.
	 * <p>
	 * The flags are automatically cleared by {@link #initialize()}.
	 * <p>
	 * Subclasses should document which flags are reserved for use by that class and which ones
	 * are available for use by subclasses.
	 * <p>
	 * Flags should generally be get/set using method provided by this class.
	 * <p>
	 * @since 0.06
	 * @see #clearFlags()
	 * @see #clearFlags(int)
	 * @see #isFlagSet(int)
	 * @see #setFlags(int)
	 */
	protected int _flags;
	
	/*----------------------------
	 * IDimpleEventSource methods
	 */
	
    @Override
	public FactorGraph getContainingGraph()
	{
    	return getModelObject().getContainingGraph();
	}

    @Override
    public IDimpleEventListener getEventListener()
    {
    	return getContainingGraph().getEventListener();
    }
    
    @Override
    public ISolverFactorGraph getEventParent()
    {
    	return getParentGraph();
    }
    
	@Override
	public String getEventSourceName()
	{
		// FIXME - determine what this should be
		return toString();
	}

    @Override
    public IModelEventSource getModelEventSource()
    {
    	return getModelObject();
    }
    
	/*----------------------------
	 * ISolverEventSource methods
	 */
	
    @Override
	public ISolverFactorGraph getContainingSolverGraph()
	{
    	return getParentGraph();
	}
    
    @Override
    public void notifyListenerChanged()
    {
    	clearFlags(getEventMask());
    }

    /*----------------------------
     * Protected/internal methods
     */

    /**
	 * Clear all flag values. Invoked automatically by {@link #initialize()}.
	 */
	protected final void clearFlags()
	{
		_flags = 0;
	}
	
	/**
	 * Clear flags in given mask.
	 */
	protected final void clearFlags(int mask)
	{
		_flags = (byte) BitSetUtil.clearMask(_flags, mask);
	}
	
	/**
	 * Return mask of flag bits that are used to determine whether to
	 * generate events. This is used by {@link #notifyListenerChanged()}
	 * to clear the specified flag bits. It is assumed that the value of
	 * all zeros indicates that the object needs to recompute its flags
	 * based on the listener.
	 * <p>
	 * The default implementation returns zero.
	 *
	 * @since 0.06
	 */
	protected int getEventMask()
	{
		return 0;
	}
	
	@Internal
	public final int getFlagValue(int mask)
	{
		return BitSetUtil.getMaskedValue(_flags, mask);
	}
	
	/**
	 * True if all of the bits in {@code mask} are set in the flags.
	 */
	protected final boolean isFlagSet(int mask)
	{
		return BitSetUtil.isMaskSet(_flags, mask);
	}
	
	protected final boolean raiseEvent(SolverEvent event)
	{
		IDimpleEventListener listener = getEventListener();
		if (listener != null)
		{
			listener.raiseEvent(event);
			return true;
		}
		return false;
	}
	
	/**
	 * Sets all of the bits in {@code mask} in the flags.
	 */
	protected final void setFlags(int mask)
	{
		_flags = (byte) BitSetUtil.setMask(_flags, mask);
	}

	/**
	 * Sets bits of flag specified by {@code mask} to {@code value}.
	 */
	@Internal
	public final void setFlagValue(int mask, int value)
	{
		_flags = (byte) BitSetUtil.setMaskedValue(_flags, mask, value);
	}
}
