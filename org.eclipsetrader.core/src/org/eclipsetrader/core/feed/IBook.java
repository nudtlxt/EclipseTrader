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

package org.eclipsetrader.core.feed;

/**
 * Level II Book interface.
 *
 * @since 1.0
 */
public interface IBook {

    /**
     * Gets the possibly empty array of bid proposals.
     *
     * @return the proposals array.
     */
    public IBookEntry[] getBidProposals();

    /**
     * Gets the possibly empty array of ask proposals.
     *
     * @return the proposals array.
     */
    public IBookEntry[] getAskProposals();
}
