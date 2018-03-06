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

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Task;
import javax.annotation.Nullable;

public class TaskDomainRepository {

  private final TaskModelRepository taskModelRepository;
  private static final TaskModelConverter TASK_MODEL_CONVERTER = new TaskModelConverter();

  @Inject
  public TaskDomainRepository(
      TaskModelRepository taskModelRepository) {
    this.taskModelRepository = taskModelRepository;
  }

  @Nullable
  public Task get(String name) {
    return TASK_MODEL_CONVERTER.apply(getModel(name));
  }

  @Nullable
  TaskModel getModel(String name) {
    return taskModelRepository.findByName(name);
  }

  void save(Task domain, JobModel jobModel) {
    saveAndGet(domain, jobModel);
  }

  TaskModel saveAndGet(Task domain, JobModel jobModel) {
    final TaskModel taskModel = createTaskModel(domain, jobModel);
    taskModelRepository.save(taskModel);
    return taskModel;
  }

  private TaskModel createTaskModel(Task domain, JobModel jobModel) {
    return new TaskModel(domain.name(), jobModel);
  }

}
