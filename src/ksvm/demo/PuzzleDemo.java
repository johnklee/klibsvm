package ksvm.demo;

import java.io.File;

import ksvm.data.BasicRDIter;
import ksvm.run.SVMPredict;
import ksvm.run.SVMTrain;

public class PuzzleDemo {

	/**
	 * BD: Refer to "Lecture 01 - The Learning Problem (April 3, 2012)" 
	 *     http://www.youtube.com/watch?v=VeKeFIepJBU&feature=relmfu
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		File trainFile = new File("puzzle_train.txt");  			// Training file
		File predictFile = new File("puzzle_test.txt");	 			// Testing file
		File rstFile = new File("puzzle_test.pdi");					// File to hold output result
		BasicRDIter basicRDIter = new BasicRDIter(trainFile);		// 1) Prepare training input data iter
		SVMTrain train = new SVMTrain(basicRDIter);					// 2) Prepare SVMTrain object
		train.showCoeff = true;
		//train.cross_validation=5;
		
		if(train.start())											// 3) Start training
		{
			System.out.printf("\t[Info] Training is done!\n");
			SVMPredict predict = new SVMPredict(train);				// 4) Prepare SVMPredict object; Pass in trained model or SVMTrain object.
			predict.start(new BasicRDIter(predictFile), rstFile);	// 5) Start predicting and output result to <prediFile>
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
