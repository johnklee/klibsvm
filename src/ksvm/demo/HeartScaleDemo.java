package ksvm.demo;

import java.io.File;
import java.io.IOException;

import ksvm.data.BasicRDIter;
import ksvm.run.SVMPredict;
import ksvm.run.SVMTrain;

public class HeartScaleDemo {
	public static void main(String[] args) throws IOException{
		File trainFile = new File("heart_scale");
		File prediFile = new File("heart_scale.pdi");
		BasicRDIter basicRDIter = new BasicRDIter(trainFile);		// 1) Prepare training input data iter
		SVMTrain train = new SVMTrain(basicRDIter);					// 2) Prepare SVMTrain object
		
		if(train.start())											// 3) Start training
		{
			System.out.printf("\t[Info] Training is done!\n");
			SVMPredict predict = new SVMPredict(train);				// 4) Prepare SVMPredict object; Pass in trained model or SVMTrain object.
			predict.showAnswer = true;
			predict.start(new BasicRDIter(trainFile), prediFile);	// 5) Start predicting and output result to <prediFile>
			System.out.printf("\t[Info] Testing is done!\n");
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
}
