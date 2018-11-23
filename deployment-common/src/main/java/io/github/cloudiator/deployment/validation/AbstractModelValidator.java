/*
 * Copyright 2018 University of Ulm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.cloudiator.deployment.validation;

import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractModelValidator<E> implements ModelValidator<E> {

  private final Set<ValidationMessage> validationMessages;

  public AbstractModelValidator() {
    this.validationMessages = new HashSet<>();
  }

  @Override
  public final Set<ValidationMessage> validate(E e) {
    inspect(e);
    return ImmutableSet.copyOf(validationMessages);
  }

  public abstract void inspect(E e);

  protected void addValidationMessage(ValidationMessage validationMessage) {
    this.validationMessages.add(validationMessage);
  }

}
