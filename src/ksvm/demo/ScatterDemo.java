package ksvm.demo;

import java.io.File;
import java.io.IOException;

import ksvm.data.BasicRDIter;
import ksvm.run.SVMPredict;
import ksvm.run.SVMTrain;

public class ScatterDemo {
	public static void main(String[] args) throws IOException{
		File data = new File("data");
		File trainFile = new File(data, "scatters_train.tf");
		File modelFile = new File(data, "scatters.model");
		File testFile = new File(data, "scatters_test.tf");
		
		
		// Training -> Generate Model
		//		1. Feed in training data <trainFile>
		// 		2. Output training model <modelFile>
		BasicRDIter basicRDIter = new BasicRDIter(trainFile);	// 1) Prepare training input data iter
		SVMTrain train = new SVMTrain(basicRDIter);				// 2) Prepare SVMTrain object
		//train.param.C = 10;
		if(train.start())										// 3) Start training
		{
			System.out.printf("\t[Info] Training is done!\n");
			train.saveModel(modelFile);
		}
		else
		{
			System.out.printf("\t[Info] Something wrong while training:\n");
			for(String em:train.errMsg)
			{
				System.out.printf("\t%s\n", em);
			}
			return;
		}
		
		// Predicting -> 
		// 		1. Loading mode <modelFile>
		//		2. Feed in testing data <testFile> and output prediction result <resultFile>.		
		File resultFile = new File("scatters_test.pid");
		SVMPredict svmPredict = new SVMPredict(modelFile);
		svmPredict.start(new BasicRDIter(testFile), resultFile);
		
		// Testing (2)
		/*File modelFile = new File("scatters.model");
		File testFile = new File("scatters_test.tf");
		SVMPredict svmPredict = new SVMPredict(modelFile);
		ScatterPlotDemo demo = new ScatterPlotDemo(testFile, svmPredict);
		demo.pack();
        demo.setVisible(true);*/
	}
}
