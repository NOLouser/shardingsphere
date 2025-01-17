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

package org.apache.shardingsphere.mode.manager.memory;

import org.apache.shardingsphere.infra.config.schema.SchemaConfiguration;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.loader.SchemaLoader;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.builder.schema.SchemaRulesBuilder;
import org.apache.shardingsphere.infra.rule.identifier.type.InstanceAwareRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilder;
import org.apache.shardingsphere.mode.manager.ContextManagerBuilderParameter;
import org.apache.shardingsphere.mode.manager.memory.workerid.generator.MemoryWorkerIdGenerator;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsBuilder;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.apache.shardingsphere.transaction.context.TransactionContextsBuilder;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Memory context manager builder.
 */
public final class MemoryContextManagerBuilder implements ContextManagerBuilder {
    
    @Override
    public ContextManager build(final ContextManagerBuilderParameter parameter) throws SQLException {
        Map<String, Collection<ShardingSphereRule>> rules = SchemaRulesBuilder.buildRules(parameter.getSchemaConfigs(), parameter.getProps());
        Map<String, ShardingSphereSchema> schemas = getShardingSphereSchemas(parameter.getSchemaConfigs(), rules, parameter.getProps());
        MetaDataContexts metaDataContexts = new MetaDataContextsBuilder(parameter.getSchemaConfigs(), parameter.getGlobalRuleConfigs(), schemas, rules, parameter.getProps()).build(null);
        TransactionContexts transactionContexts = new TransactionContextsBuilder(metaDataContexts.getMetaDataMap(), metaDataContexts.getGlobalRuleMetaData().getRules()).build();
        ContextManager result = new ContextManager();
        result.init(metaDataContexts, transactionContexts, buildInstanceContext(parameter));
        buildSpecialRules(result);
        return result;
    }
    
    private Map<String, ShardingSphereSchema> getShardingSphereSchemas(final Map<String, ? extends SchemaConfiguration> schemaConfigs, final Map<String, Collection<ShardingSphereRule>> rules,
                                                                       final Properties props) throws SQLException {
        Map<String, ShardingSphereSchema> result = new LinkedHashMap<>(schemaConfigs.size(), 1);
        for (String each : schemaConfigs.keySet()) {
            result.put(each, SchemaLoader.load(schemaConfigs.get(each).getDataSources(), rules.get(each), props));
        }
        return result;
    }
    
    private InstanceContext buildInstanceContext(final ContextManagerBuilderParameter parameter) {
        ComputeNodeInstance instance = new ComputeNodeInstance();
        instance.setInstanceDefinition(parameter.getInstanceDefinition());
        instance.setLabels(parameter.getLabels());
        return new InstanceContext(instance, new MemoryWorkerIdGenerator());
    }
    
    private void buildSpecialRules(final ContextManager contextManager) {
        contextManager.getMetaDataContexts().getMetaDataMap().forEach((key, value)
            -> value.getRuleMetaData().getRules().stream().filter(each -> each instanceof InstanceAwareRule)
            .forEach(each -> ((InstanceAwareRule) each).setInstanceContext(contextManager.getInstanceContext())));
    }
    
    @Override
    public String getType() {
        return "Memory";
    }
    
    @Override
    public boolean isDefault() {
        return true;
    }
}
