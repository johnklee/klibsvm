package ksvm.run;

import gays.tools.ArguConfig;
import gays.tools.ArguParser;
import gays.tools.enums.EArguQuantity;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import libsvm.svm_parameter;

public class Main {

	/**
	 * BD : Suggest below flow to use SVM.
	 * 		1. Transform data to the format of an SVM package
     *		2. Conduct simple scaling on the data
	 *		3. Consider the RBF kernel.
	 *		4. Use cross-validation to nd the best parameter C and 
	 *		5. Use the best parameter C and to train the whole training set5
	 *		6. Test
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		File model = null;
		File input = null;
		List<File> inputs = null;
		File output = null;
		HashMap<String,Object> arguDef = new HashMap<String,Object>();
		arguDef.put("-t,--TYPE", new ArguConfig("Task type. Support 'train/predict'. Default is <train>.", EArguQuantity.SINGLE));
		arguDef.put("-s,--SVM_TYPE", new ArguConfig("svm_type : set type of SVM (default 0). 0=C-SVC; 1=nu-SVC; 2=one-class SVM; 3=epsilon-SVR; 4=nu-SVR.", 
				                                    EArguQuantity.SINGLE,
				                                    String.valueOf(svm_parameter.C_SVC)));
		arguDef.put("-i,--INPUT", new ArguConfig("Input file. When in train model, this argument is for corpus file; in predict model, this argument is for test file(s). When multiple files is given, separated them with ':'.", EArguQuantity.SINGLE));
		arguDef.put("-k,--KERNEL_TYPE", new ArguConfig("Kernel type. 0=linear; 1=polynomial; 2=radial basis; 3=sigmoid; 4=precomputed kernel(0~4 default=2)", 
				                                       EArguQuantity.SINGLE,
				                                       String.valueOf(svm_parameter.RBF)));
		arguDef.put("-o,--OUTPUT", new ArguConfig("Output file.", EArguQuantity.SINGLE));
		arguDef.put("-m,--MODEL", new ArguConfig("In training->Output model path; In prediction->Load in model path.", EArguQuantity.SINGLE));
		arguDef.put("-c,--CROSS_VALIDATION", new ArguConfig("Only for training. Signal for doing cross validation.", EArguQuantity.SIGN));
		arguDef.put("-n,--NR_FOLDER", new ArguConfig("nr_fold for cross validation.", EArguQuantity.SINGLE));
		arguDef.put("-p,--SHOW_PREDICT_PROBABILITY", new ArguConfig("Show predict probability.", EArguQuantity.SIGN));
		arguDef.put("-a,--SHOW_ANSWER", new ArguConfig("Output answer with prediction.", EArguQuantity.SIGN));		
		arguDef.put("--COST", new ArguConfig("Cost : set the parameter C of C-SVC, epsilon-SVR, and nu-SVR (default 1)", EArguQuantity.SINGLE));
		ArguParser aPsr = new ArguParser(arguDef, args);
		
		if(args.length==0)
		{
			aPsr.showArgus();
			return;
		}
		
		
		if (aPsr.isSet("-m")) 
		{
			model = new File(aPsr.getArguValue("-m"));
		} 
		else 
		{
			System.out.printf("\t[Warn] Argument -m is required!\n");
			aPsr.showArgus();
			return;
		}
		if (aPsr.isSet("-i")) 
		{
			//System.out.printf("\t[Test] Input=%s...\n", aPsr.getArguValue("-i"));
			String inputFiles[] = aPsr.getArguValue("-i").split(":");
			if(inputFiles.length==1)
			{
				input = new File(inputFiles[0]);
				if (!input.exists()) {
					System.out.printf("\t[Error] Input file=%s doesn't exist!\n",
							input.getAbsolutePath());
					return;
				}
			}
			else
			{
				inputs = new ArrayList<File>();
				for(String filePath:inputFiles)
				{
					input = new File(filePath);
					if (!input.exists()) {
						System.out.printf("\t[Error] Input file=%s doesn't exist!\n",
								input.getAbsolutePath());
						return;
					}
					else inputs.add(input);
				}
				input = null;
			}
		} 
		else 
		{
			System.out.printf("\t[Warn] Argument -i is required!\n");
			aPsr.showArgus();
			return;
		}
		
		
		String type = aPsr.getArguValue("-t");
		if(type!=null && type.equalsIgnoreCase("PREDICT"))
		{
			// Prediction
			if (aPsr.isSet("-o")) 
			{
				output = new File(aPsr.getArguValue("-o"));				
			} 
			else 
			{
				System.out.printf("\t[Warn] Argument -o is required!\n");
				aPsr.showArgus();
				return;
			}
			
			SVMPredict predict = new SVMPredict(model);
			if(aPsr.isSet("-p")) predict.predict_probability = 1;
			if(aPsr.isSet("-a")) predict.showAnswer = true;			
			if(input!=null)
			{
				System.out.printf("\t[Info] Start predicting...\n");
				if(predict.start(input, output))
				{
					//System.out.printf("Done(%s)!\n", TimeStr.toStringFrom(st));
				}
				else
				{
					System.out.printf("\t[Error] Fail in prediction!\n");
				}
			}
			else
			{
				System.out.printf("\t[Info] Start predicting (%d)...\n", inputs.size());
				if(predict.start(inputs, output))
				{
					//System.out.printf("Done(%s)!\n", TimeStr.toStringFrom(st));
				}
				else
				{
					System.out.printf("\t[Error] Fail in prediction!\n");
				}
			}
		}
		else
		{			
			// Training
			SVMTrain svmTrain = new SVMTrain(aPsr.getArguIntValue("-k"), aPsr.getArguIntValue("-s"));			
			
			if(aPsr.isSet("-c"))
			{
				svmTrain.cross_validation = 1;
				if(aPsr.isSet("-n"))
				{
					svmTrain.nr_fold = Integer.valueOf(aPsr.getArguValue("-n"));
				}
			}				
			
			if(aPsr.isSet("--COST"))
			{
				svmTrain.param.C = Integer.valueOf(aPsr.getArguValue("--COST"));
			}
			
			long st = System.currentTimeMillis();
			System.out.printf("\t[Info] Start training...\n");
			if(svmTrain.start(input))
			{
				
			}
			else
			{
				System.err.printf("\t[Error] Fail in training!");
				return;
			}
			svmTrain.saveModel(model);
			System.out.printf("\t[Info] Save model to %s...Done!\n", model.getAbsolutePath());
		}
	}
}
