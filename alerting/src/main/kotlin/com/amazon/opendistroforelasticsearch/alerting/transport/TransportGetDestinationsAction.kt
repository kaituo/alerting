package com.amazon.opendistroforelasticsearch.alerting.transport

import com.amazon.opendistroforelasticsearch.alerting.action.GetDestinationsAction
import com.amazon.opendistroforelasticsearch.alerting.action.GetDestinationsRequest
import com.amazon.opendistroforelasticsearch.alerting.action.GetDestinationsResponse
import com.amazon.opendistroforelasticsearch.alerting.core.model.ScheduledJob
import com.amazon.opendistroforelasticsearch.alerting.elasticapi.string
import com.amazon.opendistroforelasticsearch.alerting.model.destination.Destination
import com.google.gson.JsonParser
import org.apache.logging.log4j.LogManager
import org.elasticsearch.action.ActionListener
import org.elasticsearch.action.search.SearchRequest
import org.elasticsearch.action.search.SearchResponse
import org.elasticsearch.action.support.ActionFilters
import org.elasticsearch.action.support.HandledTransportAction
import org.elasticsearch.client.Client
import org.elasticsearch.common.Strings
import org.elasticsearch.common.inject.Inject
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler
import org.elasticsearch.common.xcontent.NamedXContentRegistry
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParserUtils
import org.elasticsearch.common.xcontent.XContentType
import org.elasticsearch.index.query.QueryBuilders
import org.elasticsearch.rest.RestStatus
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.search.fetch.subphase.FetchSourceContext
import org.elasticsearch.tasks.Task
import org.elasticsearch.transport.TransportService

private val log = LogManager.getLogger(TransportGetDestinationsAction::class.java)

class TransportGetDestinationsAction @Inject constructor(
    transportService: TransportService,
    val client: Client,
    actionFilters: ActionFilters,
    val xContentRegistry: NamedXContentRegistry
) : HandledTransportAction<GetDestinationsRequest, GetDestinationsResponse> (
        GetDestinationsAction.NAME, transportService, actionFilters, ::GetDestinationsRequest
) {

    override fun doExecute(
        task: Task,
        getDestinationsRequest: GetDestinationsRequest,
        actionListener: ActionListener<GetDestinationsResponse>
    ) {
        val ctx = client.threadPool().threadContext.stashContext()
        try {
            val searchSourceBuilder = SearchSourceBuilder()
            searchSourceBuilder.fetchSource(FetchSourceContext(true, Strings.EMPTY_ARRAY, Strings.EMPTY_ARRAY))
            if (getDestinationsRequest.destinationId.isNullOrBlank()) {
                searchSourceBuilder
                        .query(QueryBuilders.boolQuery()
                                .must(QueryBuilders.existsQuery("destination"))
                        )
                        .seqNoAndPrimaryTerm(true)
                        .version(true)
            } else {
                searchSourceBuilder
                        .query(QueryBuilders.boolQuery()
                                .filter(QueryBuilders.termQuery("_id", getDestinationsRequest.destinationId))
                                .must(QueryBuilders.existsQuery("destination"))
                        )
                        .seqNoAndPrimaryTerm(true)
                        .version(true)
            }
            val searchRequest = SearchRequest()
                    .source(searchSourceBuilder)
                    .indices(ScheduledJob.SCHEDULED_JOBS_INDEX)
            client.search(searchRequest, object : ActionListener<SearchResponse> {
                override fun onResponse(response: SearchResponse) {
                    var builder = XContentFactory.jsonBuilder()
                    builder = response.toXContent(builder, ToXContent.EMPTY_PARAMS)
                    val jObject = JsonParser.parseString(builder.string()).asJsonObject
                    val hits = jObject.getAsJsonObject("hits")
                    val count = hits.getAsJsonObject("total").get("value").asInt
                    val hitArr = hits.getAsJsonArray("hits")
                    var destinations = mutableListOf<Destination>()
                    for (i in 0 until count) {
                        val dest = hitArr.get(i).asJsonObject
                        val id = dest.get("_id").asString
                        val version = dest.get("_version").asLong
                        val seqNo = dest.get("_seq_no").asInt
                        val primaryTerm = dest.get("_primary_term").asInt
                        val curDest = dest.getAsJsonObject("_source").toString()
                        var xcp = XContentFactory.xContent(XContentType.JSON)
                                .createParser(xContentRegistry, LoggingDeprecationHandler.INSTANCE, curDest)
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp::getTokenLocation)
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.FIELD_NAME, xcp.nextToken(), xcp::getTokenLocation)
                        XContentParserUtils.ensureExpectedToken(XContentParser.Token.START_OBJECT, xcp.nextToken(), xcp::getTokenLocation)
                        val d = Destination.parse(xcp, id, version, seqNo, primaryTerm)
                        destinations.add(d)
                    }

                    if (getDestinationsRequest.sortString.equals("name")) {
                        destinations.sortBy { it.name }
                    } else {
                        destinations.sortBy { it.type }
                    }

                    if (getDestinationsRequest.sortOrder.equals("desc")) {
                        destinations.reverse()
                    }

                    actionListener.onResponse(GetDestinationsResponse(RestStatus.OK, destinations))
                }

                override fun onFailure(t: Exception) {
                    log.error("fail to get destinations", t)
                    actionListener.onFailure(t)
                }
            })
        } finally {
            ctx.close()
        }
    }

}