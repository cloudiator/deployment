package io.github.cloudiator.deployment.persistance.domain;

import io.github.cloudiator.deployment.domain.Communication;
import io.github.cloudiator.deployment.domain.Job;
import io.github.cloudiator.deployment.domain.Port;
import io.github.cloudiator.deployment.domain.PortProvided;
import io.github.cloudiator.deployment.domain.PortRequired;
import io.github.cloudiator.deployment.domain.Task;
import io.github.cloudiator.deployment.persistance.converters.PortModelConverter;
import io.github.cloudiator.deployment.persistance.converters.TaskModelConverter;
import io.github.cloudiator.deployment.persistance.entities.CommunicationModel;
import io.github.cloudiator.deployment.persistance.entities.JobModel;
import io.github.cloudiator.deployment.persistance.entities.PortProvidedModel;
import io.github.cloudiator.deployment.persistance.entities.PortRequiredModel;
import io.github.cloudiator.deployment.persistance.entities.TaskModel;
import io.github.cloudiator.deployment.persistance.entities.TenantModel;
import io.github.cloudiator.deployment.persistance.repositories.CommunicationModelRepository;
import io.github.cloudiator.deployment.persistance.repositories.JobModelRepository;
import io.github.cloudiator.deployment.persistance.repositories.PortModelRepository;
import io.github.cloudiator.deployment.persistance.repositories.TaskModelRepository;
import io.github.cloudiator.deployment.persistance.repositories.TenantModelRepository;
import javax.annotation.Nullable;
import javax.inject.Inject;

public class JobDomainRepository {

  private final JobModelRepository jobModelRepository;
  private final TaskModelRepository taskModelRepository;
  private final PortModelRepository portModelRepository;
  private final CommunicationModelRepository communicationModelRepository;
  private final TaskModelConverter taskModelConverter = new TaskModelConverter();
  private final PortModelConverter portModelConverter = new PortModelConverter();
  private final TenantModelRepository tenantModelRepository;

  @Inject
  public JobDomainRepository(
      JobModelRepository jobModelRepository,
      TaskModelRepository taskModelRepository,
      PortModelRepository portModelRepository,
      CommunicationModelRepository communicationModelRepository,
      TenantModelRepository tenantModelRepository) {
    this.jobModelRepository = jobModelRepository;
    this.taskModelRepository = taskModelRepository;
    this.portModelRepository = portModelRepository;
    this.communicationModelRepository = communicationModelRepository;
    this.tenantModelRepository = tenantModelRepository;
  }

  @Nullable
  public Task getTaskByName(String name) {
    return taskModelConverter.apply(taskModelRepository.findByName(name));
  }

  public Task getTaskByNameAndUser(String name, String user) {
    return taskModelConverter.apply(taskModelRepository.findByNameAndUser(name, user));
  }

  public Port getPortByName(String name) {
    return portModelConverter.apply(portModelRepository.findByName(name));
  }

  public Port getPortByNameAndUser(String name, String user) {
    return portModelConverter.apply(portModelRepository.findByNameAndUser(name, user));
  }

  public void add(String userId, Job job) {

    TenantModel tenant = tenantModelRepository.findByUserId(userId);
    if (tenant == null) {
      tenant = new TenantModel(userId);
      tenantModelRepository.save(tenant);
    }

    JobModel jobModel = new JobModel(job.name(), tenant);
    jobModelRepository.save(jobModel);

    for (Task task : job.tasks()) {
      TaskModel taskModel = new TaskModel(task.name(), jobModel);
      taskModelRepository.save(taskModel);
      for (Port port : task.ports()) {
        if (port instanceof PortRequired) {
          PortRequiredModel portRequiredModel = new PortRequiredModel(port.name(), taskModel,
              ((PortRequired) port).updateAction(), ((PortRequired) port).isMandatory());
          portModelRepository.save(portRequiredModel);
        } else if (port instanceof PortProvided) {
          PortProvidedModel portProvidedModel = new PortProvidedModel(port.name(), taskModel,
              ((PortProvided) port).port());
          portModelRepository.save(portProvidedModel);
        } else {
          throw new AssertionError(String.format("Illegal port type %s.", port.getClass()));
        }
      }
      for (Communication communication : job.communications()) {
        CommunicationModel communicationModel = new CommunicationModel(communication.source(),
            communication.target());
        communicationModelRepository.save(communicationModel);
      }
    }
  }

}
