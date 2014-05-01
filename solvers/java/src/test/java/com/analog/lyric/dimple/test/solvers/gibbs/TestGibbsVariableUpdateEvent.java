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

package com.analog.lyric.dimple.test.solvers.gibbs;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.analog.lyric.dimple.events.DimpleEvent;
import com.analog.lyric.dimple.events.DimpleEventHandler;
import com.analog.lyric.dimple.events.DimpleEventListener;
import com.analog.lyric.dimple.events.IDimpleEventSource;
import com.analog.lyric.dimple.factorfunctions.core.FactorFunction;
import com.analog.lyric.dimple.model.core.FactorGraph;
import com.analog.lyric.dimple.model.domains.DiscreteDomain;
import com.analog.lyric.dimple.model.factors.Factor;
import com.analog.lyric.dimple.model.variables.Complex;
import com.analog.lyric.dimple.model.variables.Discrete;
import com.analog.lyric.dimple.model.variables.Real;
import com.analog.lyric.dimple.schedulers.GibbsSequentialScanScheduler;
import com.analog.lyric.dimple.solvers.gibbs.GibbsScoredVariableUpdateEvent;
import com.analog.lyric.dimple.solvers.gibbs.GibbsSolver;
import com.analog.lyric.dimple.solvers.gibbs.GibbsVariableUpdateEvent;
import com.analog.lyric.dimple.solvers.gibbs.ISolverVariableGibbs;
import com.analog.lyric.dimple.solvers.gibbs.SFactorGraph;

/**
 * Test generation of {@link GibbsVariableUpdateEvent}s.
 * 
 * @since 0.06
 * @author Christopher Barber
 */
public class TestGibbsVariableUpdateEvent
{
	static class BogoFunction extends FactorFunction
	{
		@Override
		public double evalEnergy(Object... arguments)
		{
			double energy = 0.0;
			
			for (Object arg : arguments)
			{
				energy += 1.0;
				if (arg instanceof Number)
				{
					energy += Math.abs(((Number) arg).doubleValue());
				}
				else if (arg instanceof double[])
				{
					for (double d : (double[])arg)
					{
						energy += Math.abs(d);
					}
				}
				else
				{
					throw new Error("die");
				}
			}
			
			return energy;
		}
	}
	
	static class VariableUpdateHandler extends DimpleEventHandler<GibbsVariableUpdateEvent>
	{
		List<GibbsVariableUpdateEvent> events = new ArrayList<GibbsVariableUpdateEvent>();
		
		@Override
		public void handleEvent(GibbsVariableUpdateEvent event)
		{
			events.add(event);
			printEvent(event);
			SFactorGraph sgraph = (SFactorGraph)event.getSource().getRootGraph();
			System.out.format("total score: %s\n", sgraph.getTotalPotential());
			
			ISolverVariableGibbs variable = event.getSource();
			assertTrue(event.getNewValue().valueEquals(variable.getCurrentSampleValue()));
			if (event instanceof GibbsScoredVariableUpdateEvent)
			{
				GibbsScoredVariableUpdateEvent scoredEvent = (GibbsScoredVariableUpdateEvent)event;
				assertEquals(variable.getCurrentSampleScore(), scoredEvent.getNewSampleScore(), 0.0);
				
				assertEquals(scoredEvent.getNewSampleScore() - scoredEvent.getOldSampleScore(),
					scoredEvent.getScoreDifference(), 1e-15);
			}
		}
		
		void printEvent(GibbsVariableUpdateEvent event)
		{
			System.out.format("%s: %s %s => %s",
				event.getClass().getSimpleName(), event.getModelObject().getName(),
				event.getOldValue(), event.getNewValue());
			if (event instanceof GibbsScoredVariableUpdateEvent)
			{
				GibbsScoredVariableUpdateEvent scoredEvent = (GibbsScoredVariableUpdateEvent)event;
				System.out.format(" score %+f", scoredEvent.getScoreDifference());
			}
			System.out.println("");
		}
	}
	
	@Test
	public void test()
	{
		//
		// Set up model/solver
		//
		
		final FactorFunction function = new BogoFunction();
		
		FactorGraph model = new FactorGraph();
		Discrete d1 = new Discrete(DiscreteDomain.range(0, 9));
		d1.setName("d1");
		Real r1 = new Real();
		r1.setName("r1");
		Complex c1 = new Complex();
		c1.setName("c1");
		
		model.addVariables(d1, r1, c1);
		
		Factor fdr = model.addFactor(function, d1, r1);
		Factor frc = model.addFactor(function, r1, c1);
		Factor fcd = model.addFactor(function, c1, d1);
		
		SFactorGraph sgraph = model.setSolverFactory(new GibbsSolver());
		ISolverVariableGibbs sd1 = sgraph.getSolverVariable(d1);
		ISolverVariableGibbs sr1 = sgraph.getSolverVariable(r1);
		ISolverVariableGibbs sc1 = sgraph.getSolverVariable(c1);
		
		sgraph.setBurnInScans(0);
		sgraph.setNumSamples(1);
		sgraph.setTemperature(1.0);
		model.setScheduler(new GibbsSequentialScanScheduler());
		
		//
		// Set up listener
		//
		
		DimpleEventListener listener = new DimpleEventListener();
		VariableUpdateHandler handler = new VariableUpdateHandler();
		listener.register(handler, GibbsVariableUpdateEvent.class, false, model);
		assertTrue(listener.isListeningFor(GibbsVariableUpdateEvent.class, model));
		assertTrue(listener.isListeningFor(GibbsVariableUpdateEvent.class, sc1));
		assertFalse(listener.isListeningFor(GibbsScoredVariableUpdateEvent.class, sc1));

		model.setEventListener(listener);
		assertSame(listener, model.getEventListener());
		assertSame(listener, sd1.getEventListener());

		model.solve();
		assertEvents(handler, GibbsVariableUpdateEvent.class, sd1, sr1, sc1);

		model.setEventListener(null);
		model.solve();
		assertEvents(handler, GibbsVariableUpdateEvent.class);

		model.setEventListener(listener);
		listener.block(GibbsVariableUpdateEvent.class, false,  sr1);
		model.solve();
		assertEvents(handler, GibbsVariableUpdateEvent.class, sd1, sc1);

		listener.unblock(GibbsVariableUpdateEvent.class,  sr1);
		model.solve();
		assertEvents(handler, GibbsVariableUpdateEvent.class, sd1, sr1, sc1);

		listener.register(handler, GibbsVariableUpdateEvent.class, true, model);
		model.solve();
		assertEvents(handler, GibbsScoredVariableUpdateEvent.class, sd1, sr1, sc1);
		double prevScore = sgraph.getTotalPotential();
		sgraph.sample();
		double score = sgraph.getTotalPotential();
		double scoreDifference = assertEvents(handler, GibbsScoredVariableUpdateEvent.class, sd1, sr1, sc1);
		assertEquals(score - prevScore, scoreDifference, 1e-15);
		
		listener.unregisterAll();
	}
	
	private double assertEvents(VariableUpdateHandler handler, Class<? extends DimpleEvent> expectedClass,
		IDimpleEventSource ... sources)
	{
		double scoreDifference = 0.0;
		final int nSources = sources.length;
		assertEquals(nSources, handler.events.size());
		for (int i = 0; i < nSources; ++i)
		{
			GibbsVariableUpdateEvent event = handler.events.get(i);
			assertSame(expectedClass, event.getClass());
			assertSame(sources[i], event.getSource());
			
			if (event instanceof GibbsScoredVariableUpdateEvent)
			{
				scoreDifference += ((GibbsScoredVariableUpdateEvent)event).getScoreDifference();
			}
		}
		handler.events.clear();
		
		return scoreDifference;
	}
}
