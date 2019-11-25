package io.github.cloudiator.deployment.domain;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import de.uniulm.omi.cloudiator.sword.domain.IpAddress;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Created by Daniel Seybold on 29.11.2018.
 */
public class CloudiatorSingleProcessImpl extends CloudiatorProcessImpl implements
    CloudiatorSingleProcess {


  private final String node;

  CloudiatorSingleProcessImpl(String id, @Nullable String originId, String userId,
      String scheduleId,
      String taskName,
      String taskInterface,
      ProcessState state, Type type, String node, @Nullable String diagnostic,
      @Nullable String reason, @Nullable String endpoint, Set<IpAddress> ipAddresses, Date start,
      @Nullable Date stop, @Nullable String secret) {
    super(id, originId, userId, scheduleId, taskName, taskInterface, state, type, diagnostic,
        reason, endpoint, ipAddresses, start, stop, secret);

    checkNotNull(node, "node is null");
    checkArgument(!node.isEmpty(), "node is empty");
    this.node = node;

  }

  @Override
  public String node() {
    return node;
  }

  @Override
  protected ToStringHelper stringHelper() {
    return super.stringHelper().add("node", node);
  }

  @Override
  public Set<String> nodes() {
    return Collections.singleton(node);
  }
}
