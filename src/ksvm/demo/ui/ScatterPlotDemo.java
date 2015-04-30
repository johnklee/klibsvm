package ksvm.demo.ui;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import ksvm.data.BasicRDIter;
import ksvm.data.Record;
import ksvm.data.TData;
import ksvm.run.SVMPredict;
import ksvm.run.SVMTrain;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

public class ScatterPlotDemo extends ApplicationFrame {
	public ScatterPlotDemo(File testFile, SVMPredict svmPredit) throws IOException
	{
		super(testFile.getName());
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries series1 = new XYSeries("Scatter1");
		XYSeries series2 = new XYSeries("Scatter2");
		XYSeries series4 = new XYSeries("Wrong");
		XYSeries series3 = new XYSeries("Equation");
		
		System.out.printf("\t[Test] Testfile=%s...\n", testFile.getName());
		BasicRDIter basicRDIter = new BasicRDIter(testFile);
		Record rc = null;
		double v = -1;
		double total = 0, error = 0;
		while(basicRDIter.hasNext())
		{
			total++;
			rc = basicRDIter.next();
			v = svmPredit.predict(rc);
			if(rc.features.size()==0 || rc.features.size()%2!=0)
			{
				System.out.printf("\t[Error] Feature size=%d!\n", rc.features.size());
				break;
			}
			TData x = rc.features.poll(); TData y = rc.features.poll();
			//System.out.printf("\t[Test] Label=%.01f: x=%.02f; y=%.02f...\n", rc.label, x.value, y.value);
			if(rc.label==2 && v==rc.label)
			{
				series2.add(x.value, y.value);				
			}
			else if(rc.label==1 && v==rc.label)
			{
				series1.add(x.value, y.value);				
			}
			else
			{
				if(rc.label==1)
				error++;
				series4.add(x.value, y.value);
			}
			series3.add(x.value, equationVal(x.value));
		}
		
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);
		dataset.addSeries(series4);

		JFreeChart chart = ChartFactory.createScatterPlot(String.format("Scatter Plot Demo: %.02f%% (%d/%d)", 
				                                                        100*(total-error)/total, 
				                                                        (int)(total-error), 
				                                                        (int)total),
				"X", "Y", dataset, PlotOrientation.VERTICAL, true, true, false);
		NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(false);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setVerticalAxisTrace(true);
		chartPanel.setHorizontalAxisTrace(true);
		// chartPanel.setVerticalZoom(true);
		// chartPanel.setHorizontalZoom(true);
		setContentPane(chartPanel);
	}
	
	public ScatterPlotDemo(File plotFile) throws IOException {
		super(plotFile.getName());
		XYSeriesCollection dataset = new XYSeriesCollection();
		XYSeries series1 = new XYSeries("Scatter1");
		XYSeries series2 = new XYSeries("Scatter2");
		XYSeries series3 = new XYSeries("Equation");
		
		BasicRDIter basicRDIter = new BasicRDIter(plotFile);
		Record rc = null;
		while(basicRDIter.hasNext())
		{
			rc = basicRDIter.next();
			TData x = rc.features.poll(); TData y = rc.features.poll();
			//System.out.printf("\t[Test] Label=%.01f: x=%.02f; y=%.02f...\n", rc.label, x.value, y.value);
			if(rc.label==2)
			{
				series2.add(x.value, y.value);
			}
			else
			{
				series1.add(x.value, y.value);
			}
			series3.add(x.value, equationVal(x.value));
		}
		
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);

		JFreeChart chart = ChartFactory.createScatterPlot("Scatter Plot Demo",
				"X", "Y", dataset, PlotOrientation.VERTICAL, true, true, false);
		NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
		domainAxis.setAutoRangeIncludesZero(false);
		ChartPanel chartPanel = new ChartPanel(chart);
		chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
		chartPanel.setVerticalAxisTrace(true);
		chartPanel.setHorizontalAxisTrace(true);
		// chartPanel.setVerticalZoom(true);
		// chartPanel.setHorizontalZoom(true);
		setContentPane(chartPanel);
	}
	
	public ScatterPlotDemo(String title) throws IOException{
		super(title);
		XYDataset data = createDataset();
        JFreeChart chart = ChartFactory.createScatterPlot(
            "Scatter Plot Demo",
            "X", "Y", 
            data, 
            PlotOrientation.VERTICAL,
            true, 
            true, 
            false
        );
        NumberAxis domainAxis = (NumberAxis) chart.getXYPlot().getDomainAxis();
        domainAxis.setAutoRangeIncludesZero(false);
        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        chartPanel.setVerticalAxisTrace(true);
        chartPanel.setHorizontalAxisTrace(true);
        //chartPanel.setVerticalZoom(true);
        //chartPanel.setHorizontalZoom(true);
        setContentPane(chartPanel);
	}

	private static final long serialVersionUID = 1L;

	protected static double equationVal(double i) {return i*i*0.7-10;}
	
	private static XYDataset createDataset() throws IOException{
		File dataSetInFile = new File("scatters.tf");
		BufferedWriter bw = new BufferedWriter(new FileWriter(dataSetInFile));
		XYSeriesCollection dataset = new XYSeriesCollection();
		Random rdm = new Random();
		XYSeries series1 = new XYSeries("Scatter1");
		XYSeries series2 = new XYSeries("Scatter2");
		XYSeries series3 = new XYSeries("Equation");
		for (double i = -10; i <= 10; i+=0.1) {
			double fy = equationVal(i);
			for(int j=0; j<rdm.nextInt(3)+1; j++)
			{
				double ry = rdm.nextInt(100)-50;					
				if(ry<fy) 
				{					
					System.out.printf("\t[Test] x=%.02f, y=%f.02, fy=%.02f (2)...\n", i, ry, fy);
					bw.append(String.format("%d 1:%.03f 2:%.02f\r\n", 2, i, ry));
					series2.add(i, ry);
				}
				else 
				{
					System.out.printf("\t[Test] x=%.02f, y=%f.02, fy=%.02f (1)...\n", i, ry, fy);
					bw.append(String.format("%d 1:%.03f 2:%.02f\r\n", 1, i, ry));
					series1.add(i, ry);
				}				
			}
			series3.add(i, fy);
		}
		dataset.addSeries(series1);
		dataset.addSeries(series2);
		dataset.addSeries(series3);
		bw.close();
		return dataset;
    }
	
	public static void main(String[] args) throws IOException{
        ScatterPlotDemo demo = new ScatterPlotDemo(new File("scatters_train.tf"));
		//ScatterPlotDemo demo = new ScatterPlotDemo("test");
        demo.pack();
        demo.setVisible(true);
    }
}
