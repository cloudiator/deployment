/*
 * Copyright (c) 2014-2016 University of Ulm
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.  Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package lance;

import de.uniulm.omi.cloudiator.lance.lca.container.ContainerType;
import models.ApplicationComponent;
import play.Configuration;

import java.util.function.Function;

/**
 * Created by daniel on 11.10.16.
 */
public class ApplicationComponentToContainerType
    implements Function<ApplicationComponent, ContainerType> {

    private final Configuration configuration;

    public ApplicationComponentToContainerType(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override public ContainerType apply(ApplicationComponent applicationComponent) {
        if (!dockerInstalled()) {
            return ContainerType.PLAIN;
        } else {
            return applicationComponent.containerType();
        }
    }

    private boolean dockerInstalled() {
        return this.configuration.getBoolean("colosseum.installer.linux.lance.docker.install.flag");
    }
}
