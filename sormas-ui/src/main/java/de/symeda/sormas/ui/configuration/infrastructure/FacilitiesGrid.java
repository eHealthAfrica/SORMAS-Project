/*******************************************************************************
 * SORMAS® - Surveillance Outbreak Response Management & Analysis System
 * Copyright © 2016-2018 Helmholtz-Zentrum für Infektionsforschung GmbH (HZI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui.configuration.infrastructure;

import java.util.ArrayList;
import java.util.List;

import com.vaadin.data.Container.Filter;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.GeneratedPropertyContainer;
import com.vaadin.data.util.PropertyValueGenerator;
import com.vaadin.data.util.filter.Or;
import com.vaadin.data.util.filter.SimpleStringFilter;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.Grid;
import com.vaadin.ui.renderers.HtmlRenderer;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.facility.FacilityCriteria;
import de.symeda.sormas.api.facility.FacilityDto;
import de.symeda.sormas.api.facility.FacilityType;
import de.symeda.sormas.api.region.CommunityReferenceDto;
import de.symeda.sormas.api.region.DistrictReferenceDto;
import de.symeda.sormas.api.region.RegionReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.CurrentUser;

public class FacilitiesGrid extends Grid {

	private static final long serialVersionUID = 4488941182432777837L;

	public static final String EDIT_BTN_ID = "edit";

	private FacilityCriteria facilityCriteria = new FacilityCriteria();

	public FacilitiesGrid() {
		setSizeFull();
		setSelectionMode(SelectionMode.NONE);

		// Hides "Other facility" and "Home or other place"
		facilityCriteria.excludeStaticFacilitesEquals(true);

		BeanItemContainer<FacilityDto> container = new BeanItemContainer<FacilityDto>(FacilityDto.class);
		GeneratedPropertyContainer generatedContainer = new GeneratedPropertyContainer(container);

		if (CurrentUser.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_EDIT)) {
			generatedContainer.addGeneratedProperty(EDIT_BTN_ID, new PropertyValueGenerator<String>() {
				private static final long serialVersionUID = -7255691609662228895L;

				@Override
				public String getValue(Item item, Object itemId, Object propertyId) {
					return FontAwesome.PENCIL_SQUARE.getHtml();
				}

				@Override
				public Class<String> getType() {
					return String.class;
				}
			});
		}

		setContainerDataSource(generatedContainer);
		setColumns(FacilityDto.NAME, FacilityDto.REGION, FacilityDto.DISTRICT, FacilityDto.COMMUNITY, FacilityDto.CITY,
				FacilityDto.LATITUDE, FacilityDto.LONGITUDE);

		for (Column column : getColumns()) {
			column.setHeaderCaption(I18nProperties.getPrefixCaption(FacilityDto.I18N_PREFIX,
					column.getPropertyId().toString(), column.getHeaderCaption()));
		}

		if (CurrentUser.getCurrent().hasUserRight(UserRight.INFRASTRUCTURE_EDIT)) {
			addColumn(EDIT_BTN_ID);
			getColumn(EDIT_BTN_ID).setRenderer(new HtmlRenderer());
			getColumn(EDIT_BTN_ID).setWidth(40);

			addItemClickListener(e -> {
				if (e.getPropertyId() != null && (e.getPropertyId().equals(EDIT_BTN_ID) || e.isDoubleClick())) {
					ControllerProvider.getInfrastructureController()
							.editHealthFacility(((FacilityDto) e.getItemId()).getUuid());
				}
			});
		}
	}

	@SuppressWarnings("unchecked")
	public BeanItemContainer<FacilityDto> getContainer() {
		GeneratedPropertyContainer container = (GeneratedPropertyContainer) getContainerDataSource();
		return (BeanItemContainer<FacilityDto>) container.getWrappedContainer();
	}

	public void reload() {
		getContainer().removeAllItems();

		List<FacilityDto> facilities = FacadeProvider.getFacilityFacade()
				.getIndexList(CurrentUser.getCurrent().getUuid(), facilityCriteria);

		getContainer().addAll(facilities);
	}

	public void setRegionFilter(RegionReferenceDto region) {
		facilityCriteria.regionEquals(region);
		reload();
	}

	public void setDistrictFilter(DistrictReferenceDto district) {
		facilityCriteria.districtEquals(district);
		reload();
	}

	public void setCommunityFilter(CommunityReferenceDto community) {
		facilityCriteria.communityEquals(community);
		reload();
	}

	public void setTypeFilter(boolean showLaboratories) {
		if (showLaboratories) {
			facilityCriteria.typeEquals(FacilityType.LABORATORY);
		} else {
			// TODO: Workaround; only works because normal health facilities currently don't
			// have a type
			facilityCriteria.typeEquals(null);
		}
	}

	public void filterByText(String text) {
		getContainer().removeContainerFilters(FacilityDto.NAME);
		getContainer().removeContainerFilters(FacilityDto.CITY);

		if (text != null && !text.isEmpty()) {
			List<Filter> orFilters = new ArrayList<Filter>();
			String[] words = text.split("\\s+");
			for (String word : words) {
				orFilters.add(new SimpleStringFilter(FacilityDto.NAME, word, true, false));
				orFilters.add(new SimpleStringFilter(FacilityDto.CITY, word, true, false));
			}
			getContainer().addContainerFilter(new Or(orFilters.stream().toArray(Filter[]::new)));
		}
	}

}
