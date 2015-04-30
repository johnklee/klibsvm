package ksvm.demo;

import java.io.File;
import java.io.IOException;

import ksvm.data.BasicRDIter;
import ksvm.run.SVMPredict;
import ksvm.run.SVMTrain;

public class PhClassifier {
	SVMPredict svmPredict;
	
	public PhClassifier(File model) throws IOException
	{
		svmPredict = new SVMPredict(model);
	}
	
	public double predict(String content)
	{
		return 0;
	}
	
	public static void Predict() throws Exception {
		// Testing (1)
		File modelFile = new File("phClassifier.model");
		File testFile = new File("classifier_svm_test.data");
		File resultFile = new File("classifier_svm_test.data.txt");
		SVMPredict svmPredict = new SVMPredict(modelFile);
		svmPredict.start(new BasicRDIter(testFile), resultFile);
	}
	
	public static void Train() throws Exception
	{
		File trainFile = new File("classifier_svm_train.data");
		File modelFile = new File("phClassifier.model");
		BasicRDIter basicRDIter = new BasicRDIter(trainFile);	// 1) Prepare training input data iter
		SVMTrain train = new SVMTrain(basicRDIter);				// 2) Prepare SVMTrain object
		train.param.C = 10;
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
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		//Train();
		Predict();
	}
}
