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

package org.apache.shardingsphere.infra.rewrite.sql.token.generator.builder;

import org.apache.shardingsphere.infra.binder.statement.dal.ShowColumnsStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.generic.RemoveTokenGenerator;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DefaultTokenGeneratorBuilderTest {

    @Test
    public void assertGetSQLTokenGenerators() {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getTablesContext().getSchemaName().isPresent()).thenReturn(false);
        DefaultTokenGeneratorBuilder selectBuilder = new DefaultTokenGeneratorBuilder(selectStatementContext);
        Collection<SQLTokenGenerator> selectSqlTokenGenerators = selectBuilder.getSQLTokenGenerators();
        assertThat(selectSqlTokenGenerators.size(), is(0));
        ShowColumnsStatementContext showColumnsStatementContext = mock(ShowColumnsStatementContext.class, RETURNS_DEEP_STUBS);
        DefaultTokenGeneratorBuilder showColumnsBuilder = new DefaultTokenGeneratorBuilder(showColumnsStatementContext);
        Collection<SQLTokenGenerator> showColumnsSqlTokenGenerators = showColumnsBuilder.getSQLTokenGenerators();
        assertThat(showColumnsSqlTokenGenerators.size(), is(1));
        Iterator<SQLTokenGenerator> iterator = showColumnsSqlTokenGenerators.iterator();
        SQLTokenGenerator removeTokenGenerator = iterator.next();
        assertThat(removeTokenGenerator, instanceOf(RemoveTokenGenerator.class));
    }
}
