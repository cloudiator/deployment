/*
 * Copyright (c) 2014-2017 University of Ulm
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

package io.github.cloudiator.deployment.persistance.entities;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

/**
 * Created by daniel on 07.01.15.
 */
@Entity public class CommunicationModel extends Model {

    @OneToOne(optional = false) @JoinColumn(name = "requiredCommunication") private PortRequiredModel
        requiredPort;
    @ManyToOne(optional = false) private PortProvidedModel providedPort;

    /**
     * Empty constructor for hibernate.
     */
    protected CommunicationModel() {

    }

    public CommunicationModel(PortRequiredModel requiredPort, PortProvidedModel providedPort) {
        this.requiredPort = requiredPort;
        this.providedPort = providedPort;
    }

    public PortRequiredModel getRequiredPort() {
        return requiredPort;
    }

    public PortProvidedModel getProvidedPort() {
        return providedPort;
    }

    public boolean isMandatory() {
        return requiredPort.getMandatory();
    }
}
