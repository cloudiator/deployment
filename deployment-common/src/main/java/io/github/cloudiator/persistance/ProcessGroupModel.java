package io.github.cloudiator.persistance;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

/**
 * Created by Daniel Seybold on 22.11.2018.
 */

@Entity
class ProcessGroupModel extends Model {

  @Column(nullable = false)
  private String domainId;

  @ManyToOne
  private ScheduleModel scheduleModel;

  @OneToMany(mappedBy = "processGroupModel", cascade = CascadeType.ALL)
  private List<ProcessModel> processes;

  protected ProcessGroupModel() {

  }

  ProcessGroupModel(String id, ScheduleModel scheduleModel) {
    checkNotNull(id, "id is null");
    checkNotNull(scheduleModel, "scheduleModel is null");
    this.domainId = id;
    this.scheduleModel = scheduleModel;
  }

  public String getDomainId() {
    return domainId;
  }

  public List<ProcessModel> getProcesses() {
    return ImmutableList.copyOf(processes);
  }

  public ProcessGroupModel addProcess(ProcessModel process) {
    if (processes == null) {
      processes = new ArrayList<>();
    }


    processes.add(process);
    return this;
  }
}