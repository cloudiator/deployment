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

package io.github.cloudiator.deployment.domain;

import io.github.cloudiator.deployment.domain.CloudiatorProcess.ProcessState;
import io.github.cloudiator.deployment.domain.CloudiatorProcess.Type;
import io.github.cloudiator.deployment.domain.Schedule.Instantiation;
import io.github.cloudiator.deployment.domain.Schedule.ScheduleState;
import java.util.Date;
import java.util.UUID;

public class MediaWikiSchedule {

  private static final String SCHEDULE_ID = UUID.randomUUID().toString();
  private static final String LB_ID = UUID.randomUUID().toString();
  private static final String WIKI_ID = UUID.randomUUID().toString();
  private static final String DATABASE_ID = UUID.randomUUID().toString();
  private static final String LB_NODE_ID = UUID.randomUUID().toString();
  private static final String WIKI_NODE_ID = UUID.randomUUID().toString();
  private static final String DATABASE_NODE_ID = UUID.randomUUID().toString();
  private static final Date NOW = new Date();

  public static CloudiatorProcess wikiProcess() {
    return CloudiatorSingleProcessBuilder.create().node(LB_NODE_ID)
        .userId(MediaWikiJob.wikiJob().userId()).scheduleId(SCHEDULE_ID)
        .taskName(MediaWikiJob.wikiTask().name())
        .endpoint("http://wiki.com").originId(WIKI_ID)
        .taskInterface(LanceInterface.class.getCanonicalName()).type(
            Type.LANCE).id(WIKI_ID).state(ProcessState.RUNNING).start(NOW).build();
  }

  public static CloudiatorProcess lbProcess() {
    return CloudiatorSingleProcessBuilder.create().node(WIKI_NODE_ID)
        .userId(MediaWikiJob.wikiJob().userId()).scheduleId(SCHEDULE_ID)
        .taskName(MediaWikiJob.loadBalancerTask().name())
        .endpoint("http://lb.com").originId(LB_ID)
        .taskInterface(LanceInterface.class.getCanonicalName()).type(
            Type.LANCE).id(LB_ID).state(ProcessState.RUNNING).start(NOW).build();
  }

  public static CloudiatorProcess dbProcess() {
    return CloudiatorSingleProcessBuilder.create().node(DATABASE_NODE_ID)
        .userId(MediaWikiJob.wikiJob().userId()).scheduleId(SCHEDULE_ID)
        .taskName(MediaWikiJob.databaseTask().name())
        .endpoint("http://db.com").originId(DATABASE_ID)
        .taskInterface(LanceInterface.class.getCanonicalName()).type(
            Type.LANCE).id(DATABASE_ID).state(ProcessState.RUNNING).start(NOW).build();
  }

  public static Schedule schedule() {

    final Schedule schedule = ScheduleImpl
        .of(SCHEDULE_ID, MediaWikiJob.wikiJob().userId(), MediaWikiJob.wikiJob().id(),
            Instantiation.AUTOMATIC, ScheduleState.RUNNING);
    schedule.addProcess(wikiProcess());
    schedule.addProcess(lbProcess());
    schedule.addProcess(dbProcess());

    return schedule;

  }

}
