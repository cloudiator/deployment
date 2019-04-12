/*
 * Copyright 2014-2019 University of Ulm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.cloudiator.deployment.messaging;


import de.uniulm.omi.cloudiator.util.TwoWayConverter;
import io.github.cloudiator.deployment.domain.Behaviours;
import io.github.cloudiator.deployment.domain.CollisionHandling;
import io.github.cloudiator.deployment.domain.Interval;
import io.github.cloudiator.deployment.domain.Intervals;
import io.github.cloudiator.deployment.domain.PeriodicBehaviour;
import java.util.concurrent.TimeUnit;
import org.cloudiator.messages.entities.CommonEntities;
import org.cloudiator.messages.entities.CommonEntities.Interval.Builder;
import org.cloudiator.messages.entities.CommonEntities.Unit;
import org.cloudiator.messages.entities.TaskEntities;

public class PeriodicBehaviourConverter implements
    TwoWayConverter<TaskEntities.PeriodicBehaviour, PeriodicBehaviour> {

  public static final PeriodicBehaviourConverter INSTANCE = new PeriodicBehaviourConverter();
  private static final IntervalConverter INTERVAL_CONVERTER = new IntervalConverter();
  private static final CollisionHandlingConverter COLLISION_HANDLING_CONVERTER = new CollisionHandlingConverter();

  private PeriodicBehaviourConverter() {
  }

  @Override
  public TaskEntities.PeriodicBehaviour applyBack(PeriodicBehaviour periodicBehaviour) {

    return TaskEntities.PeriodicBehaviour.newBuilder().setCollisionHandling(
        COLLISION_HANDLING_CONVERTER.applyBack(periodicBehaviour.collisionHandling()))
        .setInterval(INTERVAL_CONVERTER.applyBack(periodicBehaviour.interval())).build();
  }

  @Override
  public PeriodicBehaviour apply(TaskEntities.PeriodicBehaviour periodicBehaviour) {

    return (PeriodicBehaviour) Behaviours
        .periodic(INTERVAL_CONVERTER.apply(periodicBehaviour.getInterval()),
            COLLISION_HANDLING_CONVERTER.apply(periodicBehaviour.getCollisionHandling()));
  }

  private static class CollisionHandlingConverter implements
      TwoWayConverter<TaskEntities.CollisionHandling, CollisionHandling> {

    @Override
    public TaskEntities.CollisionHandling applyBack(CollisionHandling collisionHandling) {

      switch (collisionHandling) {
        case PARALLEL:
          return TaskEntities.CollisionHandling.PARALLEL;
        case CANCEL:
          return TaskEntities.CollisionHandling.CANCEL;
        case SKIP:
          return TaskEntities.CollisionHandling.SKIP;
        default:
          throw new AssertionError("Unknown collision handling " + collisionHandling);
      }
    }

    @Override
    public CollisionHandling apply(TaskEntities.CollisionHandling collisionHandling) {
      switch (collisionHandling) {
        case SKIP:
          return CollisionHandling.SKIP;
        case CANCEL:
          return CollisionHandling.CANCEL;
        case PARALLEL:
          return CollisionHandling.PARALLEL;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown collision handling " + collisionHandling);
      }
    }
  }

  private static class IntervalConverter implements
      TwoWayConverter<CommonEntities.Interval, Interval> {


    private IntervalConverter() {
    }

    @Override
    public CommonEntities.Interval applyBack(Interval interval) {

      final Builder builder = CommonEntities.Interval.newBuilder();

      switch (interval.timeUnit()) {
        case DAYS:
          builder.setUnit(Unit.DAYS);
          break;
        case HOURS:
          builder.setUnit(Unit.HOURS);
          break;
        case MINUTES:
          builder.setUnit(Unit.MINUTES);
          break;
        case SECONDS:
          builder.setUnit(Unit.SECONDS);
          break;
        case NANOSECONDS:
          builder.setUnit(Unit.NANOSECONDS);
          break;
        case MICROSECONDS:
          builder.setUnit(Unit.MICROSECONDS);
          break;
        case MILLISECONDS:
          builder.setUnit(Unit.MILLISECONDS);
          break;
        default:
          throw new AssertionError("Unknown timeUnit " + interval.timeUnit());
      }

      builder.setPeriod(interval.period());
      return builder.build();
    }

    @Override
    public Interval apply(CommonEntities.Interval interval) {

      TimeUnit timeUnit;
      switch (interval.getUnit()) {
        case MILLISECONDS:
          timeUnit = TimeUnit.MILLISECONDS;
          break;
        case MICROSECONDS:
          timeUnit = TimeUnit.MICROSECONDS;
          break;
        case NANOSECONDS:
          timeUnit = TimeUnit.NANOSECONDS;
          break;
        case SECONDS:
          timeUnit = TimeUnit.SECONDS;
          break;
        case MINUTES:
          timeUnit = TimeUnit.MINUTES;
          break;
        case HOURS:
          timeUnit = TimeUnit.HOURS;
          break;
        case DAYS:
          timeUnit = TimeUnit.DAYS;
          break;
        case UNRECOGNIZED:
        default:
          throw new AssertionError("Unknown time unit " + interval.getUnit());
      }

      return Intervals.interval(timeUnit, interval.getPeriod());
    }
  }

}
