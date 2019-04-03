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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import javax.persistence.Column;
import javax.persistence.Entity;

@Entity
public class IdentifierRequirementModel extends RequirementModel {

  @Column(nullable = false)
  private String hardwareId;

  @Column(nullable = false)
  private String locationId;

  @Column(nullable = false)
  private String imageId;

  protected IdentifierRequirementModel() {

  }

  IdentifierRequirementModel(TaskModel task, JobModel job, String hardwareId, String locationId,
      String imageId) {
    super(task, job);

    checkNotNull(hardwareId, "hardwareId is null");
    checkArgument(!hardwareId.isEmpty(), "hardwareId is empty");
    this.hardwareId = hardwareId;

    checkNotNull(locationId, "locationId is null");
    checkArgument(!locationId.isEmpty(), "locationId is empty");
    this.locationId = locationId;

    checkNotNull(imageId, "imageId is null");
    checkArgument(!imageId.isEmpty(), "imageId is empty");
    this.imageId = imageId;
  }

  public String getHardwareId() {
    return hardwareId;
  }

  public String getLocationId() {
    return locationId;
  }

  public String getImageId() {
    return imageId;
  }
}
