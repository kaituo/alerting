package com.amazon.opendistroforelasticsearch.alerting.action

import com.amazon.opendistroforelasticsearch.alerting.model.Alert
import com.amazon.opendistroforelasticsearch.alerting.util._ID
import com.amazon.opendistroforelasticsearch.alerting.util._PRIMARY_TERM
import com.amazon.opendistroforelasticsearch.alerting.util._SEQ_NO
import com.amazon.opendistroforelasticsearch.alerting.util._VERSION
import org.elasticsearch.action.ActionResponse
import org.elasticsearch.action.ActionType
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.common.bytes.BytesReference
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.rest.RestStatus
import java.io.IOException

class AcknowledgeAlertResponse : ActionResponse, ToXContentObject {

    val acknowledged: List<Alert>
    val failed: List<Alert>
    val missing: List<String>


    constructor(
            acknowledged: List<Alert>,
            failed: List<Alert>,
            missing: List<String>
    ) : super() {
        this.acknowledged = acknowledged
        this.failed = failed
        this.missing = missing
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : super() {
        this.acknowledged = mutableListOf()
        this.failed = mutableListOf()
        this.missing = mutableListOf()
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {

    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {

        builder.startObject().startArray("success")
        acknowledged.forEach { builder.value(it.id) }
        builder.endArray().startArray("failed")
        failed.forEach { buildFailedAlertAcknowledgeObject(builder, it) }
        missing.forEach { buildMissingAlertAcknowledgeObject(builder, it) }
        return builder.endArray().endObject()
    }

    private fun buildFailedAlertAcknowledgeObject(builder: XContentBuilder, failedAlert: Alert) {
        builder.startObject()
                .startObject(failedAlert.id)
        val reason = when (failedAlert.state) {
            Alert.State.ERROR -> "Alert is in an error state and can not be acknowledged."
            Alert.State.COMPLETED -> "Alert has already completed and can not be acknowledged."
            Alert.State.ACKNOWLEDGED -> "Alert has already been acknowledged."
            else -> "Alert state unknown and can not be acknowledged"
        }
        builder.field("failed_reason", reason)
                .endObject()
                .endObject()
    }

    private fun buildMissingAlertAcknowledgeObject(builder: XContentBuilder, alertID: String) {
        builder.startObject()
                .startObject(alertID)
                .field("failed_reason", "Alert: $alertID does not exist (it may have already completed).")
                .endObject()
                .endObject()
    }
}