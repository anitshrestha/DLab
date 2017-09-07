/*
 * Copyright (c) 2017, EPAM SYSTEMS INC
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

package com.epam.dlab.dto.azure.exploratory;

import com.epam.dlab.dto.exploratory.ExploratoryActionDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.MoreObjects;

public class ExploratoryActionStopAzure extends ExploratoryActionDTO<ExploratoryActionStopAzure> {
    @JsonProperty("computational_name")
    private String computationalName;

    public String getComputationalName() {
        return computationalName;
    }

    public ExploratoryActionStopAzure setComputationalName(String computationalName) {
        this.computationalName = computationalName;
        return this;
    }

    @Override
    public MoreObjects.ToStringHelper toStringHelper(Object self) {
        return super.toStringHelper(self)
                .add("computationalName", computationalName);
    }
}
