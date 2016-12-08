/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab.backendapi.dao;

import com.epam.dlab.backendapi.api.instance.UserComputationalResourceDTO;
import com.epam.dlab.backendapi.api.instance.UserInstanceDTO;
import com.epam.dlab.constants.UserInstanceStatus;
import com.epam.dlab.dto.StatusBaseDTO;
import com.epam.dlab.dto.computational.ComputationalStatusDTO;
import com.epam.dlab.dto.exploratory.ExploratoryStatusDTO;
import com.epam.dlab.exceptions.DlabException;
import com.mongodb.MongoWriteException;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Optional;

import static com.epam.dlab.constants.UserInstanceStatus.TERMINATED;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;
import static com.mongodb.client.model.Updates.push;
import static com.mongodb.client.model.Updates.set;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public class InfrastructureProvisionDAO extends BaseDAO {
    public static final String EXPLORATORY_NAME = "exploratory_name";
    private static final String EXPLORATORY_ID = "exploratory_id";
    private static final String EXPLORATORY_URL = "exploratory_url";
    private static final String UPTIME = "up_time";
    private static final String COMPUTATIONAL_RESOURCES = "computational_resources";
    private static final String COMPUTATIONAL_NAME = "computational_name";
    private static final String COMPUTATIONAL_ID = "computational_id";

    private static final String SET = "$set";

    private static Bson exploratoryCondition(String user, String exploratoryName) {
        return and(eq(USER, user), eq(EXPLORATORY_NAME, exploratoryName));
    }

    private static Bson computationalCondition(String user,
                                               String exploratoryName,
                                               String computationalName) {
        return and(exploratoryCondition(user, exploratoryName),
                elemMatch(COMPUTATIONAL_RESOURCES, and(eq(COMPUTATIONAL_NAME, computationalName))));
    }

    private static String computationalField(String fieldName) {
        return COMPUTATIONAL_RESOURCES + FIELD_SET_DELIMETER + fieldName;
    }

    public Iterable<Document> find(String user) {
        return find(USER_INSTANCES, eq(USER, user));
    }

    public Iterable<Document> findShapes() {
        return mongoService.getCollection(SHAPES).find();
    }

    public String fetchExploratoryId(String user, String exploratoryName) {
        return findOne(USER_INSTANCES,
                exploratoryCondition(user, exploratoryName),
                fields(include(EXPLORATORY_ID), excludeId()))
                .orElse(new Document())
                .getOrDefault(EXPLORATORY_ID, EMPTY).toString();
    }

    public UserInstanceStatus fetchExploratoryStatus(String user, String exploratoryName) {
        return UserInstanceStatus.of(
                findOne(USER_INSTANCES,
                        exploratoryCondition(user, exploratoryName),
                        fields(include(STATUS), excludeId()))
                        .orElse(new Document())
                        .getOrDefault(STATUS, EMPTY).toString());
    }

    public boolean insertExploratory(UserInstanceDTO dto) {
        try {
            insertOne(USER_INSTANCES, dto);
            return true;
        } catch (MongoWriteException e) {
            return false;
        }
    }

    public void updateExploratoryStatus(StatusBaseDTO dto) {
        updateOne(USER_INSTANCES,
                exploratoryCondition(dto.getUser(), dto.getExploratoryName()),
                set(STATUS, dto.getStatus()));
    }

    public void updateExploratoryFields(ExploratoryStatusDTO dto) {
        Document values = new Document(STATUS, dto.getStatus()).append(UPTIME, dto.getUptime());
        if (dto.getExploratoryId() != null) {
            values.append(EXPLORATORY_ID, dto.getExploratoryId());
        }
        if (dto.getExploratoryUrl() != null) {
            values.append(EXPLORATORY_URL, dto.getExploratoryUrl());
        }
        updateOne(USER_INSTANCES,
                exploratoryCondition(dto.getUser(), dto.getExploratoryName()),
                new Document(SET, values));
    }

    public boolean addComputational(String user, String exploratoryName, UserComputationalResourceDTO computationalDTO) {
        Optional<UserInstanceDTO> optional = findOne(USER_INSTANCES,
                computationalCondition(user, exploratoryName, computationalDTO.getComputationalName()),
                UserInstanceDTO.class);

        if (optional.isPresent()) {
            long count = optional.get().getResources().size();
            if (count == 0) {
                updateOne(USER_INSTANCES,
                        eq(ID, optional.get().getId()),
                        push(COMPUTATIONAL_RESOURCES, convertToBson(computationalDTO)));
                return true;
            } else {
                return false;
            }
        } else {
            throw new DlabException("User '" + user + "' has no records for environment '" + exploratoryName + "'");
        }
    }

    @SuppressWarnings("unchecked")
    public String fetchComputationalId(String user, String exploratoryName, String computationalName) {
        return findOne(USER_INSTANCES,
                computationalCondition(user, exploratoryName, computationalName),
                fields(include(computationalField(COMPUTATIONAL_NAME))))
                .orElse(new Document())
                .getOrDefault(computationalField(COMPUTATIONAL_NAME), EMPTY).toString();
    }

    public void updateComputationalStatus(ComputationalStatusDTO dto) {
        updateComputationalStatus(dto.getUser(), dto.getExploratoryName(), dto.getComputationalName(), dto.getStatus(), false);
    }

    private void updateComputationalStatus(String user, String exploratoryName, String computationalName, String status, boolean clearUptime) {
        try {
            Document values = new Document(computationalField(STATUS), status);
            if (clearUptime) {
                values.append(computationalField(UPTIME), null);
            }
            updateOne(USER_INSTANCES,
                    and(exploratoryCondition(user, exploratoryName),
                            elemMatch(COMPUTATIONAL_RESOURCES, and(eq(COMPUTATIONAL_NAME, computationalName), not(eq(STATUS, TERMINATED.toString()))))),
                    new Document(SET, values));
        } catch (Throwable t) {
            throw new DlabException("Could not update computational resource status", t);
        }
    }

    public void updateComputationalStatusesForExploratory(StatusBaseDTO dto) {
        Document values = new Document(computationalField(STATUS), dto.getStatus());
        values.append(computationalField(UPTIME), null);
        updateOne(USER_INSTANCES,
                and(exploratoryCondition(dto.getUser(), dto.getExploratoryName()),
                        elemMatch(COMPUTATIONAL_RESOURCES, not(eq(STATUS, TERMINATED.toString())))),
                new Document(SET, values));
    }

    public void updateComputationalFields(ComputationalStatusDTO dto) {
        try {
            Document values = new Document(computationalField(STATUS), dto.getStatus())
                    .append(computationalField(UPTIME), dto.getUptime());
            if (dto.getComputationalId() != null) {
                values.append(computationalField(COMPUTATIONAL_ID), dto.getComputationalId());
            }
            updateOne(USER_INSTANCES, and(exploratoryCondition(dto.getUser(), dto.getExploratoryName()),
                    elemMatch(COMPUTATIONAL_RESOURCES, and(eq(COMPUTATIONAL_NAME, dto.getComputationalName()), not(eq(STATUS, TERMINATED.toString()))))),
                    new Document(SET, values));
        } catch (Throwable t) {
            throw new DlabException("Could not update computational resource status", t);
        }
    }
}
