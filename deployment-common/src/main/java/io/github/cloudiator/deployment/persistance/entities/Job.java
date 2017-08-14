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

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.MoreObjects;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * Created by daniel on 12.12.14.
 */
@Entity public class Job extends Model {

    @Column(unique = true, nullable = false) protected String name;
    @OneToMany(mappedBy = "application") private List<Task> tasks;



    /**
     * Empty constructor for hibernate.
     */
    protected Job() {
    }

    public Job(String name) {
        checkNotNull(name);
        checkArgument(!name.isEmpty());
        this.name = name;
    }

    public List<ApplicationComponent> getApplicationComponents() {
        return applicationComponents;
    }

    public void setApplicationComponents(List<ApplicationComponent> applicationComponents) {
        this.applicationComponents = applicationComponents;
    }

    public Set<Communication> communications() {
        return applicationComponents.stream()
            .flatMap(applicationComponent -> applicationComponent.communications().stream())
            .collect(Collectors.toSet());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override public String toString() {
        return MoreObjects.toStringHelper(this).add("id", getId()).add("name", getName())
            .toString();
    }
}
