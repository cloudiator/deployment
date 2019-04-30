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

package io.github.cloudiator.deployment.graph;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import io.github.cloudiator.deployment.domain.MediaWikiJob;
import io.github.cloudiator.deployment.domain.Task;
import java.util.Iterator;
import java.util.List;
import org.junit.Test;

public class JobGraphTest {

  private final JobGraph jobGraph = JobGraph.of(MediaWikiJob.wikiJob());

  @Test
  public void evaluationOrder() {

    final Iterator<Task> taskIterator = jobGraph.evaluationOrder();

    final Task shouldBeDatabase = taskIterator.next();
    assertThat(shouldBeDatabase, equalTo(MediaWikiJob.databaseTask()));

    final Task shouldBeWiki = taskIterator.next();
    assertThat(shouldBeWiki, equalTo(MediaWikiJob.wikiTask()));

    final Task shouldBeLB = taskIterator.next();
    assertThat(shouldBeLB, equalTo(MediaWikiJob.loadBalancerTask()));


  }

  @Test
  public void getDependentTasks() {
    final List<Task> databaseDependencies = jobGraph.getDependentTasks(MediaWikiJob.databaseTask());

    assertThat(databaseDependencies, contains(MediaWikiJob.wikiTask()));

    final List<Task> wikiDependencies = jobGraph.getDependentTasks(MediaWikiJob.wikiTask());

    assertThat(wikiDependencies, contains(MediaWikiJob.loadBalancerTask()));
    assertThat(wikiDependencies, not(contains(MediaWikiJob.databaseTask())));

  }
}
