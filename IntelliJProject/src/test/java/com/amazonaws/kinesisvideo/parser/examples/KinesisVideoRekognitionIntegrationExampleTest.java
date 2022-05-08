/*
Copyright 2017-2017 Amazon.com, Inc. or its affiliates. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License"). 
You may not use this file except in compliance with the License. 
A copy of the License is located at

   http://aws.amazon.com/apache2.0/

or in the "license" file accompanying this file. 
This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
*/
package com.amazonaws.kinesisvideo.parser.examples;

import java.io.IOException;

import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.kinesisvideo.parser.TestResourceUtil;
import com.amazonaws.kinesisvideo.parser.rekognition.pojo.RekognitionInput;
import com.amazonaws.regions.Regions;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This examples demonstrates how to integrate KVS with Rekognition and draw bounding boxes for each frame.
 */
public class KinesisVideoRekognitionIntegrationExampleTest {
    /* long running test */

    @Test
    public void testExample() throws InterruptedException, IOException {
        // NOTE: Rekogntion Input needs ARN for both Kinesis Video Streams and Kinesis Data Streams.
        // For more info please refer https://docs.aws.amazon.com/rekognition/latest/dg/streaming-video.html
        RekognitionInput rekognitionInput = RekognitionInput.builder()
                .kinesisVideoStreamArn("arn:aws:kinesisvideo:us-west-2:801709213937:stream/yy")
                .kinesisDataStreamArn("arn:aws:kinesis:us-west-2:801709213937:stream/FacialRecognitionNew")
                .streamingProcessorName("FacialRecognitionStreamProcessor")
                // Refer how to add face collection :
                // https://docs.aws.amazon.com/rekognition/latest/dg/add-faces-to-collection-procedure.html
                .faceCollectionId("faces")
                .iamRoleArn("arn:aws:iam::801709213937:role/AmazonRekognitionDetect")
                .matchThreshold(0.08f)
                .build();

        KinesisVideoRekognitionIntegrationExample example = KinesisVideoRekognitionIntegrationExample.builder()
                .region(Regions.US_WEST_2)
                .kvsStreamName("yy")
                .kdsStreamName("FacialRecognitionNew")
                .rekognitionInput(rekognitionInput)
                .credentialsProvider(new ProfileCredentialsProvider())
                // NOTE: If the input stream is not passed then the example assumes that the video fragments are
                // ingested using other mechanisms like GStreamer sample app or AmazonKinesisVideoDemoApp
                //.inputStream(TestResourceUtil.getTestInputStream("bezos_vogels.mkv"))
                .build();

        // The test file resolution is 1280p.
        example.setWidth(1280);
        example.setHeight(720);

        // This test might render frames with high latency until the rekognition results are returned. Change below
        // timeout to smaller value if the frames need to be rendered with low latency when rekognition results
        // are not present.
        example.setRekognitionMaxTimeoutInMillis(100);
        example.execute(240L);
    }
}