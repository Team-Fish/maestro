/*
 * Copyright 2024 Netflix, Inc.
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
package com.netflix.maestro.engine.db;

import com.netflix.maestro.annotations.Nullable;
import com.netflix.maestro.models.instance.WorkflowInstance;
import com.netflix.maestro.models.instance.WorkflowRollupOverview;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/** Wrapper class for foreach iteration overview from DB. */
@RequiredArgsConstructor
@Getter
@ToString
public class ForeachIterationOverview {
  private final long instanceId;
  private final WorkflowInstance.Status status;
  private final WorkflowRollupOverview rollupOverview;
  @Nullable @Setter private Long runId;
}
