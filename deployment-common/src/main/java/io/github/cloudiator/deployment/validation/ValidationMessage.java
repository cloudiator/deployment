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

/**
 * Created by daniel on 19.06.16.
 */
public class ValidationMessage {

    private final String message;
    private final Type type;

    private ValidationMessage(String message, Type type) {
        this.message = message;
        this.type = type;
    }

    public static ValidationMessage of(String message, Type type) {
        return new ValidationMessage(message, type);
    }

    public String message() {
        return message;
    }

    public Type type() {
        return type;
    }

    @Override public String toString() {
        return message;
    }

    public enum Type {
        WARNING,
        ERROR
    }
}
