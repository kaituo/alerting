package com.amazon.opendistroforelasticsearch.alerting.action

import org.elasticsearch.action.ActionRequest
import org.elasticsearch.action.ActionRequestValidationException
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.rest.RestRequest
import org.elasticsearch.search.fetch.subphase.FetchSourceContext
import java.io.IOException

class GetMonitorRequest : ActionRequest {
    val monitorId: String
    val version: Long
    val method: RestRequest.Method
    val srcContext: FetchSourceContext?

    constructor(
        monitorId: String,
        version: Long,
        method: RestRequest.Method,
        srcContext: FetchSourceContext?
    ) : super() {
        this.monitorId = monitorId
        this.version = version
        this.srcContext = srcContext
        this.method = method
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : super() {
        this.monitorId = sin.readString()
        this.version = sin.readLong()
        this.srcContext = FetchSourceContext(sin)
        this.method = sin.readEnum(RestRequest.Method::class.java)
    }

    override fun validate(): ActionRequestValidationException? {
        return null
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(monitorId)
        out.writeLong(version)
        srcContext?.writeTo(out)
        out.writeEnum(method)
    }
}
