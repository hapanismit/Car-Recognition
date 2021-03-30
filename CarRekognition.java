package com.amazonaws.samples;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.AmazonRekognitionException;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.DetectLabelsResult;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.amazonaws.services.rekognition.model.S3Object;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.SendMessageRequest;

import java.util.List;

public class CarRekognition {
	public static void main(String[] args) throws Exception {
		
		String myQueue = "https://sqs.us-east-1.amazonaws.com/212776196278/Smit_Queue.fifo";
		String bucketName = "njit-cs-643";
		AmazonS3 s3Client = AmazonS3ClientBuilder.standard()
	    		.withRegion("us-east-1")
                .build();
		
		ListObjectsV2Request req = new ListObjectsV2Request().withBucketName(bucketName).withMaxKeys(10);
        ListObjectsV2Result result;
        result = s3Client.listObjectsV2(req);
        
        for (S3ObjectSummary objectSummary : result.getObjectSummaries()) {
            //System.out.printf(" - %s (size: %d)\n", objectSummary.getKey(), objectSummary.getSize());
            String imageName = objectSummary.getKey().toString();
            
            AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.standard()
    	    		.withRegion("us-east-1")
    	    		.build();
            
            DetectLabelsRequest request = new DetectLabelsRequest()
       	         .withImage(new Image()
       	         .withS3Object(new S3Object()
       	         .withName(imageName).withBucket(bucketName)))
       	         .withMaxLabels(10)
       	         .withMinConfidence(90F);
            
            AmazonSQS sqs = AmazonSQSClientBuilder.standard()
            		.withRegion("us-east-1")
    	    		.build();
            

		    try {
		       DetectLabelsResult result1 = rekognitionClient.detectLabels(request);
		       List <Label> labels = result1.getLabels();
	
		       for (Label label: labels) {
		    	   if (label.getName().equals("Car")) {
		    		   System.out.println("Sending a " +imageName+ " to SQS ");
		    		   SendMessageRequest send_msg_request = new SendMessageRequest()
		    			        .withQueueUrl(myQueue)
		    			        .withMessageBody(imageName)
		    			        .withMessageGroupId("group-1");
		    		   sqs.sendMessage(send_msg_request);
		    	   }
		       }
		    } catch(AmazonRekognitionException e) {
		       e.printStackTrace();
		    }
        }//End of For Loop
	 }// End of Main
}// End of class
