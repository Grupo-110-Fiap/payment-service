resource "aws_sns_topic" "order_payment_topic" {
  name = "order-payment-topic"
}

resource "aws_sqs_queue" "order_payment_queue" {
  name                       = "order-payment-queue"
  message_retention_seconds  = 86400
  visibility_timeout_seconds = 30
}

resource "aws_sns_topic_subscription" "notify_order_sqs" {
  topic_arn            = aws_sns_topic.order_payment_topic.arn
  protocol             = "sqs"
  endpoint             = aws_sqs_queue.order_payment_queue.arn
  raw_message_delivery = true
}

resource "aws_sqs_queue_policy" "sns_write_sqs_policy" {
  queue_url = aws_sqs_queue.order_payment_queue.id
  policy    = data.aws_iam_policy_document.sns_sqs_policy.json
}

data "aws_iam_policy_document" "sns_sqs_policy" {
  statement {
    sid    = "Allow-SNS-SendMessage"
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["sns.amazonaws.com"]
    }

    actions   = ["sqs:SendMessage"]
    resources = [aws_sqs_queue.order_payment_queue.arn]

    condition {
      test     = "ArnEquals"
      variable = "aws:SourceArn"
      values   = [aws_sns_topic.order_payment_topic.arn]
    }
  }
}

resource "aws_ssm_parameter" "sns_topic_arn" {
  name        = "/payment-service/sns-topic-arn"
  description = "ARN do topico SNS de pagamentos"
  type        = "String"
  value       = aws_sns_topic.order_payment_topic.arn
}