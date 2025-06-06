/*
 * Copyright 2025 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package com.netflix.maestro.flow.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.netflix.maestro.flow.actor.Actor;
import com.netflix.maestro.flow.actor.ActorBaseTest;
import com.netflix.maestro.flow.models.Flow;
import com.netflix.maestro.flow.models.FlowDef;
import com.netflix.maestro.flow.models.FlowGroup;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

public class FlowExecutorTest extends ActorBaseTest {

  private FlowExecutor executor;

  @Before
  public void setUp() {
    when(properties.getInitialMaintenanceDelayInMillis()).thenReturn(1L);
    when(properties.getMaintenanceDelayInMillis()).thenReturn(100000L);
    when(properties.getMaxGroupNumPerNode()).thenReturn(2L);
    when(properties.getExpirationDurationInMillis()).thenReturn(12000L);
    when(properties.getEngineAddress()).thenReturn("test-address");
    executor = new FlowExecutor(context);
  }

  @Test
  public void testInitWithoutClaimingGroup() {
    when(context.claimGroup()).thenReturn(null);
    executor.init();
    verify(context, timeout(10000)).claimGroup();
  }

  @Test
  public void testInitWithClaimingGroup() {
    FlowGroup group = new FlowGroup(1, 1, "testAddress", 12345);
    when(context.claimGroup()).thenReturn(group);
    executor.init();
    verify(context, timeout(10000)).claimGroup();
    verify(context, timeout(3000).times(1)).run(any());
  }

  @Test
  public void testMaintenanceWithTooManyGroups() {
    when(properties.getMaxGroupNumPerNode()).thenReturn(0L);
    executor = new FlowExecutor(context);
    executor.init();
    verify(context, timeout(500).times(0)).claimGroup();
    verify(context, timeout(500).times(0)).run(any());
    verify(metrics, timeout(500).times(1)).gauge("num_of_groups", 0, FlowExecutor.class);
  }

  @Test
  public void testShutdown() {
    executor.shutdown();
    verify(context, times(1)).shutdown();
  }

  @Test
  public void testStartFlow() {
    when(context.trySaveGroup(1, "test-address"))
        .thenReturn(new FlowGroup(1, 1, "test-address", 12345));
    var id = executor.startFlow(1, "test-id", "wf-1", new FlowDef(), Map.of());
    verify(context, times(1)).trySaveGroup(anyLong(), any());
    var actorCaptor = ArgumentCaptor.forClass(Actor.class);
    verify(context, times(1)).run(actorCaptor.capture());
    var flowCaptor = ArgumentCaptor.forClass(Flow.class);
    verify(context, times(1)).saveFlow(flowCaptor.capture());
    assertEquals("test-id", id);
    assertTrue(actorCaptor.getValue().isRunning());
    assertEquals("wf-1", flowCaptor.getValue().getReference());
  }

  @Test
  public void testWakeUp() {
    assertFalse(executor.wakeUp(1L, "wf-1", "task1"));
    when(context.trySaveGroup(1, "test-address"))
        .thenReturn(new FlowGroup(1, 1, "test-address", 12345));

    executor.startFlow(1, "test-id", "wf-1", new FlowDef(), Map.of());
    assertTrue(executor.wakeUp(1L, "wf-1", "task1"));
    assertFalse(executor.wakeUp(2L, "wf-1", "task1"));
    assertTrue(executor.wakeUp(1L, "wf-2", "task1"));
  }
}
