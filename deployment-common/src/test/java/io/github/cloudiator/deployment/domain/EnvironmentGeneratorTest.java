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

import static org.hamcrest.MatcherAssert.assertThat;

import org.hamcrest.collection.IsMapContaining;
import org.junit.Test;

public class EnvironmentGeneratorTest {

  @Test
  public void generate() {

    final EnvironmentGenerator generator = EnvironmentGenerator
        .of(MediaWikiJob.wikiJob(), MediaWikiSchedule.schedule());

    final Environment wikiEnv = generator.generate(MediaWikiSchedule.wikiProcess());
    assertThat(wikiEnv, IsMapContaining
        .hasEntry("PUBLIC_" + MediaWikiJob.wikiRequiresDatabase().name(), "http://db.com"));

    final Environment lbEnv = generator.generate(MediaWikiSchedule.lbProcess());
    assertThat(lbEnv, IsMapContaining
        .hasEntry("PUBLIC_" + MediaWikiJob.loadbalancerreqwiki().name(), "http://wiki.com"));
  }
}
