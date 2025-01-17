/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.scaling.core.job.dumper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.DumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.config.ingest.InventoryDumperConfiguration;
import org.apache.shardingsphere.data.pipeline.api.ingest.channel.PipelineChannel;
import org.apache.shardingsphere.data.pipeline.api.ingest.position.IngestPosition;
import org.apache.shardingsphere.data.pipeline.core.datasource.PipelineDataSourceManager;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.IncrementalDumper;
import org.apache.shardingsphere.data.pipeline.spi.ingest.dumper.InventoryDumper;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntry;
import org.apache.shardingsphere.scaling.core.spi.ScalingEntryLoader;

import java.lang.reflect.Constructor;

/**
 * Dumper factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DumperFactory {
    
    /**
     * Create inventory dumper.
     *
     * @param inventoryDumperConfig inventory dumper configuration
     * @param dataSourceManager data source factory
     * @param channel channel
     * @return inventory dumper
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static InventoryDumper createInventoryDumper(final InventoryDumperConfiguration inventoryDumperConfig, final PipelineDataSourceManager dataSourceManager, final PipelineChannel channel) {
        ScalingEntry scalingEntry = ScalingEntryLoader.getInstance(inventoryDumperConfig.getDataSourceConfig().getDatabaseType().getName());
        Constructor<? extends InventoryDumper> constructor = scalingEntry.getInventoryDumperClass()
                .getConstructor(InventoryDumperConfiguration.class, PipelineDataSourceManager.class, PipelineChannel.class);
        return constructor.newInstance(inventoryDumperConfig, dataSourceManager, channel);
    }
    
    /**
     * Create incremental dumper.
     *
     * @param dumperConfig dumper configuration
     * @param position position
     * @param channel channel
     * @return incremental dumper
     */
    @SneakyThrows(ReflectiveOperationException.class)
    public static IncrementalDumper createIncrementalDumper(final DumperConfiguration dumperConfig, final IngestPosition<?> position, final PipelineChannel channel) {
        String databaseType = dumperConfig.getDataSourceConfig().getDatabaseType().getName();
        ScalingEntry scalingEntry = ScalingEntryLoader.getInstance(databaseType);
        Constructor<? extends IncrementalDumper> constructor = scalingEntry.getIncrementalDumperClass().getConstructor(DumperConfiguration.class, IngestPosition.class, PipelineChannel.class);
        return constructor.newInstance(dumperConfig, position, channel);
    }
}
