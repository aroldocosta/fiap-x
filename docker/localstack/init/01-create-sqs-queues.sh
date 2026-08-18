#!/bin/sh

set -eu

awslocal sqs create-queue --queue-name video-uploaded-queue
awslocal sqs create-queue --queue-name video-status-api-queue
awslocal sqs create-queue --queue-name video-status-notification-queue
