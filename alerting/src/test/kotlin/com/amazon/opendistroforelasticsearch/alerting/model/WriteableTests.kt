/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.alerting.model

import com.amazon.opendistroforelasticsearch.alerting.core.model.SearchInput
import com.amazon.opendistroforelasticsearch.alerting.model.action.Action
import com.amazon.opendistroforelasticsearch.alerting.model.action.Throttle
import com.amazon.opendistroforelasticsearch.alerting.randomAction
import com.amazon.opendistroforelasticsearch.alerting.randomActionRunResult
import com.amazon.opendistroforelasticsearch.alerting.randomInputRunResults
import com.amazon.opendistroforelasticsearch.alerting.randomMonitor
import com.amazon.opendistroforelasticsearch.alerting.randomMonitorRunResult
import com.amazon.opendistroforelasticsearch.alerting.randomThrottle
import com.amazon.opendistroforelasticsearch.alerting.randomTrigger
import com.amazon.opendistroforelasticsearch.alerting.randomTriggerRunResult
import org.elasticsearch.common.io.stream.BytesStreamOutput
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.search.builder.SearchSourceBuilder
import org.elasticsearch.test.ESTestCase

class WriteableTests : ESTestCase() {

    fun `test throttle as stream`() {
        val throttle = randomThrottle()
        val out = BytesStreamOutput()
        throttle.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newThrottle = Throttle(sin)
        assertEquals("Round tripping Throttle doesn't work", throttle, newThrottle)
    }

    fun `test action as stream`() {
        val action = randomAction()
        val out = BytesStreamOutput()
        action.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newAction = Action(sin)
        assertEquals("Round tripping Action doesn't work", action, newAction)
    }

    fun `test action as stream with null subject template`() {
        val action = randomAction().copy(subjectTemplate = null)
        val out = BytesStreamOutput()
        action.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newAction = Action(sin)
        assertEquals("Round tripping Action doesn't work", action, newAction)
    }

    fun `test action as stream with null throttle`() {
        val action = randomAction().copy(throttle = null)
        val out = BytesStreamOutput()
        action.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newAction = Action(sin)
        assertEquals("Round tripping Action doesn't work", action, newAction)
    }

    fun `test action as stream with throttled enabled and null throttle`() {
        val action = randomAction().copy(throttle = null).copy(throttleEnabled = true)
        val out = BytesStreamOutput()
        action.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newAction = Action(sin)
        assertEquals("Round tripping Action doesn't work", action, newAction)
    }

    fun `test monitor as stream`() {
        val monitor = randomMonitor().copy(inputs = listOf(SearchInput(emptyList(), SearchSourceBuilder())))
        val out = BytesStreamOutput()
        monitor.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newMonitor = Monitor(sin)
        assertEquals("Round tripping Monitor doesn't work", monitor, newMonitor)
    }

    fun `test trigger as stream`() {
        val trigger = randomTrigger()
        val out = BytesStreamOutput()
        trigger.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newTrigger = Trigger(sin)
        assertEquals("Round tripping Trigger doesn't work", trigger, newTrigger)
    }

    fun `test actionrunresult as stream`() {
        val actionRunResult = randomActionRunResult()
        val out = BytesStreamOutput()
        actionRunResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newActionRunResult = ActionRunResult(sin)
        assertEquals("Round tripping ActionRunResult doesn't work", actionRunResult, newActionRunResult)
    }

    fun `test triggerrunresult as stream`() {
        val runResult = randomTriggerRunResult()
        val out = BytesStreamOutput()
        runResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRunResult = TriggerRunResult(sin)
        assertEquals("Round tripping ActionRunResult doesn't work", runResult, newRunResult)
    }

    fun `test inputrunresult as stream`() {
        val runResult = randomInputRunResults()
        val out = BytesStreamOutput()
        runResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRunResult = InputRunResults.readFrom(sin)
        assertEquals("Round tripping InputRunResults doesn't work", runResult, newRunResult)
    }

    fun `test monitorrunresult as stream`() {
        val runResult = randomMonitorRunResult()
        val out = BytesStreamOutput()
        runResult.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newRunResult = MonitorRunResult(sin)
        assertEquals("Round tripping MonitorRunResult doesn't work", runResult, newRunResult)
    }

    fun `test searchinput as stream`() {
        val input = SearchInput(emptyList(), SearchSourceBuilder())
        val out = BytesStreamOutput()
        input.writeTo(out)
        val sin = StreamInput.wrap(out.bytes().toBytesRef().bytes)
        val newInput = SearchInput(sin)
        assertEquals("Round tripping MonitorRunResult doesn't work", input, newInput)
    }
}