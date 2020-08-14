package com.amazon.opendistroforelasticsearch.alerting.action

import org.elasticsearch.action.ActionType

class GetMonitorAction private constructor() : ActionType<GetMonitorResponse>(NAME, ::GetMonitorResponse) {
    companion object {
        val INSTANCE = GetMonitorAction()
        val NAME = "cluster:admin/alerting/monitor/get"
    }
}
