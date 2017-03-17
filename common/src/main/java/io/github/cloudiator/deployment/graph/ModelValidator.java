package io.github.cloudiator.deployment.graph;

import java.util.Set;

/**
 * Created by daniel on 19.06.16.
 */
public interface ModelValidator<E> {

    Set<ValidationMessage> validate(E e);

}
