/*
 * Copyright (c) 2004-2008 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 */

package org.eclipsetrader.ui.internal.charts;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;

public class DataUpdateWizard extends Wizard implements IImportWizard {
	private ImportDataPage dataPage;

	public DataUpdateWizard() {
    	setWindowTitle("Update Historical Data");
    	setDefaultPageImageDescriptor(ChartsUIActivator.imageDescriptorFromPlugin(ChartsUIActivator.PLUGIN_ID, "icons/wizban/import_wiz.png"));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}

	/* (non-Javadoc)
     * @see org.eclipse.jface.wizard.Wizard#addPages()
     */
    @Override
    public void addPages() {
	    addPage(dataPage = new ImportDataPage());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		DataImportJob job = new DataImportJob(dataPage.getCheckedSecurities(), dataPage.getImportType(), dataPage.getFromDate(), dataPage.getToDate(), dataPage.getAggregation());
		job.setUser(true);
		job.schedule();

		dataPage.saveState();

		return true;
	}
}
