package io.github.cloudiator.deployment.faasagent.helper;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import io.github.cloudiator.deployment.domain.Function;

@Singleton
public class SaveFunctionTransactionalHelper implements SaveFunctionHelper {

    private SaveFunctionBaseHelper saveFunctionHelper;

    @Inject
    public SaveFunctionTransactionalHelper(SaveFunctionBaseHelper saveFunctionHelper) {
        this.saveFunctionHelper = saveFunctionHelper;
    }

    @Override
    @Transactional
    public void persistFunction(Function function, String userId) {
        saveFunctionHelper.persistFunction(function, userId);
    }
}
