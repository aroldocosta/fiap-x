#!/bin/sh

set -eu

awslocal sqs create-queue --queue-name video-uploaded-queue
awslocal sqs create-queue --queue-name video-status-queue
awslocal sqs create-queue --queue-name video-notification-queue
