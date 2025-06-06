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
package com.netflix.maestro.validations;

import com.netflix.maestro.models.trigger.CronTimeTrigger;
import com.netflix.maestro.models.trigger.TimeTrigger;
import jakarta.validation.ConstraintViolation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.Assert;
import org.junit.Test;

public class TimeTriggerConstraintTest extends BaseConstraintTest {
  private static class DummyWorkflow {
    List<@TimeTriggerConstraint TimeTrigger> triggers;

    DummyWorkflow(String cronExpr) {
      CronTimeTrigger cron = new CronTimeTrigger();
      cron.setCron(cronExpr);
      this.triggers = new ArrayList<>();
      triggers.add(cron);
    }

    DummyWorkflow(String cronExpr, String timezone) {
      CronTimeTrigger cron = new CronTimeTrigger();
      cron.setCron(cronExpr);
      cron.setTimezone(timezone);
      this.triggers = new ArrayList<>();
      triggers.add(cron);
    }
  }

  @Test
  public void testValid() {
    Assert.assertTrue(validator.validate(new DummyWorkflow("0 * * * *")).isEmpty());
    Assert.assertTrue(validator.validate(new DummyWorkflow("*/5 * * * *")).isEmpty());
    Assert.assertTrue(validator.validate(new DummyWorkflow("*/3 * * * *")).isEmpty());
    Assert.assertTrue(validator.validate(new DummyWorkflow("0 * * * *", null)).isEmpty());
    Assert.assertTrue(validator.validate(new DummyWorkflow("*/5 * * * *", "")).isEmpty());
    Assert.assertTrue(validator.validate(new DummyWorkflow("*/3 * * * *", "US/Pacific")).isEmpty());
  }

  @Test
  public void testInValidCron() {
    Set<ConstraintViolation<DummyWorkflow>> violations =
        validator.validate(new DummyWorkflow("*/1 * * * *"));
    Assert.assertEquals(1, violations.size());
    Assert.assertEquals(
        "[time-trigger] the interval between time triggers is less than the minimal value [120000] millis",
        violations.iterator().next().getMessage());
  }

  @Test
  public void testInValidTimezone() {
    Set<ConstraintViolation<DummyWorkflow>> violations =
        validator.validate(new DummyWorkflow("*/1 * * * *", "foo"));
    Assert.assertEquals(1, violations.size());
    Assert.assertEquals(
        "[time-trigger] is not valid - rejected value is [CronTimeTrigger(cron=*/1 * * * *, timezone=foo)] - error: [Unknown time-zone ID: foo]",
        violations.iterator().next().getMessage());
  }
}
