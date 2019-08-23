package io.github.cloudiator.deployment.faasagent.helper;

import com.google.inject.Inject;
import io.github.cloudiator.deployment.domain.Function;
import io.github.cloudiator.persistance.FunctionDomainRepository;

public class SaveFunctionBaseHelper implements SaveFunctionHelper {

    private final FunctionDomainRepository functionDomainRepository;

    @Inject
    public SaveFunctionBaseHelper(FunctionDomainRepository functionDomainRepository) {
        this.functionDomainRepository = functionDomainRepository;
    }

    @Override
    public synchronized void persistFunction(Function function, String userId) {
        functionDomainRepository.save(function, userId);
    }
}
