/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.metamodel.facets.actions.action;

import java.lang.reflect.Method;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.metamodel.facetapi.Facet;
import org.apache.isis.metamodel.facets.AbstractFacetFactoryTest;
import org.apache.isis.metamodel.facets.FacetFactory.ProcessMethodContext;
import org.apache.isis.metamodel.facets.actions.action.semantics.ActionSemanticsFacetFallbackToNonIdempotent;
import org.apache.isis.metamodel.facets.actions.semantics.ActionSemanticsFacet;

import lombok.val;

public class ActionSemanticsFacetFallbackToNonIdempotentFactoryTest extends AbstractFacetFactoryTest {

    private ActionAnnotationFacetFactory facetFactory;
    
    private void processSemantics(
            ActionAnnotationFacetFactory facetFactory, ProcessMethodContext processMethodContext) {
        val actionIfAny = processMethodContext.synthesizeOnMethod(Action.class);
        facetFactory.processSemantics(processMethodContext, actionIfAny);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        facetFactory = new ActionAnnotationFacetFactory();
    }

    @Override
    protected void tearDown() throws Exception {
        facetFactory = null;
        super.tearDown();
    }

    public void testNoAnnotationPickedUp() {
        class Customer {
            @SuppressWarnings("unused")
            public void someAction() {
            }
        }
        final Method actionMethod = findMethod(Customer.class, "someAction");

        processSemantics(facetFactory, new ProcessMethodContext(Customer.class, null, actionMethod, methodRemover, facetedMethod));

        final Facet facet = facetedMethod.getFacet(ActionSemanticsFacet.class);
        assertNotNull(facet);
        assertTrue(facet instanceof ActionSemanticsFacetFallbackToNonIdempotent);

        assertNoMethodsRemoved();
    }

}