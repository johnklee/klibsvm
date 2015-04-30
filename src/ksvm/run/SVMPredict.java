package ksvm.run;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import flib.util.TimeStr;

import ksvm.data.BasicRDIter;
import ksvm.data.IRecordIter;
import ksvm.data.MultiFRDIter;
import ksvm.data.Record;
import ksvm.data.TData;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;

public class SVMPredict {
	public int 			predict_probability = 0;
	public svm_model 	model = null;
	public boolean 		showAnswer = false;
	
	public SVMPredict(){}
	public SVMPredict(SVMTrain train){model = train.model;}
	public SVMPredict(svm_model model){this.model = model;}
	public SVMPredict(String modelPath) throws IOException
	{
		System.out.printf("\t[Test] Loading model from %s...", modelPath);
		model = svm.svm_load_model(modelPath);
		System.out.println("Done!");
	}
	public SVMPredict(File modelFile) throws IOException
	{
		this(modelFile.getAbsolutePath());
	}
	
	public double predict(Record rd)
	{
		svm_node[] x = new svm_node[rd.features.size()];
		int i=0;
		TData feature = null;
		List<TData> popout = new ArrayList<TData>();
		while(!rd.features.isEmpty())
		{
			feature = rd.features.poll();
			x[i] = new svm_node();
			x[i].index = feature.index;
			x[i].value = feature.value;
			i++;
			popout.add(feature);
		}
		rd.features.addAll(popout);
		
		int svm_type=svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);
		double[] prob_estimates=null;
		if(predict_probability == 1)
		{
			if(svm_type == svm_parameter.EPSILON_SVR ||
			   svm_type == svm_parameter.NU_SVR)
			{
				System.out.print("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
			}
			else
			{
				int[] labels=new int[nr_class];
				svm.svm_get_labels(model,labels);
				prob_estimates = new double[nr_class];				
			}
		}
				
		double v;
		if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
		{
			v = svm.svm_predict_probability(model, x, prob_estimates);						
			//ps.println();
		}
		else
		{
			v = svm.svm_predict(model, x);			
		}
		return v;
	}
	
	public void predict(IRecordIter rdIter, OutputStream output, boolean withAnswer) throws IOException
	{
		PrintStream ps = new PrintStream(output);		
		int correct = 0;
		int fp=0, fn=0, tp=0, tn=0;		
		int total = 0;
		double error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
		long st = System.currentTimeMillis();

		int svm_type=svm.svm_get_svm_type(model);
		int nr_class=svm.svm_get_nr_class(model);
		double[] prob_estimates=null;

		if(predict_probability == 1)
		{
			if(svm_type == svm_parameter.EPSILON_SVR ||
			   svm_type == svm_parameter.NU_SVR)
			{
				System.out.print("Prob. model for test data: target value = predicted value + z,\nz: Laplace distribution e^(-|z|/sigma)/(2sigma),sigma="+svm.svm_get_svr_probability(model)+"\n");
			}
			else
			{
				int[] labels=new int[nr_class];
				svm.svm_get_labels(model,labels);
				prob_estimates = new double[nr_class];
				ps.printf("labels");
				for(int j=0;j<nr_class;j++) ps.printf("\t%d", labels[j]);
				ps.println();
			}
		}
		else
		{
			ps.printf("# Prediction/Answer\r\n");
		}
		
		Record rd = null;
		while(rdIter.hasNext())
		{
			rd = rdIter.next();			

			double target = rd.label;			
			svm_node[] x = new svm_node[rd.features.size()];
			int i=0;
			TData feature = null;
			while(!rd.features.isEmpty())
			{
				feature = rd.features.poll();
				x[i] = new svm_node();
				x[i].index = feature.index;
				x[i].value = feature.value;
				i++;
			}

			double v;
			if (predict_probability==1 && (svm_type==svm_parameter.C_SVC || svm_type==svm_parameter.NU_SVC))
			{
				v = svm.svm_predict_probability(model, x, prob_estimates);
				ps.printf("%.02f\t", v);
				for(int j=0;j<nr_class;j++) System.out.printf("%.02f\t", prob_estimates[j]);
				//ps.println();
			}
			else
			{
				v = svm.svm_predict(model, x);
				ps.printf("%.02f", v);
			}
			
			if(withAnswer) ps.printf("/%.02f", target);
			ps.println();

			if(v == target) 
			{
				++correct;
				if(v==1) tp++;
				else tn++;
			}
			else
			{
				if(v==1) fp++;
				else fn++;
			}
			error += (v-target)*(v-target);
			sumv += v;
			sumy += target;
			sumvv += v*v;
			sumyy += target*target;
			sumvy += v*target;
			++total;
		}
		if(svm_type == svm_parameter.EPSILON_SVR ||
		   svm_type == svm_parameter.NU_SVR)
		{
			ps.print("Mean squared error = "+error/total+" (regression)\n");
			ps.print("Squared correlation coefficient = "+
				 ((total*sumvy-sumv*sumy)*(total*sumvy-sumv*sumy))/
				 ((total*sumvv-sumv*sumv)*(total*sumyy-sumy*sumy))+
				 " (regression)\n");
		}
		else
		{			
			System.out.printf("\t[Info] Spending time=%s!\n", TimeStr.ToStringFrom(st));
			System.out.printf("\t[Info] TP=%d; TN=%d; FP=%d; FN=%d\n", tp, tn, fp, fn);
			System.out.printf("\t[Info] Accuracy = %.02f%%(%d/%d) (classification)\n", (double)correct/total*100, correct, total);			
		}
			
	}
	
	public boolean start(IRecordIter rdIter, OutputStream os, boolean withAnswer) throws IOException
	{
		if(model!=null && rdIter!=null)
		{
			if(predict_probability == 1)
			{
				if(svm.svm_check_probability_model(model)==0)
				{
					System.err.print("Model does not support probabiliy estimates\n");
					return false;
				}
			}
			else
			{
				if(svm.svm_check_probability_model(model)!=0)
				{
					System.out.print("Model supports probability estimates, but disabled in prediction.\n");
				}
			}
			predict(rdIter, os, withAnswer);
			return true;
		}
		return false;
	}
	
	public boolean start(IRecordIter rdIter, boolean withAnswer) throws IOException
	{
		return start(rdIter, System.out, withAnswer);
	}
	
	/**
	 * BD : Start predicting the records from <rdIter> and output result to <outputRst>.
	 * @param rdIter : Record iterator for prediction.
	 * @param outputRst : File to hold prediction.
	 * @return True if success. Otherwise, False will be returned.
	 * @param withAnswer : True will output answer with prediction; Otherwise, only prediction will be output.
	 * @return True if success. Otherwise, False will be returned.
	 * @throws IOException
	 */
	public boolean start(IRecordIter rdIter, File outputRst, boolean withAnswer) throws IOException
	{
		return start(rdIter, new FileOutputStream(outputRst), withAnswer);
	}
	
	/**
	 * BD : Start predicting the records from <rdIter> and output result to <outputRst>.
	 * @param rdIter : Record iterator for prediction.
	 * @param outputRst : File to hold prediction.
	 * @return True if success. Otherwise, False will be returned.
	 * @throws IOException
	 */
	public boolean start(IRecordIter rdIter, File outputRst) throws IOException
	{
		return start(rdIter, new FileOutputStream(outputRst), showAnswer);
	}
	
	public boolean start(File input, File output) throws IOException
	{
		return start(new BasicRDIter(input), output);
	}
	
	public boolean start(List<File> inputs, File output) throws Exception
	{
		return start(new MultiFRDIter(inputs), output);
	}
}
