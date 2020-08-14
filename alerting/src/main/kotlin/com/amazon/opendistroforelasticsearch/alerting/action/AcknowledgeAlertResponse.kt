package com.amazon.opendistroforelasticsearch.alerting.action

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
    var id: String
    var version: Long
    var seqNo: Long
    var primaryTerm: Long
    var status: RestStatus
    //var monitor: Monitor?
    var isSourceEmpty: Boolean
    var sourceAsBytesRef: BytesReference?

    constructor(
            id: String,
            version: Long,
            seqNo: Long,
            primaryTerm: Long,
            status: RestStatus,
            //monitor: Monitor?
            isSourceEmpty: Boolean,
            sourceAsBytesRef: BytesReference?
    ) : super() {
        this.id = id
        this.version = version
        this.seqNo = seqNo
        this.primaryTerm = primaryTerm
        this.status = status
        //this.monitor = monitor
        this.isSourceEmpty = isSourceEmpty
        this.sourceAsBytesRef = sourceAsBytesRef
    }

    @Throws(IOException::class)
    constructor(sin: StreamInput) : super() {
        this.id = sin.readString()
        this.version = sin.readLong()
        this.seqNo = sin.readLong()
        this.primaryTerm = sin.readLong()
        this.status = sin.readEnum(RestStatus::class.java)
        //this.monitor = Monitor.readFrom(sin)
        this.isSourceEmpty= sin.readBoolean()
        this.sourceAsBytesRef = sin.readBytesReference()
    }

    @Throws(IOException::class)
    override fun writeTo(out: StreamOutput) {
        out.writeString(id)
        out.writeLong(version)
        out.writeLong(seqNo)
        out.writeLong(primaryTerm)
        out.writeEnum(status)
        //monitor?.writeTo(out)
        out.writeBoolean(isSourceEmpty)
        sourceAsBytesRef?.writeTo(out)
    }

    @Throws(IOException::class)
    override fun toXContent(builder: XContentBuilder, params: ToXContent.Params): XContentBuilder {
        return builder.startObject()
                .field(_ID, id)
                .field(_VERSION, version)
                .field(_SEQ_NO, seqNo)
                .field(_PRIMARY_TERM, primaryTerm)
                //.field("monitor", monitor)
                .endObject()
    }
}