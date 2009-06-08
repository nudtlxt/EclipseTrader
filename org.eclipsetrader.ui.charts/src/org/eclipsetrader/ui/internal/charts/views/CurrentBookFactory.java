/*
 * Copyright (c) 2004-2009 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.charts.views;

import org.eclipsetrader.core.charts.IDataSeries;
import org.eclipsetrader.core.feed.IBook;
import org.eclipsetrader.core.feed.IPricingListener;
import org.eclipsetrader.core.feed.ITrade;
import org.eclipsetrader.core.feed.PricingDelta;
import org.eclipsetrader.core.feed.PricingEvent;
import org.eclipsetrader.core.instruments.ISecurity;
import org.eclipsetrader.core.markets.IMarketService;
import org.eclipsetrader.core.markets.MarketPricingEnvironment;
import org.eclipsetrader.ui.charts.IChartObject;
import org.eclipsetrader.ui.charts.IChartObjectFactory;
import org.eclipsetrader.ui.charts.IChartParameters;
import org.eclipsetrader.ui.internal.charts.ChartsUIActivator;

public class CurrentBookFactory implements IChartObjectFactory {
	private ISecurity security;
	private MarketPricingEnvironment pricingEnvironment;
	private CurrentBook object = new CurrentBook();

	private IPricingListener pricingListener = new IPricingListener() {
		public void pricingUpdate(PricingEvent event) {
			if (!event.getSecurity().equals(security))
				return;
			for (PricingDelta delta : event.getDelta()) {
				if (delta.getNewValue() instanceof IBook)
					object.setBook((IBook) delta.getNewValue());
				if (delta.getNewValue() instanceof ITrade)
					object.setTrade((ITrade) delta.getNewValue());
			}
		}
	};

	public CurrentBookFactory() {
		IMarketService marketService = ChartsUIActivator.getDefault().getMarketService();

		pricingEnvironment = new MarketPricingEnvironment(marketService);
		pricingEnvironment.addPricingListener(pricingListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getId()
	 */
	public String getId() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getName()
	 */
	public String getName() {
		return "Current Price";
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#createObject(org.eclipsetrader.core.charts.IDataSeries)
	 */
	public IChartObject createObject(IDataSeries source) {
		return object;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#getParameters()
	 */
	public IChartParameters getParameters() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipsetrader.ui.charts.IChartObjectFactory#setParameters(org.eclipsetrader.ui.charts.IChartParameters)
	 */
	public void setParameters(IChartParameters parameters) {
	}

	public void setSecurity(ISecurity security) {
		this.security = security;
	}

	public void setEnable(boolean enable) {
		if (enable) {
			pricingEnvironment.addSecurity(security);
			pricingEnvironment.addLevel2Security(security);

			ITrade trade = pricingEnvironment.getTrade(security);
			IBook book = pricingEnvironment.getBook(security);

			object.setTrade(trade);
			object.setBook(book);
		}
		else {
			pricingEnvironment.removeLevel2Security(security);
			pricingEnvironment.removeSecurity(security);
			object.setBook(null);
		}
	}

	public void dispose() {
		pricingEnvironment.dispose();
	}
}