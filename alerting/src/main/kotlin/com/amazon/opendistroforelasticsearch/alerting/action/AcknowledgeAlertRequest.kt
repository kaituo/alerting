package com.amazon.opendistroforelasticsearch.alerting.action

import org.elasticsearch.action.ActionRequest
import org.elasticsearch.action.ActionRequestValidationException
import org.elasticsearch.action.ActionType
import org.elasticsearch.action.delete.DeleteResponse
import org.elasticsearch.action.support.WriteRequest
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import java.io.IOException

class AcknowledgeAlertRequest : ActionRequest {
    val monitorId: String
    val refreshPolicy: WriteRequest.RefreshPolicy
    val alertIds: List<String>
    var xContentRegistry: NamedXContentRegistry? = null

    constructor(
            monitorId: String,
            alertIds: List<String>,
            refreshPolicy: WriteRequest.RefreshPolicy,
            xContentRegistry: NamedXContentRegistry
    ) : super() {
        this.monitorId = monitorId
        this.alertIds = alertIds
        this.refreshPolicy = refreshPolicy
        this.xContentRegistry = xContentRegistry
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : super() {
        monitorId = sin.readString()
        alertIds = sin.readStringList()
        refreshPolicy = WriteRequest.RefreshPolicy.readFrom(sin)
    }

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(monitorId)
        out.writeStringCollection(alertIds)
        refreshPolicy.writeTo(out)
    }
}
