package com.amazon.opendistroforelasticsearch.alerting.action

import com.amazon.opendistroforelasticsearch.alerting.model.Monitor
import org.elasticsearch.action.ActionRequest
import org.elasticsearch.action.ActionRequestValidationException
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.unit.TimeValue
import java.io.IOException

class ExecuteMonitorRequest : ActionRequest {
    val dryrun: Boolean
    val requestEnd: TimeValue
    val monitorId: String?
    val monitor: Monitor?

    constructor(
        dryrun: Boolean,
        requestEnd: TimeValue,
        monitorId: String?,
        monitor: Monitor?
    ) : super() {
        this.dryrun = dryrun
        this.requestEnd = requestEnd
        this.monitorId = monitorId
        this.monitor = monitor
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : super() {
        dryrun = sin.readBoolean()
        requestEnd = sin.readTimeValue()
        monitorId = sin.readOptionalString()
        monitor = Monitor.readFrom(sin)
    }

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeBoolean(dryrun)
        out.writeTimeValue(requestEnd)
        out.writeOptionalString(monitorId)
        monitor?.writeTo(out)
    }
}
