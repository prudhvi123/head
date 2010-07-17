/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 * explanation of the license and how it is applied.
 */

package org.mifos.application.admin.servicefacade;

import java.util.List;

import org.mifos.dto.domain.OfficeLevelDto;
import org.mifos.dto.domain.UpdateConfiguredOfficeLevelRequest;
import org.mifos.dto.screen.ConfigureApplicationLabelsDto;
import org.mifos.dto.screen.ProductConfigurationDto;
import org.mifos.dto.screen.ProductDisplayDto;
import org.mifos.dto.screen.ProductDto;
import org.mifos.dto.screen.ProductMixDetailsDto;
import org.springframework.security.access.prepost.PreAuthorize;

@SuppressWarnings("PMD.SignatureDeclareThrowsException")
public interface AdminServiceFacade {

    @PreAuthorize("isFullyAuthenticated()")
    ProductConfigurationDto retrieveProductConfiguration();

    @PreAuthorize("isFullyAuthenticated() and hasRole('ROLE_UPDATE_LATENESS_DORMANCY')")
    void updateProductConfiguration(ProductConfigurationDto productConfiguration);

    @PreAuthorize("isFullyAuthenticated()")
    OfficeLevelDto retrieveOfficeLevelsWithConfiguration();

    @PreAuthorize("isFullyAuthenticated()")
    void updateOfficeLevelHierarchies(UpdateConfiguredOfficeLevelRequest updateRequest);

    @PreAuthorize("isFullyAuthenticated()")
    List<ProductDisplayDto> retrieveLoanProducts();

    @PreAuthorize("isFullyAuthenticated()")
    List<ProductDisplayDto> retrieveSavingsProducts();

    @PreAuthorize("isFullyAuthenticated()")
    ProductMixDetailsDto retrieveProductMixDetails(Short prdOfferingId, String productType) throws Exception;

    @PreAuthorize("isFullyAuthenticated()")
    ProductDto retrieveAllProductMix() throws Exception;

    ConfigureApplicationLabelsDto retrieveConfigurableLabels();

    void updateApplicationLabels(ConfigureApplicationLabelsDto applicationLabels);
}