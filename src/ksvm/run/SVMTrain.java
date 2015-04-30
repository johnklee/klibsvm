package ksvm.run;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import flib.util.TimeStr;

import ksvm.data.BasicRDIter;
import ksvm.data.IRecordIter;
import ksvm.data.Record;
import ksvm.data.TData;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class SVMTrain{
	public svm_parameter 	param = null;		// set by parse_command_line
	public svm_problem 		prob = null;		// set by read_problem
	public svm_model 		model = null;
	public int 				cross_validation=0;
	public int 				nr_fold=5;
	public IRecordIter 		rdIter = null;
	public List<String>		errMsg = new LinkedList<String>();
	public boolean			showCoeff = false;
	
	public SVMTrain()
	{
		this(svm_parameter.RBF, svm_parameter.C_SVC);
	}
	
	public SVMTrain(int kernel_type) {this(kernel_type, svm_parameter.C_SVC);}
	
	public SVMTrain(int kernel_type, int svm_type)
	{		
		param = new svm_parameter();
		// default values
		param.svm_type = svm_type;
		param.kernel_type = kernel_type;
		param.degree = 3;
		param.gamma = 0;	// 1/num_features
		param.coef0 = 0;
		param.nu = 0.5;
		param.cache_size = 100;
		param.C = 1;
		param.eps = 1e-3;
		param.p = 0.1;
		param.shrinking = 1;
		param.probability = 0;
		param.nr_weight = 0;
		param.weight_label = new int[0];
		param.weight = new double[0];
		cross_validation = 0;
	}
	public SVMTrain(IRecordIter iter){this(); this.rdIter = iter;}
	public SVMTrain(File modelFile)throws IOException{this(); this.loadModel(modelFile);}
	
	private void do_cross_validation()
	{
		int i;
		int total_correct = 0;
		double total_error = 0;
		double sumv = 0, sumy = 0, sumvv = 0, sumyy = 0, sumvy = 0;
		double[] target = new double[prob.l];

		svm.svm_cross_validation(prob,param,nr_fold,target);
		if(param.svm_type == svm_parameter.EPSILON_SVR ||
		   param.svm_type == svm_parameter.NU_SVR)
		{
			for(i=0;i<prob.l;i++)
			{
				double y = prob.y[i];
				double v = target[i];
				total_error += (v-y)*(v-y);
				sumv += v;
				sumy += y;
				sumvv += v*v;
				sumyy += y*y;
				sumvy += v*y;
			}
			System.out.print("Cross Validation Mean squared error = "+total_error/prob.l+"\n");
			System.out.print("Cross Validation Squared correlation coefficient = "+
				((prob.l*sumvy-sumv*sumy)*(prob.l*sumvy-sumv*sumy))/
				((prob.l*sumvv-sumv*sumv)*(prob.l*sumyy-sumy*sumy))+"\n"
				);
		}
		else
		{
			for(i=0;i<prob.l;i++)
				if(target[i] == prob.y[i])
					++total_correct;
			System.out.print("Cross Validation Accuracy = "+100.0*total_correct/prob.l+"%\n");
		}
	}
	
	protected int _readTrainData(Vector<Double> vy, Vector<svm_node[]> vx)
	{
		if(rdIter==null) 
		{
			errMsg.add("Record Iterator is null!");
			return -1;
		}		
		int max_index = 0;
		Record rd = null;
		while(rdIter.hasNext())
		{
			rd = rdIter.next();
			//System.out.printf("\t[Test] Readin: Label=%f...\n", rd.label);
			vy.addElement(rd.label);
			svm_node[] x = new svm_node[rd.features.size()];
			TData data = null;
			int i = 0;
			while(!rd.features.isEmpty())
			{
				data = rd.features.poll();
				x[i] = new svm_node();
				x[i].index = data.index;
				x[i].value = data.value;				
				i++;
			}
			vx.addElement(x);
			if(data!=null) max_index = Math.max(max_index, data.index);
		}
		return max_index;
	}
	
	protected void _prepreProblem(Vector<Double> vy, Vector<svm_node[]> vx, int max_index)
	{
		prob = new svm_problem();
		prob.l = vy.size();
		prob.x = new svm_node[prob.l][];
		for(int i=0;i<prob.l;i++)
			prob.x[i] = vx.elementAt(i);
		prob.y = new double[prob.l];
		for(int i=0;i<prob.l;i++)
			prob.y[i] = vy.elementAt(i);

		if(param.gamma == 0 && max_index > 0) param.gamma = 1.0/max_index;

		if(param.kernel_type == svm_parameter.PRECOMPUTED)
			for(int i=0;i<prob.l;i++)
			{
				if (prob.x[i][0].index != 0)
				{
					System.err.print("Wrong kernel matrix: first column must be 0:sample_serial_number\n");
					errMsg.add("Wrong kernel matrix: first column must be 0:sample_serial_number");
					System.exit(1);
				}
				if ((int)prob.x[i][0].value <= 0 || (int)prob.x[i][0].value > max_index)
				{
					System.err.print("Wrong input format: sample_serial_number out of range\n");
					errMsg.add("Wrong input format: sample_serial_number out of range");
					System.exit(1);
				}
			}
	}
	
	public boolean start(File input) throws IOException
	{
		rdIter = new BasicRDIter(input);
		return start();
	}
	
	public boolean start()
	{
		Vector<Double> vy = new Vector<Double>();
		Vector<svm_node[]> vx = new Vector<svm_node[]>();
		errMsg.clear();
		int max_index = _readTrainData(vy, vx);	// Read training data
		_prepreProblem(vy, vx, max_index);
		
		if(errMsg.size()==0)
		{
			long st = System.currentTimeMillis();
			model = svm.svm_train(prob,param);	
			if(cross_validation != 0)
			{
				do_cross_validation();
			}
			if(showCoeff)
			{
				System.out.printf("\t[Info] Total %d class coeff:\n", model.sv_coef.length);
				for(int i=0; i<model.sv_coef.length; i++)
				{
					System.out.printf("\tSV-%d (%d): ", i, model.sv_coef[i].length);
					for(int j=0; j<model.sv_coef[i].length; j++) System.out.printf("%.02f ", model.sv_coef[i][j]);
					System.out.println();
				}
			}			
			System.out.printf("\t[Info] Training time=%s\n", TimeStr.ToStringFrom(st));
			return true;
		}
		return false;
	}
	
	public void loadModel(File modelFile)throws IOException {loadModel(modelFile.getAbsolutePath());}
	public void loadModel(String modelPath)throws IOException {model = svm.svm_load_model(modelPath);}
	public void saveModel(File out_file) throws IOException {saveModel(out_file.getAbsolutePath());}
	public void saveModel(String out_file_name) throws IOException
	{
		svm.svm_save_model(out_file_name,model);
	}
}
