/*
 * Copyright 2018 University of Ulm
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;

import java.util.Iterator;
import java.util.Set;
import org.junit.Test;

public class JobImplTest {


  @Test
  public void providingTaskTest() {
    Job job = MediaWikiJob.wikiJob();
    final Task wikiTask = job.providingTask(MediaWikiJob.wikiWithLB());
    final Task dbTask = job.providingTask(MediaWikiJob.wikiWithDB());

    assertThat(wikiTask, equalTo(MediaWikiJob.wikiTask()));
    assertThat(dbTask, equalTo(MediaWikiJob.databaseTask()));
  }

  @Test
  public void requiredTaskTest() {
    Job job = MediaWikiJob.wikiJob();

    final Task lbTask = job.requiredTask(MediaWikiJob.wikiWithLB());
    final Task wikiTask = job.requiredTask(MediaWikiJob.wikiWithDB());

    assertThat(lbTask, equalTo(MediaWikiJob.loadBalancerTask()));
    assertThat(wikiTask, equalTo(MediaWikiJob.wikiTask()));

  }

  @Test
  public void attachedCommunicationsTest() {

    Job job = MediaWikiJob.wikiJob();

    final Set<Communication> lbCommunications = job.attachedCommunications(MediaWikiJob.lbProv());
    assertThat(lbCommunications.size(), equalTo(0));

    final Set<Communication> databaseCommunications = job
        .attachedCommunications(MediaWikiJob.databaseProvided());
    assertThat(databaseCommunications.size(), equalTo(1));
    assertThat(databaseCommunications, contains(MediaWikiJob.wikiWithDB()));

    final Set<Communication> wikiCommunications = job
        .attachedCommunications(MediaWikiJob.wikiProvided());
    assertThat(wikiCommunications.size(), equalTo(1));
    assertThat(wikiCommunications, contains(MediaWikiJob.wikiWithLB()));

  }

  @Test
  public void consumedByTest() {

    Job job = MediaWikiJob.wikiJob();

    assertThat(job.consumedBy(MediaWikiJob.databaseTask()), contains(MediaWikiJob.wikiTask()));
    assertThat(job.consumedBy(MediaWikiJob.wikiTask()), contains(MediaWikiJob.loadBalancerTask()));


  }

  @Test
  public void taskInOrderTest() {
    Job job = MediaWikiJob.wikiJob();
    final Iterator<Task> taskIterator = job.tasksInOrder();

    job.tasksInOrder().forEachRemaining(System.out::println);

    Task first = taskIterator.next();
    assertThat(first, equalTo(MediaWikiJob.databaseTask()));
  }
}
