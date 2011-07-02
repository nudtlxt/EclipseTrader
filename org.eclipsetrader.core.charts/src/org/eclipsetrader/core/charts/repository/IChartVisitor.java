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

package org.eclipsetrader.core.charts.repository;

/**
 * Interface implemented by objects that visits chart trees.
 *
 * @since 1.0
 */
public interface IChartVisitor {

    public boolean visit(IChartTemplate chart);

    public boolean visit(IChartSection section);

    public boolean visit(ISecuritySection section);

    public void visit(IElementSection section);
}
