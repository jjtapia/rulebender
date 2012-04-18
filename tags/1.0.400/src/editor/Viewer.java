package editor;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.MessageBox;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.experimental.chart.swt.ChartComposite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class Viewer {

	private Shell viewer = null;
	private Composite composite1 = null;
	private Button button1 = null;
	private Button button2 = null;
	private Composite composite2 = null;
	private List filelist = null;
	private List specieslist = null;
	private ChartComposite chartpanel;
	private Text textarea;
	
	ArrayList<Integer> selection= new ArrayList<Integer>();
	ArrayList<FileData> filesdata = new ArrayList<FileData>();  //  @jve:decl-index=0:
	String filepath;
	String[] dats, nets;
	private int currentfile;
	private int charttype = 1;
	private Label label1 = null;
	private Label label2 = null;
	private BufferedReader br;
	
	Viewer(String fpath, String[] files1, String[] files2) throws IOException
	{
		filepath = fpath;
		dats = files1;
		nets = files2;
		if(dats.length == 0 && nets.length == 0)
		{
			MessageBox mb = new MessageBox(BNGEditor.getMainEditorShell(), SWT.ICON_INFORMATION);
			mb.setMessage("No results files to be shown.");
			mb.setText("Error Info");
			mb.open();
			return;
		}
		for(int i = 0; i<dats.length; i++)
		{
			filesdata.add(new FileData());
			ProcessData(i);
		}	
		Init();
	}
	
	private void Init() {
		viewer = new Shell(BNGEditor.getMainEditorShell(), SWT.SHELL_TRIM);
		viewer.setBounds(new Rectangle(23, 23, 800, 600));
		viewer.setText("Viewer");
		viewer.setLayout(null);
		viewer.addControlListener(new ControlAdapter() {
			public void controlResized(ControlEvent e) {
				composite1.setBounds(new Rectangle(1, viewer.getBounds().height - 80, 198, 35));
				button1.setBounds(new Rectangle(5, 5, 90, 25));
				button2.setBounds(new Rectangle(102, 5, 90, 25));
				composite2.setBounds(new Rectangle(201, 5, viewer.getBounds().width-222, viewer.getBounds().height-50));
				filelist.setBounds(new Rectangle(5, 25, 190, (int) Math.round((viewer.getBounds().height - 80)*0.35-20)));
				specieslist.setBounds(new Rectangle(5, (int) Math.round((viewer.getBounds().height - 80)*0.35+32), 190, (int) Math.round((viewer.getBounds().height - 80)*0.65-45)));
				label1.setBounds(5, 5, 100, 17);
				label2.setBounds(5, (int) Math.round((viewer.getBounds().height - 80)*0.35+12), 100, 17);
			}
		});
		composite1 = new Composite(viewer, SWT.NONE);
		composite1.setLayout(null);
		composite1.setBounds(new Rectangle(1, viewer.getBounds().height - 80, 198, 35));
		button1 = new Button(composite1, SWT.NONE);
		button1.setFont(BNGEditor.getGlobalfont());
		button1.setBounds(new Rectangle(5, 5, 90, 25));
		button1.setText("Scatter Chart");
		button1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(charttype == 1)
				{
					charttype = 2;
					button1.setText("Line Chart");
					chartpanel.dispose();
					chartpanel = new ChartComposite(composite2,SWT.NONE,Plot(selection, charttype));
					chartpanel.pack();
					composite2.layout();
					composite2.update();
				}
				else
				{
					charttype = 1;
					button1.setText("Scatter Chart");
					chartpanel.dispose();
					chartpanel = new ChartComposite(composite2,SWT.NONE,Plot(selection, charttype));
					chartpanel.pack();
					composite2.layout();
					composite2.update();
				}
			}
		});
		button2 = new Button(composite1, SWT.NONE);
		button2.setFont(BNGEditor.getGlobalfont());
		button2.setBounds(new Rectangle(102, 5, 90, 25));
		button2.setText("Close");
		button2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				viewer.dispose();
			}
		});
		composite2 = new Composite(viewer, SWT.BORDER);
		composite2.setLayout(new FillLayout());
		composite2.setBounds(new Rectangle(201, 5, viewer.getBounds().width-222, viewer.getBounds().height-50));
		composite2.setSize(10000, 10000);
		filelist = new List(viewer, SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL);
		filelist.setBounds(new Rectangle(5, 25, 190, (int) Math.round((viewer.getBounds().height - 80)*0.35-20)));
		filelist.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent arg0) {		
				currentfile = filelist.getSelectionIndex();
				if(chartpanel!=null)
					chartpanel.dispose();
				if(textarea!=null)
					textarea.dispose();
				if(currentfile<dats.length)
				{
					button1.setEnabled(true);
					specieslist.setEnabled(true);
					specieslist.removeAll();
					selection.clear();
					selection.add(0);
					if(!dats[currentfile].endsWith("cdat"))
						label2.setText("Observables");
					else
						label2.setText("Species");
					for(int i=0; i<filesdata.get(currentfile).name.size(); i++)
						specieslist.add(filesdata.get(currentfile).name.get(i));
					chartpanel = new ChartComposite(composite2,SWT.NONE,Plot(selection, charttype));
					chartpanel.pack();
					composite2.layout();
					composite2.update();
					specieslist.setSelection(0);
				}
				else
				{
					label2.setText("");
					textarea = new Text(composite2,SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
					textarea.setText("");
					try {
						br = new BufferedReader(new FileReader(new File(filepath, nets[currentfile-dats.length])));
						try {
							while(br.ready())
							{
								textarea.append(br.readLine()+textarea.getLineDelimiter());
							}
						} catch (IOException e1) {}
					} catch (FileNotFoundException e1) {}
					textarea.setEditable(false);
					textarea.pack();
					composite2.layout();
					composite2.update();
					button1.setEnabled(false);
					specieslist.removeAll();
					specieslist.setEnabled(false);
				}
			}
		});
		specieslist = new List(viewer, SWT.BORDER|SWT.MULTI|SWT.V_SCROLL);
		specieslist.setBounds(new Rectangle(5, (int) Math.round((viewer.getBounds().height - 80)*0.35+32), 190, (int) Math.round((viewer.getBounds().height - 80)*0.65-45)));
		label1 = new Label(viewer, SWT.NONE);
		label1.setText("Result Files");
		label2 = new Label(viewer, SWT.NONE);
		label1.setBounds(5, 5, 100, 17);
		label2.setBounds(5, (int) Math.round((viewer.getBounds().height - 80)*0.35+12), 100, 17);
		
		for(int i=0;i<dats.length;i++)
			filelist.add(dats[i]);
		for(int i=0;i<nets.length;i++)
			filelist.add(nets[i]);
		currentfile=0;
		if(dats.length!=0)
			while(!dats[currentfile].endsWith("gdat")&&currentfile<dats.length-1)
				currentfile++;
		filelist.setSelection(currentfile);
		if(currentfile<dats.length)
		{
			if(!dats[currentfile].endsWith("cdat"))
				label2.setText("Observables");
			else
				label2.setText("Species");
			selection.add(0);
			for(int i=0; i<filesdata.get(currentfile).name.size(); i++)
				specieslist.add(filesdata.get(currentfile).name.get(i));
			chartpanel = new ChartComposite(composite2,SWT.NONE,Plot(selection, charttype));
			chartpanel.pack();
			composite2.layout();
			composite2.update();
			specieslist.setSelection(0);
		}
		else
		{
			label2.setText("");
			textarea = new Text(composite2,SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
			textarea.setText("");
			try {
				br = new BufferedReader(new FileReader(new File(filepath, nets[currentfile-dats.length])));
				try {
					while(br.ready())
					{
						textarea.append(br.readLine()+textarea.getLineDelimiter());
					}
				} catch (IOException e1) {}
			} catch (FileNotFoundException e1) {}
			textarea.setEditable(false);
			textarea.pack();
			composite2.layout();
			composite2.update();
			button1.setEnabled(false);
			specieslist.removeAll();
			specieslist.setEnabled(false);
		}
		specieslist.addMouseListener(new MouseAdapter(){
			public void mouseUp(MouseEvent arg0) {
				chartpanel.dispose();
				selection.clear();
				int[] sele = specieslist.getSelectionIndices();
				for(int i=0;i<sele.length;i++)
					selection.add(sele[i]);
				chartpanel = new ChartComposite(composite2,SWT.NONE, Plot(selection,charttype));
				chartpanel.pack();
				composite2.layout();
				composite2.update();
			}
		});
		specieslist.addKeyListener(new KeyAdapter(){
			public void keyReleased(KeyEvent arg0) {
				chartpanel.dispose();
				selection.clear();
				int[] sele = specieslist.getSelectionIndices();
				for(int i=0;i<sele.length;i++)
					selection.add(sele[i]);
				chartpanel = new ChartComposite(composite2,SWT.NONE, Plot(selection,charttype));
				chartpanel.pack();
				composite2.layout();
				composite2.update();
			}
		});
		
		viewer.open();
	}
	
	void ProcessData(int in) throws IOException
	{
		filesdata.get(in).name.clear();//species names
		filesdata.get(in).xaxis.clear();//time column
		filesdata.get(in).values.clear();//all the values for construction of series
		filesdata.get(in).series.clear();//xyseries
		File tfile = new File(filepath, dats[in]);
		BufferedReader br = new BufferedReader(new FileReader(tfile));
		String str;
		int j;
		str = br.readLine();
		str = str.substring(str.indexOf('#')+1,str.length()).trim();
		filesdata.get(in).nameforxaxis = str.substring(0, str.indexOf(' ')).trim();
		str = str.substring(str.indexOf(' '), str.length()).trim();
		XYSeries xytemp;
		Value vtemp;
		while(str.indexOf(' ')!=-1)//names
		{
			filesdata.get(in).name.add(str.substring(0, str.indexOf(' ')).trim());
			str=str.substring(str.indexOf(' '), str.length()).trim();
		}
		filesdata.get(in).name.add(str);
		
		for(int i=0;i<filesdata.get(in).name.size();i++)
			filesdata.get(in).values.add(new Value());
		
		int count =0;
		while (br.ready())
		{
			count++;
			str = br.readLine().trim();
			if(!str.equals(""))
			{
				filesdata.get(in).xaxis.add(ProcessNum(str.substring(0,str.indexOf(' ')).trim()));
				str = str.substring(str.indexOf(' '),str.length()).trim();
				j=0;
				while(str.indexOf(' ')!=-1)
				{
					if(j==filesdata.get(in).values.size())
					{
						filesdata.get(in).values.add(new Value());
						filesdata.get(in).name.add("New Species");
						for(int k=0;k<=count;k++)
							filesdata.get(in).values.get(j).value.add((float)0.0);
					}
					filesdata.get(in).values.get(j).value.add(ProcessNum(str.substring(0, str.indexOf(' ')).trim()));
					str=str.substring(str.indexOf(' '), str.length()).trim();
					j++;
				}
				if(j==filesdata.get(in).values.size())
				{
					filesdata.get(in).values.add(new Value());
					filesdata.get(in).name.add("New Species");
					for(int k=0;k<=count;k++)
						filesdata.get(in).values.get(j).value.add((float)0.0);
				}
				filesdata.get(in).values.get(j).value.add(ProcessNum(str));
			}
		}

		for(int i=0; i<filesdata.get(in).name.size();i++)
		{
			vtemp = filesdata.get(in).values.get(i);
			xytemp = new XYSeries(filesdata.get(in).name.get(i));
			for(int k=0;k<filesdata.get(in).xaxis.size();k++)
				xytemp.add(filesdata.get(in).xaxis.get(k), vtemp.value.get(k));
			filesdata.get(in).series.add(xytemp);
		}
	}
	
	Float ProcessNum(String in)
	{
		Double base = Double.parseDouble(in.substring(0, in.indexOf('e')).trim());
		int pow = Integer.parseInt(in.substring(in.indexOf('e')+2, in.length()).trim());
		char chr = in.charAt(in.indexOf('e')+1);
		Double temp;
		if(chr=='+')
			temp = base*Math.pow(10, pow);
		else
			temp = base*Math.pow(10, 0-pow);
		return Float.valueOf(temp.toString())  ;
	}
	
	JFreeChart Plot(ArrayList<Integer> in, int charttype)//in: selection
	{
		XYSeriesCollection seriesCollection = new XYSeriesCollection();
		for(int i=0;i<in.size();i++)
			seriesCollection.addSeries(filesdata.get(currentfile).series.get(in.get(i)));
		String tempstr = dats[currentfile];
		if(charttype == 1)
			return ChartFactory.createXYLineChart(tempstr, filesdata.get(currentfile).nameforxaxis,"Concentration", seriesCollection, PlotOrientation.VERTICAL, true,false, false);
		else
			return ChartFactory.createScatterPlot(tempstr, filesdata.get(currentfile).nameforxaxis,"Concentration", seriesCollection, PlotOrientation.VERTICAL, true,false, false);	
	}
	
	class Value
	{
		ArrayList<Float> value = new ArrayList<Float>();
	}
	
	class FileData
	{
		ArrayList<String> name = new ArrayList<String>();
		ArrayList<Float> xaxis = new ArrayList<Float>();
		ArrayList<Value> values = new ArrayList<Value>();
		ArrayList<XYSeries> series= new ArrayList<XYSeries>(); 
		String nameforxaxis;
	}
}
