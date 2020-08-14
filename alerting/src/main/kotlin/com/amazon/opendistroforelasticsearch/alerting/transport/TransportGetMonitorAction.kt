package com.amazon.opendistroforelasticsearch.alerting.transport

import com.amazon.opendistroforelasticsearch.alerting.action.GetMonitorAction
import com.amazon.opendistroforelasticsearch.alerting.action.GetMonitorRequest
import com.amazon.opendistroforelasticsearch.alerting.action.GetMonitorResponse
import com.amazon.opendistroforelasticsearch.alerting.core.model.ScheduledJob
import com.amazon.opendistroforelasticsearch.alerting.model.Monitor
import org.apache.logging.log4j.LogManager
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.get.GetRequest
import org.elasticsearch.action.get.GetResponse
import org.elasticsearch.action.support.ActionFilters
import org.elasticsearch.action.support.HandledTransportAction
import org.elasticsearch.client.Client
import org.elasticsearch.common.inject.Inject
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.XContentHelper
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.tasks.Task
import org.elasticsearch.transport.TransportService

private val log = LogManager.getLogger(TransportGetMonitorAction::class.java)

class TransportGetMonitorAction @Inject constructor(
        transportService: TransportService,
        val client: Client,
        actionFilters: ActionFilters
): HandledTransportAction<GetMonitorRequest, GetMonitorResponse> (
        GetMonitorAction.NAME, transportService, actionFilters, ::GetMonitorRequest) {

    override fun doExecute(task: Task, getRequest: GetMonitorRequest, actionListener: ActionListener<GetMonitorResponse>) {

        val getRequest = GetRequest(ScheduledJob.SCHEDULED_JOBS_INDEX, getRequest.monitorId)
                .version(getRequest.version)
                .fetchSourceContext(getRequest.srcContext)

        client.get(getRequest, object : ActionListener<GetResponse> {
            override fun onResponse(response: GetResponse) {
                if (!response.isExists) {
                    actionListener.onFailure(ElasticsearchStatusException("Monitor not found.", RestStatus.NOT_FOUND))
                }

                actionListener.onResponse(GetMonitorResponse(response.id, response.version,
                        response.seqNo, response.primaryTerm, RestStatus.OK, response.isSourceEmpty, response.sourceAsBytesRef))
            }

            override fun onFailure(t: Exception) {
                actionListener.onFailure(t)
            }
        })
    }
}
