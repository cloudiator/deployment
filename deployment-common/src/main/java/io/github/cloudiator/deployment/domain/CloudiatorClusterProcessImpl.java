package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableSet;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Created by Daniel Seybold on 29.11.2018.
 */
public class CloudiatorClusterProcessImpl extends CloudiatorProcessImpl implements
    CloudiatorClusterProcess {

  private final Set<String> nodes;


  CloudiatorClusterProcessImpl(String id, @Nullable String originId, String userId,
      String scheduleId, String taskName, String lifecycleInterface,
      CloudiatorProcess.ProcessState state, Type type, Set<String> nodes,
      @Nullable String diagnostic, @Nullable String reason, @Nullable String endpoint,
      Set<IpAddress> ipAddresses, Date start, @Nullable Date stop, @Nullable String secret) {
    super(id, originId, userId, scheduleId, taskName, lifecycleInterface, state, type, diagnostic,
        reason, endpoint, ipAddresses, start, stop, secret);

    checkNotNull(nodes, "nodes is null");
    this.nodes = new HashSet<>(nodes);
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("nodes", nodes);
  }

  @Override
  public Set<String> nodes() {
    return ImmutableSet.copyOf(nodes);
  }
}
