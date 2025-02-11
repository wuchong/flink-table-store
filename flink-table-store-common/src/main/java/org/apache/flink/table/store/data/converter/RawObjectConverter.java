/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.table.store.data.converter;

import org.apache.flink.annotation.Internal;
import org.apache.flink.api.common.ExecutionConfig;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.table.data.RawValueData;
import org.apache.flink.table.types.DataType;
import org.apache.flink.table.types.logical.LogicalType;
import org.apache.flink.table.types.logical.RawType;
import org.apache.flink.table.types.logical.TypeInformationRawType;

/** Converter for {@link RawType} of object external type. */
@Internal
public class RawObjectConverter<T> implements DataStructureConverter<RawValueData<T>, T> {

    private static final long serialVersionUID = 1L;

    private final TypeSerializer<T> serializer;

    private RawObjectConverter(TypeSerializer<T> serializer) {
        this.serializer = serializer;
    }

    @Override
    public RawValueData<T> toInternal(T external) {
        return RawValueData.fromObject(external);
    }

    @Override
    public T toExternal(RawValueData<T> internal) {
        return internal.toObject(serializer);
    }

    // --------------------------------------------------------------------------------------------
    // Factory method
    // --------------------------------------------------------------------------------------------

    public static RawObjectConverter<?> create(DataType dataType) {
        final LogicalType logicalType = dataType.getLogicalType();
        final TypeSerializer<?> serializer;
        if (logicalType instanceof TypeInformationRawType) {
            serializer =
                    ((TypeInformationRawType<?>) logicalType)
                            .getTypeInformation()
                            .createSerializer(new ExecutionConfig());
        } else {
            serializer = ((RawType<?>) dataType.getLogicalType()).getTypeSerializer();
        }
        return new RawObjectConverter<>(serializer);
    }
}
