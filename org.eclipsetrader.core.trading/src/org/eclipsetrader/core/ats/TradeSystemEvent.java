/*
 * Copyright (c) 2004-2011 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.core.ats;

public class TradeSystemEvent {

    public static final int KIND_ADDED = 1;
    public static final int KIND_REMOVED = 2;
    public static final int KIND_CHANGED = 3;

    public static final int KIND_STARTED = 4;
    public static final int KIND_STOPPED = 5;

    public int kind;
    public ITradeSystemService service;
    public ITradeSystem tradeSystem;
    public ITradeSystemContext tradeSystemContext;

    public TradeSystemEvent() {
    }
}
