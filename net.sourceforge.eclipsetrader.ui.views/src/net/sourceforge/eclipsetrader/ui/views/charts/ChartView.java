/*******************************************************************************
 * Copyright (c) 2004 Marco Maccaferri and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     Marco Maccaferri - initial API and implementation
 *******************************************************************************/
package net.sourceforge.eclipsetrader.ui.views.charts;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.sourceforge.eclipsetrader.IBasicData;
import net.sourceforge.eclipsetrader.IChartData;
import net.sourceforge.eclipsetrader.IChartDataProvider;
import net.sourceforge.eclipsetrader.IIndexDataProvider;
import net.sourceforge.eclipsetrader.TraderPlugin;
import net.sourceforge.eclipsetrader.ui.internal.views.ViewsPlugin;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.part.ViewPart;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class ChartView extends ViewPart implements ControlListener, MouseListener, MouseMoveListener, PaintListener, SelectionListener
{
  protected Color background = new Color(null, 255, 255, 255);
  protected Color lineColor = new Color(null, 0, 0, 255);
  protected Color textColor = new Color(null, 0, 0, 0);
  protected Color positiveColor = new Color(null, 0, 192, 0);
  protected Color negativeColor = new Color(null, 192, 0, 0);
  protected Color yearColor = new Color(null, 192, 0, 0);
  protected int width = 5;
  protected int margin = 2;
  protected int scaleWidth = 60;
  protected int limitPeriod = 12;
  protected Composite container;
  protected Composite composite;
  protected SashForm form;
  protected Composite bottombar;
  protected Label title;
  protected Label date;
  protected Label closePrice;
  protected Label maxPrice;
  protected Label minPrice;
  protected Label variance;
  protected Label volume;
  protected IChartDataProvider dataProvider;
  protected IBasicData basicData;
  protected IChartData[] data = new IChartData[0];
  protected Vector chart = new Vector();
  protected NumberFormat nf = NumberFormat.getInstance();
  protected NumberFormat pf = NumberFormat.getInstance();
  protected NumberFormat pcf = NumberFormat.getInstance();
  protected SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
  protected int selectedZone = 0;
  
  public ChartView()
  {
    nf.setGroupingUsed(true);
    nf.setMinimumIntegerDigits(1);
    nf.setMinimumFractionDigits(0);
    nf.setMaximumFractionDigits(0);

    pf.setGroupingUsed(true);
    pf.setMinimumIntegerDigits(1);
    pf.setMinimumFractionDigits(4);
    pf.setMaximumFractionDigits(4);

    pcf.setMinimumIntegerDigits(1);
    pcf.setMinimumFractionDigits(2);
    pcf.setMaximumFractionDigits(2);
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  public void createPartControl(Composite parent)
  {
    container = new Composite(parent, SWT.H_SCROLL);
    container.getHorizontalBar().addSelectionListener(this);
    GridLayout gridLayout = new GridLayout(1, false);
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 0;
    gridLayout.verticalSpacing = 0;
    container.setLayout(gridLayout);
    container.addControlListener(this);
    
    composite = new Composite(container, SWT.NONE);
    composite.setBackground(background);
    gridLayout = new GridLayout(20, false);
    gridLayout.marginWidth = 3;
    gridLayout.marginHeight = 0;
    gridLayout.horizontalSpacing = 5;
    gridLayout.verticalSpacing = 0;
    composite.setLayout(gridLayout);
    GridData gridData = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_FILL);
    gridData.heightHint = 16;
    composite.setLayoutData(gridData);
    composite.addPaintListener(this);
    title = new Label(composite, SWT.NONE);
    title.setBackground(title.getParent().getBackground());
    title.setLayoutData(new GridData());
    Label label = new Label(composite, SWT.NONE);
    label.setText("Data:");
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    date = new Label(composite, SWT.NONE);
    date.setForeground(lineColor);
    date.setBackground(title.getParent().getBackground());
    date.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText("Val.:");
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    closePrice = new Label(composite, SWT.NONE);
    closePrice.setForeground(lineColor);
    closePrice.setBackground(title.getParent().getBackground());
    closePrice.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText("Max.:");
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    maxPrice = new Label(composite, SWT.NONE);
    maxPrice.setForeground(lineColor);
    maxPrice.setBackground(title.getParent().getBackground());
    maxPrice.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText("Min.:");
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    minPrice = new Label(composite, SWT.NONE);
    minPrice.setForeground(lineColor);
    minPrice.setBackground(title.getParent().getBackground());
    minPrice.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText("Var.:");
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    variance = new Label(composite, SWT.NONE);
    variance.setForeground(lineColor);
    variance.setBackground(title.getParent().getBackground());
    variance.setLayoutData(new GridData());
    label = new Label(composite, SWT.NONE);
    label.setText("Vol.:");
    label.setBackground(title.getParent().getBackground());
    label.setLayoutData(new GridData());
    volume = new Label(composite, SWT.NONE);
    volume.setForeground(lineColor);
    volume.setBackground(title.getParent().getBackground());
    volume.setLayoutData(new GridData());

    form = new SashForm(container, SWT.VERTICAL);
    form.setLayoutData(new GridData(GridData.FILL_BOTH));

    bottombar = new Canvas(container, SWT.NONE);
    bottombar.setBackground(background);
    gridData = new GridData(GridData.FILL_HORIZONTAL|GridData.VERTICAL_ALIGN_FILL);
    gridData.heightHint = 20;
    bottombar.setLayoutData(gridData);
    bottombar.addPaintListener(this);
  }

  public abstract void reloadPreferences();
  
  public void reloadPreferences(File folder)
  {
    Vector sectionHeights = new Vector();
    
    // Remove all charts
    for (int i = 0; i < chart.size(); i++)
    {
      ChartCanvas canvas = (ChartCanvas)chart.elementAt(i);
      canvas.removeMouseListener(this);
      canvas.dispose();
    }
    chart.removeAllElements();
    limitPeriod = 12;

    // Read the preferences files for the new chart
    File f = new File(folder, basicData.getSymbol().toLowerCase() + ".prefs");
    if (f.exists() == true)
    {
      try {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(f);

        int index = 0;
        NodeList firstChild = document.getFirstChild().getChildNodes();
        for (int i = 0; i < firstChild.getLength(); i++)
        {
          Node n = firstChild.item(i);
          if (n.getNodeName().equalsIgnoreCase("settings"))
          {
            Node attr = n.getAttributes().getNamedItem("limit");
            if (attr != null)
              limitPeriod = Integer.parseInt(attr.getNodeValue());
          }
          else if (n.getNodeName().equalsIgnoreCase("section"))
          {
            ChartCanvas canvas = new ChartCanvas(form);
            canvas.createContextMenu(this);
            canvas.addMouseListener(this);
            chart.addElement(canvas);

            // Standard attributes
            Node attr = n.getAttributes().getNamedItem("price");
            if (attr != null && attr.getNodeValue().equalsIgnoreCase("true") == true)
              canvas.addPainter(new PriceChart());
            attr = n.getAttributes().getNamedItem("volume");
            if (attr != null && attr.getNodeValue().equalsIgnoreCase("true") == true)
              canvas.addPainter(new VolumeChart());
            String height = n.getAttributes().getNamedItem("height").getNodeValue();
            sectionHeights.addElement(new Integer(height));

            // Charts
            NodeList parent = n.getChildNodes();
            for (int ii = 0; ii < parent.getLength(); ii++)
            {
              Node item = parent.item(ii);
              if (item.getNodeName().equalsIgnoreCase("chart") == true)
              {
                Object obj = null;
                String id = item.getAttributes().getNamedItem("id").getNodeValue();
                if (id.equalsIgnoreCase("price") == true)
                  obj = new PriceChart();
                else
                {
                  IExtensionRegistry registry = Platform.getExtensionRegistry();
                  IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.chartPlotter");
                  if (extensionPoint != null)
                  {
                    IConfigurationElement[] members = extensionPoint.getConfigurationElements();
                    for (int m = 0; m < members.length; m++)
                    {
                      IConfigurationElement member = members[m];
                      if (id.equalsIgnoreCase(member.getAttribute("id")))
                        try {
                          obj = member.createExecutableExtension("class");
                        } catch(Exception x) { x.printStackTrace(); };
                    }
                  }
                }
                if (obj != null && obj instanceof IChartPlotter)
                {
                  setPlotterParameters((NodeList)item, (IChartPlotter)obj);
                  if (item.getAttributes().getNamedItem("name") != null)
                    ((IChartPlotter)obj).setName(item.getAttributes().getNamedItem("name").getNodeValue());
                  canvas.addPainter((IChartPlotter)obj);
                }
              }
            }
          }
        }
      } catch (Exception ex) {
        ex.printStackTrace();
      }

      int[] weights = new int[sectionHeights.size()];
      for (int i = 0; i < weights.length; i++)
        weights[i] = ((Integer)sectionHeights.elementAt(i)).intValue();
      form.setWeights(weights);
    }
    else
    {
      ChartCanvas canvas = new ChartCanvas(form);
      canvas.createContextMenu(this);
      canvas.addMouseListener(this);
      chart.addElement(canvas);
      canvas.addPainter(new PriceChart());
      
      canvas = new ChartCanvas(form);
      canvas.createContextMenu(this);
      chart.addElement(canvas);
      canvas.addPainter(new VolumeChart());

      int[] weights = { 85, 15 };
      form.setWeights(weights);
    }
  }
  
  private void setPlotterParameters(NodeList parent, IChartPlotter obj)
  {
    for (int i = 0; i < parent.getLength(); i++)
    {
      Node node = parent.item(i);
      if (node.getNodeName().equalsIgnoreCase("params") == true)
      {
        NamedNodeMap map = node.getAttributes();
        for (int ii = 0; ii < map.getLength(); ii++)
        {
          String name = map.item(ii).getNodeName();
          String value = map.item(ii).getNodeValue();
          if (name != null && value != null)
            obj.setParameter(name, value);
        }
      }
    }
  }

  /**
   * Save the chart's preference to an XML file.
   * <p></p>
   */
  public abstract void savePreferences();
  
  public void savePreferences(File folder)
  {
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document document = builder.getDOMImplementation().createDocument("", "preferences", null);

      Element element = document.createElement("settings");
      element.setAttribute("limit", String.valueOf(limitPeriod));
      document.getDocumentElement().appendChild(element);

      int[] weights = form.getWeights();
      for (int i = 0; i < weights.length; i++)
      {
        element = document.createElement("section");
        element.setAttribute("height", String.valueOf(weights[i]));
        ChartCanvas canvas = (ChartCanvas)chart.elementAt(i);
        for (int ii = 0; ii < canvas.getPainterCount(); ii++)
        {
//          if (canvas.getPainter(ii) instanceof PriceChart)
//            element.setAttribute("price", "true");
          if (canvas.getPainter(ii) instanceof VolumeChart)
            element.setAttribute("volume", "true");
        }
        document.getDocumentElement().appendChild(element);

        // Get all plotters for the given canvas
        for (int ii = 0; ii < canvas.getPainterCount(); ii++)
        {
          IChartPlotter painter = canvas.getPainter(ii);
          if (painter instanceof VolumeChart)
            continue;

          // Set the standard attributes
          Element node = document.createElement("chart");
          node.setAttribute("id", painter.getId());
          if (painter.getName() != null)
            node.setAttribute("name", painter.getName());
          
          // Append the parameters map
          HashMap params = painter.getParameters();
          Iterator keys = params.keySet().iterator();
          while (keys.hasNext())
          {
            String key = (String)keys.next();
            Element p = document.createElement("params");
            p.setAttribute(key, (String)params.get(key));
            node.appendChild(p);
          }
          
          element.appendChild(node);
        }
      }

      // XML transform
      File f = new File(folder, basicData.getSymbol().toLowerCase() + ".prefs");
      Transformer transformer = TransformerFactory.newInstance().newTransformer();
      transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty("{http\u003a//xml.apache.org/xslt}indent-amount", "4");
      DOMSource source = new DOMSource(document);
      BufferedWriter out = new BufferedWriter(new FileWriter(f));
      StreamResult result = new StreamResult(out);
      transformer.transform(source, result);
      out.flush();
      out.close();
    } catch (Exception ex) { ex.printStackTrace(); };
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#dispose()
   */
  public void dispose()
  {
    super.dispose();
  }

  /* (non-Javadoc)
   * @see org.eclipse.ui.IWorkbenchPart#setFocus()
   */
  public void setFocus()
  {
    container.setFocus();
  }

  public abstract void setData(final IBasicData d);
  public abstract IChartData[] getChartData(IBasicData data);

  public void setData(final IBasicData d, String stitle, String prefId)
  {
    basicData = d;
    reloadPreferences();

    String id = getViewSite().getSecondaryId();
    ViewsPlugin.getDefault().getPreferenceStore().setValue(prefId + id, basicData.getSymbol());
    setPartName(stitle);

    data = getChartData(basicData);
    if (data != null)
    {
      container.getDisplay().asyncExec(new Runnable() {
        public void run() {
          if (data != null && data.length > 0)
          {
            if (data[0].getMaxPrice() >= 10)
            {
              pf.setMinimumFractionDigits(2);
              pf.setMaximumFractionDigits(2);
              setScaleWidth(42);
            }
            else
            {
              pf.setMinimumFractionDigits(4);
              pf.setMaximumFractionDigits(4);
              setScaleWidth(50);
            }
          }
          else
            setScaleWidth(50);
          controlResized(null);
          bottombar.redraw();
          title.setText(basicData.getDescription());
          updateLabels();
        }
      });
      for (int i = 0; i < chart.size(); i++)
        ((ChartCanvas)chart.elementAt(i)).setData(data);
    }
  }
  
  public boolean isIndex(IBasicData d)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.indexProvider"); //$NON-NLS-1$
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        IConfigurationElement[] children = members[i].getChildren();
        for (int ii = 0; ii < children.length; ii++)
        {
          if (children[ii].getName().equalsIgnoreCase("category") == true) //$NON-NLS-1$
          {
            IConfigurationElement[] items = children[ii].getChildren();
            for (int iii = 0; iii < items.length; iii++)
            {
              String symbol = items[iii].getAttribute("symbol"); //$NON-NLS-1$
              if (d.getSymbol().equalsIgnoreCase(symbol) == true)
                return true;
            }
          }
          else if (children[ii].getName().equalsIgnoreCase("index") == true) //$NON-NLS-1$
          {
            String symbol = children[ii].getAttribute("symbol"); //$NON-NLS-1$
            if (d.getSymbol().equalsIgnoreCase(symbol) == true)
              return true;
          }
        }
      }
    }
    
    return false;
  }
  
  public IIndexDataProvider getIndexProvider(IBasicData d)
  {
    String EXTENSION_POINT_ID = "net.sourceforge.eclipsetrader.indexProvider";

    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.indexProvider"); //$NON-NLS-1$
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        IConfigurationElement[] children = members[i].getChildren();
        for (int ii = 0; ii < children.length; ii++)
        {
          if (children[ii].getName().equalsIgnoreCase("category") == true) //$NON-NLS-1$
          {
            IConfigurationElement[] items = children[ii].getChildren();
            for (int iii = 0; iii < items.length; iii++)
            {
              String symbol = items[iii].getAttribute("symbol"); //$NON-NLS-1$
              if (d.getSymbol().equalsIgnoreCase(symbol) == true)
              {
                IIndexDataProvider ip = (IIndexDataProvider)TraderPlugin.getExtensionInstance(EXTENSION_POINT_ID, members[i].getAttribute("id"));
                return ip;
              }
            }
          }
          else if (children[ii].getName().equalsIgnoreCase("index") == true) //$NON-NLS-1$
          {
            String symbol = children[ii].getAttribute("symbol"); //$NON-NLS-1$
            if (d.getSymbol().equalsIgnoreCase(symbol) == true)
            {
              IIndexDataProvider ip = (IIndexDataProvider)TraderPlugin.getExtensionInstance(EXTENSION_POINT_ID, members[i].getAttribute("id"));
              return ip;
            }
          }
        }
      }
    }
    
    return null;
  }

  public void updateIndexData(IBasicData d)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.indexProvider"); //$NON-NLS-1$
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int i = 0; i < members.length; i++)
      {
        IConfigurationElement[] children = members[i].getChildren();
        for (int ii = 0; ii < children.length; ii++)
        {
          if (children[ii].getName().equalsIgnoreCase("category") == true) //$NON-NLS-1$
          {
            IConfigurationElement[] items = children[ii].getChildren();
            for (int iii = 0; iii < items.length; iii++)
            {
              String symbol = items[iii].getAttribute("symbol"); //$NON-NLS-1$
              String ticker = items[iii].getAttribute("ticker"); //$NON-NLS-1$
              String label = items[iii].getAttribute("label"); //$NON-NLS-1$
              if (d.getSymbol().equalsIgnoreCase(symbol) == true)
              {
                if (ticker.length() != 0)
                  d.setTicker(ticker);
                d.setDescription(label);
                return;
              }
            }
          }
          else if (children[ii].getName().equalsIgnoreCase("index") == true) //$NON-NLS-1$
          {
            String symbol = children[ii].getAttribute("symbol"); //$NON-NLS-1$
            String ticker = children[ii].getAttribute("ticker"); //$NON-NLS-1$
            String label = children[ii].getAttribute("label"); //$NON-NLS-1$
            if (d.getSymbol().equalsIgnoreCase(symbol) == true)
            {
              if (ticker.length() != 0)
                d.setTicker(ticker);
              d.setDescription(label);
              return;
            }
          }
        }
      }
    }
  }
  
  public void updateView()
  {
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.elementAt(i)).setData(data);
  }
  
  public void addOscillator(String id)
  {
    IExtensionRegistry registry = Platform.getExtensionRegistry();
    IExtensionPoint extensionPoint = registry.getExtensionPoint("net.sourceforge.eclipsetrader.chartPlotter");
    if (extensionPoint != null)
    {
      IConfigurationElement[] members = extensionPoint.getConfigurationElements();
      for (int m = 0; m < members.length; m++)
      {
        IConfigurationElement member = members[m];
        if (id.equalsIgnoreCase(member.getAttribute("id")))
          try {
            Object obj = member.createExecutableExtension("class");
            if (obj instanceof IChartPlotter)
            {
              IChartPlotter chartPlotter = (IChartPlotter)obj;
              ChartParametersDialog pdlg = new ChartParametersDialog((IChartConfigurer)chartPlotter);
              if (pdlg.open() == ChartParametersDialog.OK)
              {
                if (pdlg.getPosition() == ChartParametersDialog.SELECTED_ZONE)
                  ((ChartCanvas)chart.elementAt(selectedZone)).addPainter(chartPlotter);
                else
                {
                  int[] w = form.getWeights();
                  ChartCanvas canvas = new ChartCanvas(form);
                  Control[] children = form.getChildren();
                  children[children.length - 1].moveBelow(children[0]);
                  canvas.createContextMenu(this);
                  chart.insertElementAt(canvas, 1);
                  canvas.addPainter(chartPlotter);
                  int[] weights = new int[chart.size()];
                  for (int i = 0; i < w.length; i++)
                    weights[i] = w[i];
                  weights[0] -= 150;
                  weights[weights.length - 1] = 150;
                  form.layout();
                }
                updateView();
                savePreferences();
              }
            }
          } catch(Exception x) { x.printStackTrace(); };
      }
    }
  }
  
  public void editOscillator()
  {
    ChartDialog dlg = new ChartDialog();
    dlg.setChart(chart);
    if (dlg.open() == ChartDialog.OK)
    {
      IChartPlotter chartPlotter = dlg.getObject();
      if (chartPlotter != null)
      {
        ChartParametersDialog pdlg = new ChartParametersDialog((IChartConfigurer)chartPlotter);
        if (pdlg.openEdit() == ChartParametersDialog.OK)
        {
          updateView();
          savePreferences();
        }
      }
    }
  }
  
  public void removeOscillator()
  {
    ChartDialog dlg = new ChartDialog();
    dlg.setChart(chart);
    if (dlg.open() == ChartDialog.OK)
    {
      IChartPlotter obj = dlg.getObject();
      if (obj != null)
      {
        for (int i = chart.size() - 1; i >= 0; i--)
        {
          ChartCanvas canvas = (ChartCanvas)chart.elementAt(i);
          for (int ii = canvas.getPainterCount() - 1; ii >= 0; ii--)
          {
            if (canvas.getPainter(ii) == obj)
            {
              canvas.removePainter(obj);
              if (canvas.getPainterCount() == 0)
              {
                canvas.removeMouseListener(this);
                canvas.dispose();
                chart.removeElementAt(i);
                form.layout();
              }
              break;
            }
          }
        }
        updateView();
        savePreferences();
      }
    }
  }
  
  public void setChartType(int type)
  {
    for (int i = chart.size() - 1; i >= 0; i--)
    {
      ChartCanvas canvas = (ChartCanvas)chart.elementAt(i);
      for (int ii = canvas.getPainterCount() - 1; ii >= 0; ii--)
      {
        if (canvas.getPainter(ii) instanceof PriceChart)
        {
          canvas.getPainter(ii).setParameter("type", String.valueOf(type));
          updateView();
          savePreferences();
          break;
        }
      }
    }
  }

  public void updateLabels()
  {
    updateLabels(data.length - 1);
  }
  
  public void updateLabels(int index)
  {
    if (data.length > 0)
    {
      date.setText(df.format(data[index].getDate()));
      closePrice.setText(pf.format(data[index].getClosePrice()));
      maxPrice.setText(pf.format(data[index].getMaxPrice()));
      minPrice.setText(pf.format(data[index].getMinPrice()));
      if (index >= 1)
      {
        double pc = data[index].getClosePrice() - data[index - 1].getClosePrice();
        pc = pc / data[index - 1].getClosePrice();
        variance.setText(pcf.format(pc * 100) + "%");
        if (pc > 0)
          variance.setForeground(positiveColor);
        else if (pc < 0)
          variance.setForeground(negativeColor);
        else
          variance.setForeground(textColor);
      }
      else
      {
        double pc = data[index].getClosePrice() - data[index].getOpenPrice();
        pc = pc / data[index].getOpenPrice();
        variance.setText(pcf.format(pc * 100) + "%");
        if (pc > 0)
          variance.setForeground(positiveColor);
        else if (pc < 0)
          variance.setForeground(negativeColor);
        else
          variance.setForeground(textColor);
      }
      volume.setText(nf.format(data[index].getVolume()));
    }
    else
    {
      date.setText("");
      closePrice.setText("");
      maxPrice.setText("");
      minPrice.setText("");
      variance.setText("");
      volume.setText("");
    }
    composite.layout();
  }
  
  protected void setWidth(int width)
  {
    this.width = width;
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.elementAt(i)).setColumnWidth(width);
  }
  
  protected void setMargin(int margin)
  {
    this.margin = margin;
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.elementAt(i)).setMargin(margin);
  }
  
  private void setScaleWidth(int scaleWidth)
  {
    this.scaleWidth = scaleWidth;
    for (int i = 0; i < chart.size(); i++)
      ((ChartCanvas)chart.elementAt(i)).setScaleWidth(scaleWidth);
  }

  /**
   * Method to return the limitPeriod field.<br>
   * @return Returns the limitPeriod.
   */
  public int getLimitPeriod()
  {
    return limitPeriod;
  }
  /**
   * Method to set the limitPeriod field.<br>
   * @param limitPeriod The limitPeriod to set.
   */
  public void setLimitPeriod(int limitPeriod)
  {
    this.limitPeriod = limitPeriod;
    savePreferences();
    setData(basicData);
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.PaintListener#paintControl(org.eclipse.swt.events.PaintEvent)
   */
  public void paintControl(PaintEvent e)
  {
    GC gc = e.gc;
    
    if (e.getSource() == bottombar)
    {
      gc.setForeground(textColor);
      gc.drawLine(0, 0, bottombar.getClientArea().width, 0); 

      if (data != null)
      {
        int month = -1;
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MMM");
        
        gc.setClipping(0, 0, bottombar.getClientArea().width - scaleWidth, bottombar.getClientArea().height);

        int x = margin + width / 2;
        if (container.getHorizontalBar().isVisible() == true)
          x -= container.getHorizontalBar().getSelection();
        for (int i = 0; i < data.length; i++, x += width)
        {
          c.setTime(data[i].getDate());
          if (c.get(Calendar.MONTH) != month)
          {
            String s = df.format(data[i].getDate());
            int x1 = x - gc.stringExtent(s).x / 2;
            gc.setForeground(textColor);
            if (c.get(Calendar.MONTH) == Calendar.JANUARY)
            {
              s += " " + c.get(Calendar.YEAR);
              gc.setForeground(yearColor);
            }
            gc.drawLine(x, 0, x, 5);
            gc.drawString(s, x1, 5);
            month = c.get(Calendar.MONTH);
          }
        }
      }
    }
    else
    {
      gc.setForeground(textColor);
      Composite c = (Composite)e.getSource();
      gc.drawLine(0, c.getClientArea().height - 1, c.getClientArea().width, c.getClientArea().height - 1); 
    }
  }
  
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ControlListener#controlMoved(org.eclipse.swt.events.ControlEvent)
   */
  public void controlMoved(ControlEvent e)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.ControlListener#controlResized(org.eclipse.swt.events.ControlEvent)
   */
  public void controlResized(ControlEvent e)
  {
    ScrollBar sb = container.getHorizontalBar();
    if (data != null)
    {
      if (sb != null && (data.length * width + margin * 2) > container.getClientArea().width - scaleWidth)
      {
        sb.setMaximum(data.length * width + margin * 2);
        sb.setMinimum(0);
        sb.setIncrement(6);
        sb.setPageIncrement(container.getClientArea().width - scaleWidth);
        sb.setThumb(container.getClientArea().width - scaleWidth);
        sb.setSelection(sb.getMaximum() - sb.getThumb());
        sb.setVisible(true);
        sb.setEnabled(true);
        for (int i = 0; i < chart.size(); i++)
          ((ChartCanvas)chart.elementAt(i)).setChartOrigin(0 - sb.getSelection());
      }
      else
      {
        sb.setVisible(false);
        for (int i = 0; i < chart.size(); i++)
          ((ChartCanvas)chart.elementAt(i)).setChartOrigin(0);
      }
    }
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
   */
  public void widgetDefaultSelected(SelectionEvent e)
  {
  }

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
   */
  public void widgetSelected(SelectionEvent e)
  {
    if (e.getSource() instanceof ScrollBar)
    {
      ScrollBar sb = (ScrollBar)e.getSource();
      for (int i = 0; i < chart.size(); i++)
        ((ChartCanvas)chart.elementAt(i)).setChartOrigin(0 - sb.getSelection());
      bottombar.redraw();
    }
  }

  private boolean mouseDown = false;
  private int mousePreviousX = -1;
  private GC mouseGC;

  /* (non-Javadoc)
   * @see org.eclipse.swt.events.MouseListener#mouseDoubleClick(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDoubleClick(MouseEvent e)
  {
  }
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.MouseListener#mouseDown(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseDown(MouseEvent e)
  {
    if (e.button == 1)
    {
      mouseDown = true;
      mouseGC = new GC((Canvas)e.getSource());
      mouseGC.setXORMode(true);
      mouseGC.setForeground(background);
      mouseMove(e);
    }
    // TODO: Hilight the selected zone with the red separator line.
    for (int i = 0; i < chart.size(); i++)
    {
      ChartCanvas canvas = (ChartCanvas)chart.elementAt(i);
      if (e.getSource() == canvas.getChart())
        selectedZone = i;
    }
  }
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.MouseListener#mouseUp(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseUp(MouseEvent e)
  {
    if (e.button == 1)
    {
      mouseDown = false;
      updateLabels();
      if (mousePreviousX != -1)
      {
        mouseGC.drawLine(mousePreviousX, 0, mousePreviousX, ((Canvas)e.getSource()).getClientArea().height);
        mousePreviousX = -1;
        mouseGC.dispose();
      }
    }
  }
  /* (non-Javadoc)
   * @see org.eclipse.swt.events.MouseMoveListener#mouseMove(org.eclipse.swt.events.MouseEvent)
   */
  public void mouseMove(MouseEvent e)
  {
    if (mouseDown == true)
    {
      int index = (e.x - margin) / width;
      if (index >= 0 && index < data.length)
      {
        updateLabels(index);
        if (mousePreviousX != -1)
          mouseGC.drawLine(mousePreviousX, 0, mousePreviousX, ((Canvas)e.getSource()).getClientArea().height);
        mouseGC.drawLine(e.x, 0, e.x, ((Canvas)e.getSource()).getClientArea().height);
        mousePreviousX = e.x;
      }
      else
      {
        updateLabels();
        mouseGC.drawLine(mousePreviousX, 0, mousePreviousX, ((Canvas)e.getSource()).getClientArea().height);
        mousePreviousX = -1;
      }
    }
  }
}
