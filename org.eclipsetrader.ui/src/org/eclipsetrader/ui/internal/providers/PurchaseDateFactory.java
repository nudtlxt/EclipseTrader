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

package org.eclipsetrader.ui.internal.providers;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipsetrader.core.views.IDataProvider;
import org.eclipsetrader.core.views.IDataProviderFactory;
import org.eclipsetrader.core.views.IHolding;

public class PurchaseDateFactory extends AbstractProviderFactory {

    protected DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM);

    public class DataProvider implements IDataProvider {

        public DataProvider() {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#init(org.eclipse.core.runtime.IAdaptable)
         */
        @Override
        public void init(IAdaptable adaptable) {
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getFactory()
         */
        @Override
        public IDataProviderFactory getFactory() {
            return PurchaseDateFactory.this;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#getValue(org.eclipse.core.runtime.IAdaptable)
         */
        @Override
        public IAdaptable getValue(IAdaptable adaptable) {
            IHolding element = (IHolding) adaptable.getAdapter(IHolding.class);
            if (element != null && element.getDate() != null) {
                final Date value = element.getDate();
                return new IAdaptable() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public Object getAdapter(Class adapter) {
                        if (adapter.isAssignableFrom(String.class)) {
                            return formatter.format(value);
                        }
                        if (adapter.isAssignableFrom(Date.class)) {
                            return value;
                        }
                        return null;
                    }

                    @Override
                    public boolean equals(Object obj) {
                        if (!(obj instanceof IAdaptable)) {
                            return false;
                        }
                        Date s = (Date) ((IAdaptable) obj).getAdapter(Date.class);
                        return s == value || value != null && value.equals(s);
                    }
                };
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipsetrader.core.views.IDataProvider#dispose()
         */
        @Override
        public void dispose() {
        }
    }

    public PurchaseDateFactory() {
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IDataProviderFactory#createProvider()
     */
    @Override
    public IDataProvider createProvider() {
        return new DataProvider();
    }

    /* (non-Javadoc)
     * @see org.eclipsetrader.core.views.IDataProviderFactory#getType()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class[] getType() {
        return new Class[] {
                Date.class, String.class,
        };
    }
}
