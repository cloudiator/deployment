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

import com.google.common.base.MoreObjects.ToStringHelper;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;

/**
 * Created by daniel on 12.12.14.
 */
@Entity public abstract class Task extends Model {

    @Column(unique = true, nullable = false) private String name;
    @OneToMany(mappedBy = "task") private List<Port> ports;

    /**
     * Empty constructor for hibernate.
     */
    protected Task() {
    }

    public Task(String name) {
        checkNotNull(name);
        checkArgument(!name.isEmpty());
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    protected ToStringHelper stringHelper() {
     return super.stringHelper().add("name", name).add("ports", Arrays.toString(ports.toArray()));
    }
}
