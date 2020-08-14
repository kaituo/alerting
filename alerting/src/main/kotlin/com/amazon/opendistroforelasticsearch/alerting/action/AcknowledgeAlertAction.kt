package com.amazon.opendistroforelasticsearch.alerting.action

import org.elasticsearch.action.ActionType
import org.elasticsearch.action.delete.DeleteResponse

class AcknowledgeAlertAction private constructor() : ActionType<AcknowledgeAlertResponse>(NAME, ::AcknowledgeAlertResponse) {
    companion object {
        val INSTANCE = AcknowledgeAlertAction()
        val NAME = "cluster:admin/alerting/alerts/ack"
    }
}