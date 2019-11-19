package io.github.cloudiator.deployment.faasagent.helper;

import io.github.cloudiator.deployment.domain.Function;

public interface SaveFunctionHelper {
    void persistFunction(Function function, String userId);
}
