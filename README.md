# ColumbiaSpring2022ELEN6889YOLOAWSKinesis

1. to create stream
    1.1 install Gstreamer and create a live steam based on your own system
    https://github.com/awslabs/amazon-kinesis-video-streams-producer-sdk-cpp
    https://docs.aws.amazon.com/kinesisvideostreams/latest/dg/gs-send-data.html

    1.2 create video stream 
    aws kinesisvideo create-stream --stream-name yy --data-retention-in-hours 1
    1.3 Create a data stream
    aws kinesis create-stream --stream-name FacialRecognition --shard-count 1
    1.4 upload the video stream
gst-launch-1.0 -v avfvideosrc ! videoconvert ! vtenc_h264_hw allow-frame-reordering=FALSE realtime=TRUE max-keyframe-interval=45 ! kvssink name=sink stream-name="streamname" access-key="your access key"  secret-key="your secret-key"

2. To create a rekognition processor
    2.1 create collection
    aws rekognition create-collection   --collection-id "facesNew"

    2.2 upload face pictures to collection
    aws rekognition index-faces \
      --image '{"S3Object":{"Bucket":"faces6889","Name":”cr1"}}' \
      --collection-id "collection-id" \
      --max-faces 1 \
      --quality-filter "AUTO" \
      --detection-attributes "ALL" \
      --external-image-id "cr-cr1.jpg" 
    2.3 to create IAM role and add policy
     create an IAM service role.

Add policy:
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "kinesis:PutRecord",
                "kinesis:PutRecords"
            ],
            "Resource": "data-arn"
        },
        {
            "Effect": "Allow",
            "Action": [
                "kinesisvideo:GetDataEndpoint",
                "kinesisvideo:GetMedia"
            ],
            "Resource": "video-arn"
        }
    ]
}
2.4 create rekognition processor
aws rekognition create-stream-processor --cli-input-json file://processor.json
Json file:
# processor.json
{
   "Name": "FacialRecognitionStreamProcessor",
   "Input": {
    "KinesisVideoStream": {
       "Arn": "<Kinesis Video Stream ARN>"
    }
   },
   "Output": {
    "KinesisDataStream": {
       "Arn": "<Kinesis Data Stream ARN>"
    }
   },
   "RoleArn": "<AWS Role ARN>",
   "Settings": {
    "FaceSearch": {
       "CollectionId": "faces",
       "FaceMatchThreshold": 85.5
    }
   }
}
2.5 check the created processor:
aws rekognition list-stream-processors
2.6 to start/ stop processor:
aws rekognition start-stream-processor --name FacialRecognitionStreamProcessor
aws rekognition stop-stream-processor --name FacialRecognitionStreamProcessor

3.how the read the result from the rekognition stream
SHARD_ITERATOR=$(aws kinesis get-shard-iterator --shard-id shardId-000000000000 --shard-iterator-type TRIM_HORIZON --stream-name FacialRecognition --query 'ShardIterator')
                        aws kinesis get-records --shard-iterator $SHARD_ITERATOR


3.How to start this facial recognition application
1 open the video and create a live stream 2. start the rekognition processor 3 check whether the processor is under a running state. 4. Read the data from kinesis data stream 5. Close the processor 6. Close the live stream
