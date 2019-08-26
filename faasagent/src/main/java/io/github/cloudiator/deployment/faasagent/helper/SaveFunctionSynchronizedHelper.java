package io.github.cloudiator.deployment.faasagent.helper;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Function;

public class SaveFunctionSynchronizedHelper implements SaveFunctionHelper {

    private SaveFunctionTransactionalHelper saveFunctionTransactionalHelper;

    @Inject
    public SaveFunctionSynchronizedHelper(SaveFunctionTransactionalHelper saveFunctionTransactionalHelper) {
        this.saveFunctionTransactionalHelper = saveFunctionTransactionalHelper;
    }

    @Override
    public synchronized void persistFunction(Function function, String userId) {
        saveFunctionTransactionalHelper.persistFunction(function, userId);
    }
}
