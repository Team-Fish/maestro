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
package com.netflix.maestro.flow.actor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.netflix.maestro.flow.models.FlowGroup;
import org.junit.Test;

public class ActorTest extends ActorBaseTest {

  @Test
  public void testStartGroupActor() {
    FlowGroup group = new FlowGroup(1, 2, "testAddress", 12345);
    GroupActor actor = (GroupActor) Actor.startGroupActor(group, context);
    verify(context, times(1)).run(any());
    // check if the group actor was started
    assertTrue(actor.isRunning());
    assertEquals(2, actor.generation());
    verifyActions(actor, Action.GROUP_START);
  }
}
