klibsvm is a simple, easy-to-use wrapper of libsvm. 
For more detail or advanced usage of libsvm, please refer to below URL:
http://www.csie.ntu.edu.tw/~cjlin/libsvm

This wrapper pvovides you a simple way to leverage libsvm.jar and use it as a library.

Table of contents
==================
- Training process
- Predict process



Training process
==================
All related class for libsvm training/predict process can be found under packages : ksvm.run.*
For the training process, check below flow :
1. Prepare your training data. (ex. heart_scale)
2. Prepare your own record iterator to implement ksvm.data.IRecordIter. 
   You can refer to ksvm.data.BasicRDIter which support default libsvm training format:
   <Label> <item1>:<value1> <item2>:<value2> ¡K
   * Label : The class to predict. Ex. 1, 2¡K
   * itemN: The feature number. Ex. 1, 2¡K (Start from 1 not 0!)
   * valueN: The value of featureN
3. Initialize ksvm.run.SVMTrain object and pass your implemented  IRecordIter into the constructor.
4. Call SVMTrain.start() to start training.
5. Call SVMTrain.saveModel() to save the training model for future usage without re-training.

Predict process
==================
Once you have trained the model successfully, it is very easy for you to predict the class based on it. Please check below flow
1. Make sure you have ksvm.run.SVMTrain object which has already completed training or the external file as model being saved.
2. Create ksvm.run.SVMPredict object using below supported constructors:
   * public SVMPredict(SVMTrain train)  // Using trained ksvm.run.SVMTrain object
   * public SVMPredict(svm_model model) // Using libsvm.svm_model object
   * public SVMPredict(String modelPath) // Using external file as saved model.
3. Then you can call SVMPredict.start(IRecordIter rdIter, File outputRst) to start predicting.





