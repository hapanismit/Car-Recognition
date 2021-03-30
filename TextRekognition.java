package com.amazonaws.samples;

import java.util.List;

import java.io.File;  // Import the File class
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.FileWriter;
import java.io.BufferedWriter;


import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.SendMessageBatchRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectTextRequest;
import com.amazonaws.services.rekognition.model.DetectTextResult;
import com.amazonaws.services.rekognition.model.TextDetection;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.S3Object;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

public class TextRekognition {
	public static void main(String[] args) throws Exception {

		try {
			File f1 = new File("output.txt");
			if(f1.exists()) {
	            f1.delete();
	         }
			f1.createNewFile();
		}catch (IOException e) {
		      System.out.println("An error occurred.");
		      e.printStackTrace();
		    }	
		
		
		String bucketName = "njit-cs-643";
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
	    		.withRegion("us-east-1")
                .build();
		
		AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
	    		.withRegion("us-east-1")
	    		.build();
		
		String myQueue = "https://sqs.us-east-1.amazonaws.com/212776196278/Smit_Queue.fifo";
		AmazonSQS sqs = AmazonSQSClientBuilder.standard()
        		.withRegion("us-east-1")
	    		.build();
		
		ReceiveMessageRequest re_request = new ReceiveMessageRequest().withQueueUrl(myQueue)
				.withMaxNumberOfMessages(10)
				.withVisibilityTimeout(30);
		
		List<Message> messages = sqs.receiveMessage(re_request).getMessages();
		
		//System.out.println(messages.size());
		for (Message m : messages) {
			String imageName = m.getBody().toString();
			
			DetectTextRequest request = new DetectTextRequest()
	       	         .withImage(new Image()
	       	         .withS3Object(new S3Object()
	       	         .withName(imageName).withBucket(bucketName)));
			
			try {
		         DetectTextResult result = rekognitionClient.detectText(request);
		      
		         List<TextDetection> textDetections = result.getTextDetections();

		         
		         for (TextDetection text: textDetections) {
		        	 String textOnCar = text.getDetectedText();
		             System.out.println("Detected text of " +imageName+" : "+ textOnCar);
		             System.out.println();
		             
		             FileWriter fileWritter = new FileWriter("output.txt",true);
		             BufferedWriter bw = new BufferedWriter(fileWritter);
		             bw.write(imageName+" Text : "+textOnCar);
		             bw.newLine();
		             bw.close();
		             
		             System.out.println("Successfully wrote to the file.");
		             break;
		         }
		      } catch(AmazonRekognitionException e) {
		         e.printStackTrace();
		      }	catch(Exception e) {
		    	 e.printStackTrace();
		      }
		}	// End of for loop	
	}	// End of main 	
}   // End of class
