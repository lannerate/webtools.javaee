/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 *  $RCSfile: VETimerTests.java,v $
 *  $Revision: 1.1 $  $Date: 2004/11/22 22:21:38 $ 
 */
package org.eclipse.jem.internal.temp;

import java.util.*;

/**
 * 
 * @since 1.0.2
 */
public class VETimerTests {

	/**
	 * Default TimerTests class to use when not using your own. It's a global.
	 */
	public static VETimerTests basicTest = new VETimerTests();

	public static final String CURRENT_PARENT_ID = "current parent";
	protected String currentParentId = null;


	protected static class TimerStep {
		static final int START = 0;
		static final int STOP = 1;
		static final int START_CUMULATIVE = 2;
		static final int STOP_CUMULATIVE = 3;
		static final int START_ACCUMULATING = 4;
		static final int STOP_ACCUMULATING = 5;
		protected String id;
		protected int type;
		protected long currentTime;
		protected int threadId;
}

	protected boolean testOn = false;
	protected List steps;

	public synchronized boolean startStep(String id) {
		if (!testOn)
			return true;

		TimerStep step = createTimerStep(id, TimerStep.START);
		return step != null;
	}

	protected TimerStep createTimerStep(String id, int stepType) {
		TimerStep newStep = new TimerStep();
		newStep.threadId = Thread.currentThread().hashCode();
		newStep.id = id;
		newStep.type = stepType;
		newStep.currentTime = System.currentTimeMillis();
		steps.add(newStep);

		return newStep;
	}

	public synchronized boolean stopStep(String id) {
		if (!testOn)
			return true;
		TimerStep step = createTimerStep(id, TimerStep.STOP);
		return step != null;
	}

	public synchronized boolean startAccumulating(String id) {
		if (!testOn)
			return true;

		return createTimerStep(id, TimerStep.START_ACCUMULATING) != null;
	}

	public synchronized boolean stopAccumulating(String id) {
		if (!testOn)
			return true;

		return createTimerStep(id, TimerStep.STOP_ACCUMULATING) != null;
	}
	public synchronized boolean startCumulativeStep(String id) {
		if (!testOn)
			return true;

		return createTimerStep(id, TimerStep.START_CUMULATIVE) != null;
	}

	public synchronized boolean stopCumulativeStep(String id) {
		if (!testOn)
			return true;
		return createTimerStep(id, TimerStep.STOP_CUMULATIVE) != null;
	}

	/**
	 * Clear the tests so that you can restart and do some more tests.
	 * 
	 * 
	 * @since 1.0.2
	 */
	public synchronized void clearTests() {
		if (!testOn)
			return;
		steps.clear();
		currentParentId = null;
	}

	/**
	 * Turn this test on. If not turned on then all calls will quickly return with no errors. This allows the code to stay in place even when not
	 * debugging.
	 * <p>
	 * When turned off, it will clear the test.
	 * 
	 * @param on
	 * 
	 * @since 1.0.2
	 */
	public synchronized void testState(boolean on) {
		if (on == testOn)
			return;
		if (on) {
			testOn = true;
			if (steps == null)
				steps = new ArrayList();
		} else {
			testOn = false;
			steps = null;
		}
		currentParentId = null;
	}
	private static class CumulativeInformation {
		public TimerStep currentCumulativeStep;
		public long cumTime;
		public int cumCount;
		public long maxTime;
		public long minTime = Integer.MAX_VALUE;
	}
	public synchronized void printIt() {
		if (!testOn)
			return;
		if (steps == null)
			return;
		Map stepInfoByThreadId = new HashMap();
		Map indentsByThreadId = new HashMap();
		Map cumSteps;
		TimerStep prevStep = null;
		TimerStep startStep;
		StringBuffer strb = new StringBuffer(150);
		for (int i = 0; i < steps.size(); i++) {
			TimerStep step = (TimerStep) steps.get(i);
			Integer threadId = new Integer(step.threadId);
			switch (step.type) {
				case TimerStep.START:
				case TimerStep.STOP:
					Integer threadIndent = (Integer) indentsByThreadId.get(threadId);
					int indent = 0;
					if (step.type == TimerStep.START) {
						if (threadIndent != null)
							indent = threadIndent.intValue() + 1;
						indentsByThreadId.put(threadId, new Integer(indent));
					} else {
						if (threadIndent != null)
							indent = threadIndent.intValue();
						if (indent > 0)
							indentsByThreadId.put(threadId, new Integer(indent - 1));
						else {
							indentsByThreadId.remove(threadId);
							indent = 0;
						}
					}
					strb.setLength(0);
					strb.append(step.currentTime);
					strb.append("\t");
					for (int j = 0; j < indent; j++) {
						strb.append("     ");
					}
					switch (step.type) {
						case TimerStep.START:
							strb.append("Start");
							break;

						case TimerStep.STOP:
							strb.append("Stop ");
							break;
						default:
							break;
					}
					;
					strb.append(" \"");
					strb.append(step.id);
					strb.append("\"   id(");
					strb.append(step.threadId);
					strb.append(")");
					Map startSteps = (Map) stepInfoByThreadId.get(threadId);
					if (startSteps == null)
						stepInfoByThreadId.put(threadId, startSteps = new HashMap());
					if (step.type == TimerStep.START) {
						// Store the start step for later lookup when calulating the total time
						startSteps.put(step.id, step);
					} else {
						// This is the stop time for a step. We need to find
						// the corresponding start time and calculate the total time.
						Object item = startSteps.remove(step.id);
						if (item instanceof TimerStep) {
							startStep = (TimerStep) item;
							if (startStep != null) {
								int addchars = 100 - strb.length();
								for (int j = 0; j < addchars; j++) {
									strb.append(" ");
								}
								strb.append("    Total = " + (step.currentTime - startStep.currentTime) + " ms");
							}
						} else
							strb.append("    ---> Couldn't find Starting point for \"" + step.id + "\"");
					}
					if (i > 0 && (step.currentTime - prevStep.currentTime) > 0)
						System.out.println("-- " + (step.currentTime - prevStep.currentTime) + " ms --");
					prevStep = step;
					System.out.println(strb);
					break;

				case TimerStep.START_ACCUMULATING:
					cumSteps = (Map) stepInfoByThreadId.get(threadId);
					if (cumSteps == null)
						stepInfoByThreadId.put(threadId, cumSteps = new HashMap());
					cumSteps.put(step.id, new CumulativeInformation());
					indent = 0;
					threadIndent = (Integer) indentsByThreadId.get(threadId);
					if (threadIndent != null)
						indent = threadIndent.intValue() + 1;
					indentsByThreadId.put(threadId, new Integer(indent));
					strb.setLength(0);
					strb.append(step.currentTime);
					strb.append("\t");
					for (int j = 0; j < indent; j++) {
						strb.append("     ");
					}
					strb.append("Start Accumulating");
					strb.append(" \"");
					strb.append(step.id);
					strb.append("\"   id(");
					strb.append(step.threadId);
					strb.append(")");
					if (i > 0 && (step.currentTime - prevStep.currentTime) > 0)
						System.out.println("-- " + (step.currentTime - prevStep.currentTime) + " ms --");
					prevStep = step;
					System.out.println(strb);
					break;

				case TimerStep.START_CUMULATIVE:
					cumSteps = (Map) stepInfoByThreadId.get(threadId);
					if (cumSteps != null) {
						Object info = cumSteps.get(step.id);
						if (info instanceof CumulativeInformation)
							((CumulativeInformation) info).currentCumulativeStep = step;
					}
					break;
					
				case TimerStep.STOP_CUMULATIVE:
					cumSteps = (Map) stepInfoByThreadId.get(threadId);
					if (cumSteps != null) {
						Object info = cumSteps.get(step.id);
						if (info instanceof CumulativeInformation) {
							CumulativeInformation cumInfo = (CumulativeInformation) info;
							if (cumInfo.currentCumulativeStep != null) {
								cumInfo.cumCount++;
								long delta = step.currentTime - cumInfo.currentCumulativeStep.currentTime;
								cumInfo.cumTime += delta;
								if (cumInfo.maxTime < delta)
									cumInfo.maxTime = delta;
								if (delta < cumInfo.minTime)
									cumInfo.minTime = delta;
							}
						}
					}
					break;
					
				case TimerStep.STOP_ACCUMULATING:
					indent = 0;
					threadIndent = (Integer) indentsByThreadId.get(threadId);
					if (threadIndent != null)
						indent = threadIndent.intValue();
					if (indent > 0)
						indentsByThreadId.put(threadId, new Integer(indent - 1));
					else {
						indentsByThreadId.remove(threadId);
						indent = 0;
					}
					strb.setLength(0);
					strb.append(step.currentTime);
					strb.append("\t");
					for (int j = 0; j < indent; j++) {
						strb.append("     ");
					}
					strb.append("Stop  Accumulating");
					strb.append(" \"");
					strb.append(step.id);
					strb.append("\"   id(");
					strb.append(step.threadId);
					strb.append(")");
					cumSteps = (Map) stepInfoByThreadId.get(threadId);
					if (cumSteps != null) {
						Object info = cumSteps.get(step.id);
						if (info instanceof CumulativeInformation) {
							CumulativeInformation cumInfo = (CumulativeInformation) info;
							if (cumInfo.currentCumulativeStep != null) {
								strb.append("   cumulative time=");
								strb.append(cumInfo.cumTime);
								strb.append("   cumulative count=");
								strb.append(cumInfo.cumCount);
								strb.append("   max time=");
								strb.append(cumInfo.maxTime);
								strb.append("   min time=");
								strb.append(cumInfo.minTime);
							}
						}
					}
					if (i > 0 && (step.currentTime - prevStep.currentTime) > 0)
						System.out.println("-- " + (step.currentTime - prevStep.currentTime) + " ms --");
					prevStep = step;
					System.out.println(strb);
					break;
			}
		}
	}
}