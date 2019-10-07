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
package org.apache.isis.metamodel.facets.object.mixin;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.util.ReflectionUtils;

import org.apache.isis.applib.annotation.Action;
import org.apache.isis.applib.annotation.DomainObject;
import org.apache.isis.applib.annotation.Mixin;
import org.apache.isis.metamodel.MetaModelContext;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.metamodel.facets.object.mixin.MixinFacet.Policy;
import org.apache.isis.metamodel.spec.ManagedObject;
import org.apache.isis.metamodel.spec.ObjectSpecId;
import org.apache.isis.metamodel.specloader.specimpl.IntrospectionState;
import org.apache.isis.runtime.persistence.adapter.PojoAdapter;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.val;

@SuppressWarnings("unused")
class MixinIntendedAsActionTest extends MixinIntendedAs {

    @BeforeEach
    void beforeEach() throws Exception {
        super.setUp();
    }

    @AfterEach
    void afterEach() throws Exception {
        super.tearDown();
    }

    // the non holder
    @DomainObject
    static class NoCustomer {
        
    }
    
    // the (shared) holder
    @DomainObject @Data
    static class Customer {
        String name;
    }
    
    // ------------------------------
    // -- classic mix-in declaration
    // ------------------------------
    
    @Mixin @RequiredArgsConstructor 
    static class Customer_mixin {
        
        private final Customer holder;
        
        @Action
        public void $$(String newName) { holder.setName(newName); }
    }
    
    @Test
    void classicMixin_shouldHaveProperFacet() {

        val mixinFacet = super.runTypeContextOn(Customer_mixin.class)
                .getFacet(MixinFacet.class);
        
        // proper predicates
        assertNotNull(mixinFacet);
        assertTrue(mixinFacet.isMixinFor(Customer.class));
        assertFalse(mixinFacet.isMixinFor(NoCustomer.class));
        
        // proper instantiation
        val holderPojo = new Customer();
        val mixinPojo = mixinFacet.instantiate(holderPojo);
        ((Customer_mixin)mixinPojo).$$("hello");
        assertEquals("hello", holderPojo.getName());
        
    }
    
    // ------------------------------------------
    // -- advanced mix-in declaration ... @Action
    // ------------------------------------------
    
    @Action @RequiredArgsConstructor 
    static class Customer_action {
        
        private final Customer holder;

        @Action // <-- TODO optional?
        public void $$(String newName) { holder.setName(newName); }
    }
    
    @Test @Disabled("under construction")
    void actionMixin_shouldHaveProperFacet() {

        val mixinFacet = super.runTypeContextOn(Customer_action.class)
                .getFacet(MixinFacet.class);
        
        // proper predicates
        assertNotNull(mixinFacet);
        assertTrue(mixinFacet.isMixinFor(Customer.class));
        assertFalse(mixinFacet.isMixinFor(NoCustomer.class));
        
        // proper instantiation
        val holderPojo = new Customer();
        val mixinPojo = mixinFacet.instantiate(holderPojo);
        ((Customer_action)mixinPojo).$$("hello");
        assertEquals("hello", holderPojo.getName());
    }




}