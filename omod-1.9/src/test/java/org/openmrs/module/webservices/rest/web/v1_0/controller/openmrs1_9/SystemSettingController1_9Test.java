/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.webservices.rest.web.v1_0.controller.openmrs1_9;



import org.apache.commons.beanutils.PropertyUtils;
import org.codehaus.jackson.map.ObjectMapper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.RestTestConstants1_9;
import org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Tests CRUD operations for {@link GlobalProperty}s via web service calls
 */
public class SystemSettingController1_9Test extends MainResourceControllerTest {
    
        private AdministrationService service;
        
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getURI()
	 */
	@Override
	public String getURI() {
		return "systemsetting";
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#getUuid()
	 */
	@Override
	public String getUuid() {
		return RestTestConstants1_9.GLOBAL_PROPERTY_UUID;
	}
        
        @Override
        public long getAllCount() {
            return 4;
        }
        
        @Before
	public void before() throws Exception {
            this.service = Context.getAdministrationService();
	}
	
	/**
	 * @see org.openmrs.module.webservices.rest.web.v1_0.controller.MainResourceControllerTest#shouldGetAll()
	 */
	@Override
	@Test
	public void shouldGetAll() throws Exception {
		super.shouldGetAll();
	}
        
	@Test
	public void shouldSaveSystemSettingWithCustomDatatype() throws Exception {
		SimpleObject property = new SimpleObject();
		property.add("property", "a.property.test");
		property.add("description", "Testing post operation of global property");
                property.add("datatypeClassname", "org.openmrs.customdatatype.datatype.BooleanDatatype");
                property.add("datatypeConfig", null);
                property.add("value", "false");
		String json = new ObjectMapper().writeValueAsString(property);
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
                
		SimpleObject newlyCreatedSetting = deserialize(handle(req));
                String uuid = (String) PropertyUtils.getProperty(newlyCreatedSetting, "uuid");
                
                MockHttpServletRequest getReq = request(RequestMethod.GET, getURI() + "/"+uuid);
                getReq.addParameter(RestConstants.REQUEST_PROPERTY_FOR_REPRESENTATION, RestConstants.REPRESENTATION_FULL);
		SimpleObject result = deserialize(handle(getReq));
		assertEquals("a.property.test", PropertyUtils.getProperty(result, "property"));
                assertEquals("false", PropertyUtils.getProperty(result, "value"));
                assertEquals("org.openmrs.customdatatype.datatype.BooleanDatatype", PropertyUtils.getProperty(result, "datatypeClassname"));
                assertNull(PropertyUtils.getProperty(result, "datatypeConfig"));
	}
        
        @Test
	public void shouldSaveSystemSettingWithoutCustomDatatype() throws Exception {
		SimpleObject property = new SimpleObject();
		property.add("property", "a.property.test");
		property.add("description", "Testing post operation of global property");
                property.add("value", "Saving property value without custome datatype");
		String json = new ObjectMapper().writeValueAsString(property);
		MockHttpServletRequest req = request(RequestMethod.POST, getURI());
		req.setContent(json.getBytes());
                
		SimpleObject result = deserialize(handle(req));
                assertEquals("a.property.test", PropertyUtils.getProperty(result, "property"));
                assertEquals("Saving property value without custome datatype", PropertyUtils.getProperty(result, "value"));
	}
        
        @Test
	public void shouldFindASystemSettingWithUUID() throws Exception {
                SimpleObject property = deserialize(handle(newGetRequest(getURI() + "/" + getUuid())));
		
                GlobalProperty expectedProperty = service.getGlobalPropertyByUuid(getUuid());
		assertNotNull(property);
                assertEquals(expectedProperty.getUuid(), PropertyUtils.getProperty(property, "uuid"));
		assertEquals(expectedProperty.getProperty(), PropertyUtils.getProperty(property, "property"));
                assertEquals(expectedProperty.getValue(), PropertyUtils.getProperty(property, "value"));
	}
        
        @Test
	public void shouldEditASystemSetting() throws Exception {
		final String newValue = "Adding description by editing property";
		GlobalProperty expectedProperty = service.getGlobalPropertyByUuid(getUuid());
                assertNull(expectedProperty.getDescription());
		String json = "{ \"description\":\""+newValue+"\" }";
                
		SimpleObject updatedProperty = deserialize(handle(newPostRequest(getURI() + "/" + getUuid(), json)));
		assertTrue(newValue.equals(PropertyUtils.getProperty(updatedProperty, "description")));
	}
        
        @Test
	public void shouldPurgeASystemSetting() throws Exception {
		assertNotNull(service.getGlobalPropertyByUuid(getUuid()));
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		req.addParameter("purge", "");
		handle(req);
		assertNull(service.getGlobalPropertyByUuid(getUuid()));
	}
        
        @Test
	public void shouldDeleteASystemSetting() throws Exception {
		assertNotNull(service.getGlobalPropertyByUuid(getUuid()));
		MockHttpServletRequest req = request(RequestMethod.DELETE, getURI() + "/" + getUuid());
		handle(req);
		assertNull(service.getGlobalPropertyByUuid(getUuid()));
	}
}
