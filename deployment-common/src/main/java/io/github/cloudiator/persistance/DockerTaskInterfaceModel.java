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

package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;

@Entity
public class DockerTaskInterfaceModel extends TaskInterfaceModel {

  @Column(nullable = false)
  private String dockerImage;

  @ElementCollection
  private Map<String, String> environmentMap = new HashMap<>();

  public DockerTaskInterfaceModel(TaskModel taskModel, String dockerImage,
      Map<String, String> environmentMap) {
    super(taskModel);

    checkNotNull(dockerImage, "DockerImage is null");

    this.dockerImage = dockerImage;
    this.environmentMap = environmentMap;
  }

  @Nullable
  public String getDockerImage() {
    return dockerImage;
  }

  public Map<String, String> getEnvVars() {
    return ImmutableMap.copyOf(environmentMap);
  }

  /**
   * Empty constructor for hibernate.
   */
  protected DockerTaskInterfaceModel() {
  }


  public DockerTaskInterfaceModel putEnvVars(String key, String value) {
    checkNotNull(key, "key is null");
    checkNotNull(value, "value is null");
    environmentMap.put(key, value);
    return this;
  }
}
