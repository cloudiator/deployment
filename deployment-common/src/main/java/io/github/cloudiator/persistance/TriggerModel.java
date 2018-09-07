package io.github.cloudiator.persistance;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@Entity
public class TriggerModel extends Model {

  @ManyToOne
  private FaasTaskInterfaceModel faasInterface;

  protected TriggerModel() {

  }

  protected TriggerModel(FaasTaskInterfaceModel faasInterface) {
    this.faasInterface = faasInterface;
  }
}
