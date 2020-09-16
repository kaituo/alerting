package com.amazon.opendistroforelasticsearch.alerting.util


import org.apache.logging.log4j.LogManager
import org.elasticsearch.ElasticsearchException
import org.elasticsearch.ElasticsearchSecurityException
import org.elasticsearch.ElasticsearchStatusException
import org.elasticsearch.common.Strings
import org.elasticsearch.index.IndexNotFoundException
import org.elasticsearch.index.engine.VersionConflictEngineException
import org.elasticsearch.rest.RestStatus

private val log = LogManager.getLogger(AlertingError::class.java)

/**
 * Converts into a user friendly message.
 *
 */
class AlertingError(message: String, status: RestStatus, ex: Exception) : ElasticsearchStatusException(message, status, ex) {


    companion object {
        //todo: more msgs needs to be added.
        @JvmStatic
        fun wrap(ex: Exception): AlertingError {
            log.error("Alerting error: $ex")

            var msg = "Unknown error"
            var status = RestStatus.INTERNAL_SERVER_ERROR
            if ( ex is IndexNotFoundException) {
                msg = "Configured monitored indices are not found: [${ex.index} ]"
                status = ex.status()
            } else if ( ex is ElasticsearchSecurityException) {
                msg = "User doesn't have permissions to execute this action. Contact administrator"
                status = ex.status()
            } else if ( ex is ElasticsearchStatusException) {
                msg = ex.message as String
                status = ex.status()
            } else if ( ex is IllegalArgumentException) {
                msg = ex.message as String
                status = RestStatus.BAD_REQUEST
            } else if ( ex is VersionConflictEngineException) {
                msg = ex.message as String
                status = ex.status()
            } else {
                if (!Strings.isNullOrEmpty(ex.message)) {
                    msg = ex.message as String
                }
            }
            return AlertingError(msg, status, ex)
        }
    }


}


