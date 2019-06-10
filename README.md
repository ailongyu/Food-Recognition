# Food-Recognition
## CNN-Model
This folder contains the code to train the model. The codes need run on jupyter notebook. <br/>
Important versions for required library are listed in the code file. <br/>
"foodCNN_mobilenetV2.ipynb" is the code to load data and train the CNN model. <br/>
"mobilenetV2_model_best.h5" is the trained model. <br/>
"labels.txt" are labels for training data.<br/>
"load_model.ipynb" is the code to test our trained model. <br/>
"read_image.ipynb" is the code to test if a image is crupted and it can also move images between folders.<br/>

# Android Application 
## deployed with TensorFlowlite model
The layout file in the res folder.<br/>
Three java classes including Mainactivity, TensorFlowclassifer and classifer in the main folder in app directory.<br/>
The trained CNN model deployed in the android app is in the mobilenetV2_model_best.tflite file which is in assets folder under main directory.<br/>
Labels.txt contains all the food the app can recognize which is in assets folder.<br/>
