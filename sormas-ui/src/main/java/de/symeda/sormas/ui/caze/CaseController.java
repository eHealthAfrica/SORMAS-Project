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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 *******************************************************************************/
package de.symeda.sormas.ui.caze;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.vaadin.navigator.Navigator;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.Sizeable;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.Notification.Type;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;
import com.vaadin.ui.themes.ValoTheme;

import de.symeda.sormas.api.CountryHelper;
import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.DiseaseHelper;
import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.caze.CaseBulkEditData;
import de.symeda.sormas.api.caze.CaseCriteria;
import de.symeda.sormas.api.caze.CaseDataDto;
import de.symeda.sormas.api.caze.CaseFacade;
import de.symeda.sormas.api.caze.CaseIndexDto;
import de.symeda.sormas.api.caze.CaseLogic;
import de.symeda.sormas.api.caze.CaseOrigin;
import de.symeda.sormas.api.caze.CaseSimilarityCriteria;
import de.symeda.sormas.api.caze.classification.ClassificationHtmlRenderer;
import de.symeda.sormas.api.caze.classification.DiseaseClassificationCriteriaDto;
import de.symeda.sormas.api.contact.ContactClassification;
import de.symeda.sormas.api.contact.ContactDto;
import de.symeda.sormas.api.contact.ContactLogic;
import de.symeda.sormas.api.contact.ContactSimilarityCriteria;
import de.symeda.sormas.api.contact.ContactStatus;
import de.symeda.sormas.api.contact.SimilarContactDto;
import de.symeda.sormas.api.event.EventDto;
import de.symeda.sormas.api.event.EventParticipantCriteria;
import de.symeda.sormas.api.event.EventParticipantDto;
import de.symeda.sormas.api.event.SimilarEventParticipantDto;
import de.symeda.sormas.api.externalsurveillancetool.ExternalSurveillanceToolException;
import de.symeda.sormas.api.feature.FeatureType;
import de.symeda.sormas.api.i18n.Captions;
import de.symeda.sormas.api.i18n.I18nProperties;
import de.symeda.sormas.api.i18n.Strings;
import de.symeda.sormas.api.infrastructure.district.DistrictReferenceDto;
import de.symeda.sormas.api.infrastructure.facility.FacilityDto;
import de.symeda.sormas.api.infrastructure.pointofentry.PointOfEntryDto;
import de.symeda.sormas.api.infrastructure.pointofentry.PointOfEntryReferenceDto;
import de.symeda.sormas.api.infrastructure.region.RegionReferenceDto;
import de.symeda.sormas.api.messaging.MessageType;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.api.person.PersonHelper;
import de.symeda.sormas.api.symptoms.SymptomsContext;
import de.symeda.sormas.api.symptoms.SymptomsDto;
import de.symeda.sormas.api.symptoms.SymptomsHelper;
import de.symeda.sormas.api.travelentry.TravelEntryDto;
import de.symeda.sormas.api.user.UserDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.user.UserRight;
import de.symeda.sormas.api.user.UserRole;
import de.symeda.sormas.api.utils.DataHelper;
import de.symeda.sormas.api.utils.DateHelper;
import de.symeda.sormas.api.utils.HtmlHelper;
import de.symeda.sormas.api.utils.ValidationRuntimeException;
import de.symeda.sormas.api.utils.YesNoUnknown;
import de.symeda.sormas.api.utils.fieldaccess.UiFieldAccessCheckers;
import de.symeda.sormas.api.visit.VisitDto;
import de.symeda.sormas.ui.ControllerProvider;
import de.symeda.sormas.ui.SormasUI;
import de.symeda.sormas.ui.UserProvider;
import de.symeda.sormas.ui.ViewModelProviders;
import de.symeda.sormas.ui.caze.components.linelisting.LineListingLayout;
import de.symeda.sormas.ui.caze.maternalhistory.MaternalHistoryForm;
import de.symeda.sormas.ui.caze.maternalhistory.MaternalHistoryView;
import de.symeda.sormas.ui.caze.messaging.SmsComponent;
import de.symeda.sormas.ui.caze.porthealthinfo.PortHealthInfoForm;
import de.symeda.sormas.ui.caze.porthealthinfo.PortHealthInfoView;
import de.symeda.sormas.ui.clinicalcourse.ClinicalCourseForm;
import de.symeda.sormas.ui.clinicalcourse.ClinicalCourseView;
import de.symeda.sormas.ui.epidata.CaseEpiDataView;
import de.symeda.sormas.ui.epidata.EpiDataForm;
import de.symeda.sormas.ui.externalsurveillanceservice.ExternalSurveillanceServiceGateway;
import de.symeda.sormas.ui.hospitalization.HospitalizationForm;
import de.symeda.sormas.ui.hospitalization.HospitalizationView;
import de.symeda.sormas.ui.symptoms.SymptomsForm;
import de.symeda.sormas.ui.therapy.TherapyView;
import de.symeda.sormas.ui.utils.AbstractView;
import de.symeda.sormas.ui.utils.ButtonHelper;
import de.symeda.sormas.ui.utils.CommitDiscardWrapperComponent;
import de.symeda.sormas.ui.utils.CssStyles;
import de.symeda.sormas.ui.utils.DateHelper8;
import de.symeda.sormas.ui.utils.VaadinUiUtil;
import de.symeda.sormas.ui.utils.ViewMode;

public class CaseController {

	public CaseController() {

	}

	public void registerViews(Navigator navigator) {

		UserProvider userProvider = UserProvider.getCurrent();
		navigator.addView(CasesView.VIEW_NAME, CasesView.class);
		if (userProvider.hasUserRight(UserRight.CASE_MERGE)) {
			navigator.addView(MergeCasesView.VIEW_NAME, MergeCasesView.class);
		}
		navigator.addView(CaseDataView.VIEW_NAME, CaseDataView.class);
		navigator.addView(CasePersonView.VIEW_NAME, CasePersonView.class);
		navigator.addView(MaternalHistoryView.VIEW_NAME, MaternalHistoryView.class);
		if (userProvider.hasUserRight(UserRight.PORT_HEALTH_INFO_VIEW)) {
			navigator.addView(PortHealthInfoView.VIEW_NAME, PortHealthInfoView.class);
		}
		navigator.addView(CaseSymptomsView.VIEW_NAME, CaseSymptomsView.class);
		if (userProvider.hasUserRight(UserRight.CONTACT_VIEW)) {
			navigator.addView(CaseContactsView.VIEW_NAME, CaseContactsView.class);
		}
		navigator.addView(HospitalizationView.VIEW_NAME, HospitalizationView.class);
		navigator.addView(CaseEpiDataView.VIEW_NAME, CaseEpiDataView.class);
		if (userProvider.hasUserRight(UserRight.THERAPY_VIEW)) {
			navigator.addView(TherapyView.VIEW_NAME, TherapyView.class);
		}
		if (userProvider.hasUserRight(UserRight.CLINICAL_COURSE_VIEW)) {
			navigator.addView(ClinicalCourseView.VIEW_NAME, ClinicalCourseView.class);
		}
		if (FacadeProvider.getFeatureConfigurationFacade().isFeatureEnabled(FeatureType.CASE_FOLLOWUP)) {
			navigator.addView(CaseVisitsView.VIEW_NAME, CaseVisitsView.class);
		}
	}

	public void create() {
		CommitDiscardWrapperComponent<CaseCreateForm> caseCreateComponent = getCaseCreateComponent(null, null, null, null, false);
		VaadinUiUtil.showModalPopupWindow(caseCreateComponent, I18nProperties.getString(Strings.headingCreateNewCase));
	}

	public void createFromEventParticipant(EventParticipantDto eventParticipant) {
		EventDto event = FacadeProvider.getEventFacade().getEventByUuid(eventParticipant.getEvent().getUuid(), false);
		if (event.getDisease() == null) {
			new Notification(
				I18nProperties.getString(Strings.headingCreateNewCaseIssue),
				I18nProperties.getString(Strings.messageEventParticipantToCaseWithoutEventDisease),
				Notification.Type.ERROR_MESSAGE,
				false).show(Page.getCurrent());
			return;
		}

		CommitDiscardWrapperComponent<CaseCreateForm> caseCreateComponent = getCaseCreateComponent(null, eventParticipant, null, null, false);
		caseCreateComponent.addCommitListener(() -> {
			EventParticipantDto updatedEventparticipant = FacadeProvider.getEventParticipantFacade().getByUuid(eventParticipant.getUuid());
			if (updatedEventparticipant.getResultingCase() != null) {
				String caseUuid = updatedEventparticipant.getResultingCase().getUuid();
				CaseDataDto caze = FacadeProvider.getCaseFacade().getCaseDataByUuid(caseUuid);
				Date relevantDate = event.getStartDate() != null
					? event.getStartDate()
					: (event.getEndDate() != null ? event.getEndDate() : event.getReportDateTime());
				convertSamePersonContactsAndEventparticipants(caze, relevantDate);
			}
		});
		VaadinUiUtil.showModalPopupWindow(caseCreateComponent, I18nProperties.getString(Strings.headingCreateNewCase));
	}

	public void createFromEventParticipantDifferentDisease(EventParticipantDto eventParticipant, Disease disease) {
		if (disease == null) {
			new Notification(
				I18nProperties.getString(Strings.headingCreateNewCaseIssue),
				I18nProperties.getString(Strings.messageEventParticipantToCaseWithoutEventDisease),
				Notification.Type.ERROR_MESSAGE,
				false).show(Page.getCurrent());
			return;
		}

		CommitDiscardWrapperComponent<CaseCreateForm> caseCreateComponent = getCaseCreateComponent(null, eventParticipant, null, disease, false);
		VaadinUiUtil.showModalPopupWindow(caseCreateComponent, I18nProperties.getString(Strings.headingCreateNewCase));
	}

	public void createFromContact(ContactDto contact) {
		PersonDto selectedPerson = FacadeProvider.getPersonFacade().getPersonByUuid(contact.getPerson().getUuid());
		CaseDataDto dto = CaseDataDto.build(PersonDto.build().toReference(), contact.getDisease());

		dto.setDiseaseDetails(contact.getDiseaseDetails());
		dto.setRegion(contact.getRegion());
		dto.setDistrict(contact.getDistrict());
		dto.setReportDate(contact.getReportDateTime());
		dto.setCommunity(contact.getCommunity());
		dto.setReportingUser(UserProvider.getCurrent().getUserReference());

		selectOrCreateCase(dto, FacadeProvider.getPersonFacade().getPersonByUuid(selectedPerson.getUuid()), uuid -> {
			if (uuid == null) {
				CommitDiscardWrapperComponent<CaseCreateForm> caseCreateComponent = getCaseCreateComponent(contact, null, null, null, false);
				caseCreateComponent.addCommitListener(() -> {
					if (contact.getResultingCase() != null) {
						String caseUuid = contact.getResultingCase().getUuid();
						CaseDataDto caze = FacadeProvider.getCaseFacade().getCaseDataByUuid(caseUuid);
						convertSamePersonContactsAndEventparticipants(
							caze,
							ContactLogic.getStartDate(contact.getLastContactDate(), contact.getReportDateTime()));
					}
				});
				VaadinUiUtil.showModalPopupWindow(caseCreateComponent, I18nProperties.getString(Strings.headingCreateNewCase));
			} else {
				CaseDataDto selectedCase = FacadeProvider.getCaseFacade().getCaseDataByUuid(uuid);
				selectedCase.getEpiData().setContactWithSourceCaseKnown(YesNoUnknown.YES);
				FacadeProvider.getCaseFacade().saveCase(selectedCase);

				ContactDto updatedContact = FacadeProvider.getContactFacade().getContactByUuid(contact.getUuid());
				updatedContact.setContactStatus(ContactStatus.CONVERTED);
				updatedContact.setResultingCase(selectedCase.toReference());
				updatedContact.setResultingCaseUser(UserProvider.getCurrent().getUserReference());
				FacadeProvider.getContactFacade().saveContact(updatedContact);

				convertSamePersonContactsAndEventparticipants(
					selectedCase,
					ContactLogic.getStartDate(updatedContact.getLastContactDate(), updatedContact.getReportDateTime()));

				navigateToView(CaseDataView.VIEW_NAME, selectedCase.getUuid(), null);
			}
		});
	}

	public void createFromUnrelatedContact(ContactDto contact, Disease disease) {
		CommitDiscardWrapperComponent<CaseCreateForm> caseCreateComponent = getCaseCreateComponent(contact, null, null, disease, false);
		VaadinUiUtil.showModalPopupWindow(caseCreateComponent, I18nProperties.getString(Strings.headingCreateNewCase));
	}

	public void createFromTravelEntry(TravelEntryDto travelEntryDto) {
		PersonDto selectedPerson = FacadeProvider.getPersonFacade().getPersonByUuid(travelEntryDto.getPerson().getUuid());
		CaseDataDto dto = CaseDataDto.buildFromTravelEntry(travelEntryDto, selectedPerson);

		dto.setReportingUser(UserProvider.getCurrent().getUserReference());

		selectOrCreateCase(dto, FacadeProvider.getPersonFacade().getPersonByUuid(selectedPerson.getUuid()), uuid -> {
			if (uuid == null) {
				CommitDiscardWrapperComponent<CaseCreateForm> caseCreateComponent = getCaseCreateComponent(null, null, travelEntryDto, null, false);
				VaadinUiUtil.showModalPopupWindow(caseCreateComponent, I18nProperties.getString(Strings.headingCreateNewCase));
			} else {
				TravelEntryDto updatedTravelEntry = FacadeProvider.getTravelEntryFacade().getByUuid(travelEntryDto.getUuid());
				updatedTravelEntry.setResultingCase(FacadeProvider.getCaseFacade().getCaseDataByUuid(uuid).toReference());
				FacadeProvider.getTravelEntryFacade().save(updatedTravelEntry);
				navigateToView(CaseDataView.VIEW_NAME, uuid, null);
			}
		});
	}

	private void convertSamePersonContactsAndEventparticipants(CaseDataDto caze, Date relevantDate) {

		ContactSimilarityCriteria contactCriteria = new ContactSimilarityCriteria().withPerson(caze.getPerson())
			.withDisease(caze.getDisease())
			.withContactClassification(ContactClassification.CONFIRMED)
			.withExcludePseudonymized(true)
			.withNoResultingCase(true);
		List<SimilarContactDto> matchingContacts = FacadeProvider.getContactFacade().getMatchingContacts(contactCriteria);

		EventParticipantCriteria eventParticipantCriteria = new EventParticipantCriteria().withPerson(caze.getPerson())
			.withDisease(caze.getDisease())
			.withExcludePseudonymized(true)
			.withNoResultingCase(true);
		List<SimilarEventParticipantDto> matchingEventParticipants =
			FacadeProvider.getEventParticipantFacade().getMatchingEventParticipants(eventParticipantCriteria);

		if (matchingContacts.size() > 0 || matchingEventParticipants.size() > 0) {
			String infoText = matchingEventParticipants.isEmpty()
				? String.format(I18nProperties.getString(Strings.infoConvertToCaseContacts), matchingContacts.size(), caze.getDisease())
				: (matchingContacts.isEmpty()
					? String.format(
						I18nProperties.getString(Strings.infoConvertToCaseEventParticipants),
						matchingEventParticipants.size(),
						caze.getDisease())
					: String.format(
						I18nProperties.getString(Strings.infoConvertToCaseContactsAndEventParticipants),
						matchingContacts.size(),
						caze.getDisease(),
						matchingEventParticipants.size(),
						caze.getDisease()));

			HorizontalLayout infoComponent = VaadinUiUtil.createInfoComponent(infoText);
			infoComponent.setWidth(600, Sizeable.Unit.PIXELS);
			CommitDiscardWrapperComponent<HorizontalLayout> convertToCaseConfirmComponent = new CommitDiscardWrapperComponent<>(infoComponent);
			convertToCaseConfirmComponent.getCommitButton().setCaption(I18nProperties.getCaption(Captions.actionYesForAll));
			convertToCaseConfirmComponent.getDiscardButton().setCaption(I18nProperties.getCaption(Captions.actionNo));

			convertToCaseConfirmComponent.addCommitListener(() -> {
				caze.getEpiData().setContactWithSourceCaseKnown(YesNoUnknown.YES);
				saveCase(caze);
				setResultingCase(caze, matchingContacts, matchingEventParticipants);
				SormasUI.refreshView();
			});

			Button convertSomeButton =
				ButtonHelper.createButton("convertSome", I18nProperties.getCaption(Captions.actionYesForSome), (Button.ClickListener) event -> {
					convertToCaseConfirmComponent.discard();
					showConvertToCaseSelection(caze, matchingContacts, matchingEventParticipants);
				}, ValoTheme.BUTTON_PRIMARY);

			HorizontalLayout buttonsPanel = convertToCaseConfirmComponent.getButtonsPanel();
			buttonsPanel.addComponent(convertSomeButton, convertToCaseConfirmComponent.getComponentCount() - 1);
			buttonsPanel.setComponentAlignment(convertSomeButton, Alignment.BOTTOM_RIGHT);
			buttonsPanel.setExpandRatio(convertSomeButton, 0);

			VaadinUiUtil.showModalPopupWindow(convertToCaseConfirmComponent, I18nProperties.getString(Strings.headingCaseConversion));
		}
	}

	private void showConvertToCaseSelection(
		CaseDataDto caze,
		List<SimilarContactDto> matchingContacts,
		List<SimilarEventParticipantDto> matchingEventParticipants) {

		ConvertToCaseSelectionField convertToCaseSelectionField = new ConvertToCaseSelectionField(caze, matchingContacts, matchingEventParticipants);
		convertToCaseSelectionField.setWidth(1280, Sizeable.Unit.PIXELS);

		CommitDiscardWrapperComponent<ConvertToCaseSelectionField> convertToCaseSelectComponent =
			new CommitDiscardWrapperComponent<>(convertToCaseSelectionField);
		convertToCaseSelectComponent.getCommitButton().setCaption(I18nProperties.getCaption(Captions.actionConfirm));
		convertToCaseSelectComponent.getDiscardButton().setCaption(I18nProperties.getCaption(Captions.actionCancel));

		convertToCaseSelectComponent.addCommitListener(() -> {
			List<SimilarContactDto> selectedContacts = convertToCaseSelectionField.getSelectedContacts();
			if (!selectedContacts.isEmpty()) {
				caze.getEpiData().setContactWithSourceCaseKnown(YesNoUnknown.YES);
			}
			saveCase(caze);
			setResultingCase(caze, selectedContacts, convertToCaseSelectionField.getSelectedEventParticipants());
			SormasUI.refreshView();
		});

		VaadinUiUtil.showModalPopupWindow(convertToCaseSelectComponent, I18nProperties.getString(Strings.headingCaseConversion));
	}

	private void setResultingCase(
		CaseDataDto caze,
		List<SimilarContactDto> matchingContacts,
		List<SimilarEventParticipantDto> matchingEventParticipants) {

		if (matchingContacts != null && !matchingContacts.isEmpty()) {
			List<String> contactUuids = matchingContacts.stream().map(SimilarContactDto::getUuid).collect(Collectors.toList());
			List<ContactDto> contacts = FacadeProvider.getContactFacade().getByUuids(contactUuids);
			for (ContactDto contact : contacts) {
				contact.setContactStatus(ContactStatus.CONVERTED);
				contact.setResultingCase(caze.toReference());
				contact.setResultingCaseUser(UserProvider.getCurrent().getUserReference());
				FacadeProvider.getContactFacade().saveContact(contact);
			}
		}

		if (matchingEventParticipants != null && !matchingEventParticipants.isEmpty()) {
			List<String> eventParticipantUuids =
				matchingEventParticipants.stream().map(SimilarEventParticipantDto::getUuid).collect(Collectors.toList());
			List<EventParticipantDto> eventParticipants = FacadeProvider.getEventParticipantFacade().getByUuids(eventParticipantUuids);
			for (EventParticipantDto eventParticipant : eventParticipants) {
				eventParticipant.setResultingCase(caze.toReference());
				FacadeProvider.getEventParticipantFacade().saveEventParticipant(eventParticipant);
			}
		}

	}

	public void navigateToIndex() {
		String navigationState = CasesView.VIEW_NAME;
		SormasUI.get().getNavigator().navigateTo(navigationState);
	}

	public void navigateTo(CaseCriteria caseCriteria) {
		ViewModelProviders.of(CasesView.class).remove(CaseCriteria.class);
		String navigationState = AbstractView.buildNavigationState(CasesView.VIEW_NAME, caseCriteria);
		SormasUI.get().getNavigator().navigateTo(navigationState);
	}

	public void navigateToMergeCasesView() {
		String navigationState = MergeCasesView.VIEW_NAME;
		SormasUI.get().getNavigator().navigateTo(navigationState);
	}

	public void navigateToCase(String caseUuid) {
		navigateToView(CaseDataView.VIEW_NAME, caseUuid, null, false);
	}

	public void navigateToCase(String caseUuid, boolean openTab) {
		navigateToView(CaseDataView.VIEW_NAME, caseUuid, null, openTab);
	}

	public void navigateToView(String viewName, String caseUuid, ViewMode viewMode) {
		navigateToView(viewName, caseUuid, viewMode, false);
	}

	public void navigateToView(String viewName, String caseUuid, ViewMode viewMode, boolean openTab) {

		String navigationState = viewName + "/" + caseUuid;
		if (viewMode == ViewMode.NORMAL) {
			// pass full view mode as param so it's also used for other views when switching
			navigationState += "?" + AbstractCaseView.VIEW_MODE_URL_PREFIX + "=" + viewMode.toString();
		}

		if (openTab) {
			SormasUI.get().getPage().open(SormasUI.get().getPage().getLocation().getRawPath() + "#!" + navigationState, "_blank", false);
		} else {
			SormasUI.get().getNavigator().navigateTo(navigationState);
		}
	}

	public Link createLinkToData(String caseUuid, String caption) {
		return new Link(caption, new ExternalResource("#!" + CaseDataView.VIEW_NAME + "/" + caseUuid));
	}

	/**
	 * Update the fragment without causing navigator to change view
	 */
	public void setUriFragmentParameter(String caseUuid) {
		String fragmentParameter;
		if (caseUuid == null || caseUuid.isEmpty()) {
			fragmentParameter = "";
		} else {
			fragmentParameter = caseUuid;
		}

		Page page = SormasUI.get().getPage();
		page.setUriFragment("!" + CasesView.VIEW_NAME + "/" + fragmentParameter, false);
	}

	private CaseDataDto findCase(String uuid) {
		return FacadeProvider.getCaseFacade().getCaseDataByUuid(uuid);
	}

	private void saveCase(CaseDataDto cazeDto) {

		// Compare old and new case
		CaseDataDto existingDto = FacadeProvider.getCaseFacade().getCaseDataByUuid(cazeDto.getUuid());
		onCaseChanged(existingDto, cazeDto);

		CaseDataDto resultDto = FacadeProvider.getCaseFacade().saveCase(cazeDto);

		if (resultDto.getPlagueType() != cazeDto.getPlagueType()) {
			// TODO would be much better to have a notification for this triggered in the backend
			Window window = VaadinUiUtil.showSimplePopupWindow(
				I18nProperties.getString(Strings.headingSaveNotification),
				String.format(
					I18nProperties.getString(Strings.messagePlagueTypeChange),
					resultDto.getPlagueType().toString(),
					resultDto.getPlagueType().toString()));
			window.addCloseListener(new CloseListener() {

				private static final long serialVersionUID = 1L;

				@Override
				public void windowClose(CloseEvent e) {
					if (existingDto.getCaseClassification() != resultDto.getCaseClassification() && resultDto.getClassificationUser() == null) {
						Notification notification = new Notification(
							String.format(
								I18nProperties.getString(Strings.messageCaseSavedClassificationChanged),
								resultDto.getCaseClassification().toString()),
							Type.WARNING_MESSAGE);
						notification.setDelayMsec(-1);
						notification.show(Page.getCurrent());
					} else {
						Notification.show(I18nProperties.getString(Strings.messageCaseSaved), Type.WARNING_MESSAGE);
					}
					SormasUI.refreshView();
				}
			});
		} else {
			// Notify user about an automatic case classification change
			if (existingDto != null
				&& existingDto.getCaseClassification() != resultDto.getCaseClassification()
				&& resultDto.getClassificationUser() == null) {
				Notification notification = new Notification(
					String.format(
						I18nProperties.getString(Strings.messageCaseSavedClassificationChanged),
						resultDto.getCaseClassification().toString()),
					Type.WARNING_MESSAGE);
				notification.setDelayMsec(-1);
				notification.show(Page.getCurrent());
			} else {
				Notification.show(I18nProperties.getString(Strings.messageCaseSaved), Type.WARNING_MESSAGE);
			}
			SormasUI.refreshView();
		}
	}

	private void onCaseChanged(CaseDataDto existingCase, CaseDataDto changedCase) {

		if (existingCase == null) {
			return;
		}

		// classification
		if (changedCase.getCaseClassification() != existingCase.getCaseClassification()) {
			changedCase.setClassificationDate(new Date());
			changedCase.setClassificationUser(UserProvider.getCurrent().getUserReference());
		}
	}

	public CommitDiscardWrapperComponent<CaseCreateForm> getCaseCreateComponent(
		ContactDto convertedContact,
		EventParticipantDto convertedEventParticipant,
		TravelEntryDto convertedTravelEntry,
		Disease unrelatedDisease,
		boolean createdFromLabMessage) {

		assert ((convertedContact == null && convertedEventParticipant == null)
			|| (convertedContact == null && convertedTravelEntry == null)
			|| (convertedEventParticipant == null && convertedTravelEntry == null));
		assert (unrelatedDisease == null || (convertedEventParticipant == null && convertedTravelEntry == null));

		CaseCreateForm createForm = new CaseCreateForm(convertedTravelEntry);

		CaseDataDto caze;
		PersonDto person;
		SymptomsDto symptoms;
		if (convertedContact != null) {
			VisitDto lastVisit = FacadeProvider.getVisitFacade().getLastVisitByContact(convertedContact.toReference());
			if (lastVisit != null) {
				symptoms = lastVisit.getSymptoms();
			} else {
				symptoms = null;
			}
			person = FacadeProvider.getPersonFacade().getPersonByUuid(convertedContact.getPerson().getUuid());
			if (unrelatedDisease == null) {
				caze = CaseDataDto.buildFromContact(convertedContact);
				caze.getEpiData().setContactWithSourceCaseKnown(YesNoUnknown.YES);
			} else {
				caze = CaseDataDto.buildFromUnrelatedContact(convertedContact, unrelatedDisease);
			}
		} else if (convertedEventParticipant != null) {
			EventDto event = FacadeProvider.getEventFacade().getEventByUuid(convertedEventParticipant.getEvent().getUuid(), false);
			symptoms = null;
			person = convertedEventParticipant.getPerson();
			if (unrelatedDisease == null) {
				caze = CaseDataDto.buildFromEventParticipant(convertedEventParticipant, person, event.getDisease());
			} else {
				caze = CaseDataDto.buildFromEventParticipant(convertedEventParticipant, person, unrelatedDisease);
			}
		} else if (convertedTravelEntry != null) {
			symptoms = null;
			person = FacadeProvider.getPersonFacade().getPersonByUuid(convertedTravelEntry.getPerson().getUuid());
			caze = CaseDataDto.buildFromTravelEntry(convertedTravelEntry, person);
		} else {
			symptoms = null;
			person = null;
			caze = CaseDataDto.build(null, null);
		}

		UserDto user = UserProvider.getCurrent().getUser();
		UserReferenceDto userReference = UserProvider.getCurrent().getUserReference();
		caze.setReportingUser(userReference);

		if (UserRole.isPortHealthUser(UserProvider.getCurrent().getUserRoles())) {
			caze.setResponsibleRegion(user.getRegion());
			caze.setResponsibleDistrict(user.getDistrict());
			caze.setCaseOrigin(CaseOrigin.POINT_OF_ENTRY);
			caze.setDisease(Disease.UNDEFINED);
		} else if (user.getHealthFacility() != null) {
			FacilityDto healthFacility = FacadeProvider.getFacilityFacade().getByUuid(user.getHealthFacility().getUuid());
			caze.setResponsibleRegion(healthFacility.getRegion());
			caze.setResponsibleDistrict(healthFacility.getDistrict());
			caze.setResponsibleCommunity(healthFacility.getCommunity());
			caze.setHealthFacility(healthFacility.toReference());
		} else if (convertedTravelEntry == null) {
			caze.setResponsibleRegion(user.getRegion());
			caze.setResponsibleDistrict(user.getDistrict());
			caze.setResponsibleCommunity(user.getCommunity());
		}

		createForm.setValue(caze);
		createForm.setPerson(person);
		createForm.setSymptoms(symptoms);

		if (convertedContact != null || convertedEventParticipant != null || convertedTravelEntry != null) {
			createForm.setPersonalDetailsReadOnlyIfNotEmpty(true);
			createForm.setDiseaseReadOnly(true);
		}

		final CommitDiscardWrapperComponent<CaseCreateForm> editView = new CommitDiscardWrapperComponent<CaseCreateForm>(
			createForm,
			UserProvider.getCurrent().hasUserRight(UserRight.CASE_CREATE),
			createForm.getFieldGroup());

		editView.addCommitListener(() -> {
			if (!createForm.getFieldGroup().isModified()) {
				final CaseDataDto dto = createForm.getValue();

				if (dto.getHealthFacility() == null || FacilityDto.NONE_FACILITY_UUID.equals(dto.getHealthFacility().getUuid())) {
					dto.setFacilityType(null);
				}

				if (convertedContact != null) {

					int incubationPeriod = FacadeProvider.getDiseaseConfigurationFacade().getCaseFollowUpDuration(dto.getDisease());
					List<VisitDto> visits = FacadeProvider.getVisitFacade()
						.getVisitsByContactAndPeriod(
							convertedContact.toReference(),
							DateHelper.subtractDays(dto.getReportDate(), incubationPeriod),
							DateHelper.addDays(dto.getReportDate(), incubationPeriod));
					for (VisitDto visit : visits) {
						SymptomsHelper.updateSymptoms(visit.getSymptoms(), dto.getSymptoms());
					}

					dto.getSymptoms().setOnsetDate(createForm.getOnsetDate());
					dto.getSymptoms().setUuid(DataHelper.createUuid());
					dto.getClinicalCourse().getHealthConditions().setUuid(DataHelper.createUuid());
					dto.getEpiData().setUuid(DataHelper.createUuid());
					dto.getEpiData().getExposures().forEach(exposure -> {
						exposure.setUuid(DataHelper.createUuid());
						exposure.getLocation().setUuid(DataHelper.createUuid());
					});

					dto.setWasInQuarantineBeforeIsolation(YesNoUnknown.YES);

					transferDataToPerson(createForm, person);
					FacadeProvider.getPersonFacade().savePerson(person);

					saveCase(dto);
					// retrieve the contact just in case it has been changed during case saving
					ContactDto updatedContact = FacadeProvider.getContactFacade().getContactByUuid(convertedContact.getUuid());
					// automatically change the contact status to "converted"
					updatedContact.setContactStatus(ContactStatus.CONVERTED);
					// set resulting case on contact and save it
					updatedContact.setResultingCase(dto.toReference());
					FacadeProvider.getContactFacade().saveContact(updatedContact);
					FacadeProvider.getCaseFacade().setSampleAssociations(updatedContact.toReference(), dto.toReference());
					Notification.show(I18nProperties.getString(Strings.messageCaseCreated), Type.ASSISTIVE_NOTIFICATION);
					if (!createdFromLabMessage) {
						navigateToView(CaseDataView.VIEW_NAME, dto.getUuid(), null);
					}
				} else if (convertedEventParticipant != null) {
					selectOrCreateCase(dto, convertedEventParticipant.getPerson(), uuid -> {
						if (uuid == null) {
							dto.getSymptoms().setOnsetDate(createForm.getOnsetDate());
							saveCase(dto);
							// retrieve the event participant just in case it has been changed during case saving
							EventParticipantDto updatedEventParticipant =
								FacadeProvider.getEventParticipantFacade().getEventParticipantByUuid(convertedEventParticipant.getUuid());
							if (unrelatedDisease == null) {
								// set resulting case on event participant and save it
								updatedEventParticipant.setResultingCase(dto.toReference());
								FacadeProvider.getEventParticipantFacade().saveEventParticipant(updatedEventParticipant);
								FacadeProvider.getCaseFacade().setSampleAssociations(updatedEventParticipant.toReference(), dto.toReference());
							} else {
								FacadeProvider.getCaseFacade()
									.setSampleAssociationsUnrelatedDisease(updatedEventParticipant.toReference(), dto.toReference());
							}
							if (!createdFromLabMessage) {
								navigateToView(CaseDataView.VIEW_NAME, dto.getUuid(), null);
							}
						} else {
							convertedEventParticipant.setResultingCase(FacadeProvider.getCaseFacade().getReferenceByUuid(uuid));
							FacadeProvider.getEventParticipantFacade().saveEventParticipant(convertedEventParticipant);
							if (!createdFromLabMessage) {
								navigateToView(CaseDataView.VIEW_NAME, uuid, null);
							}
						}
					});
				} else if (convertedTravelEntry != null) {
					transferDataToPerson(createForm, person);
					FacadeProvider.getPersonFacade().savePerson(person);

					saveCase(dto);

					// retrieve the travel entry just in case it has been changed during case saving
					TravelEntryDto updatedTravelEntry = FacadeProvider.getTravelEntryFacade().getByUuid(convertedTravelEntry.getUuid());
					// set resulting case on travel entry and save it
					updatedTravelEntry.setResultingCase(dto.toReference());
					FacadeProvider.getTravelEntryFacade().save(updatedTravelEntry);
					Notification.show(I18nProperties.getString(Strings.messageCaseCreated), Type.ASSISTIVE_NOTIFICATION);
					if (!createdFromLabMessage) {
						navigateToView(CaseDataView.VIEW_NAME, dto.getUuid(), null);
					}
				} else if (createdFromLabMessage) {
					PersonDto dbPerson = FacadeProvider.getPersonFacade().getPersonByUuid(dto.getPerson().getUuid());
					if (dbPerson == null) {
						PersonDto personDto = PersonDto.build();
						transferDataToPerson(createForm, personDto);
						FacadeProvider.getPersonFacade().savePerson(personDto);
						dto.getSymptoms().setOnsetDate(createForm.getOnsetDate());
						dto.setPerson(personDto.toReference());
						saveCase(dto);
					} else {
						transferDataToPerson(createForm, dbPerson);
						FacadeProvider.getPersonFacade().savePerson(dbPerson);
						dto.getSymptoms().setOnsetDate(createForm.getOnsetDate());
						saveCase(dto);
					}
				} else {
					// look for potential duplicate
					final PersonDto duplicatePerson = PersonDto.build();
					transferDataToPerson(createForm, duplicatePerson);

					ControllerProvider.getPersonController()
						.selectOrCreatePerson(duplicatePerson, I18nProperties.getString(Strings.infoSelectOrCreatePersonForCase), selectedPerson -> {
							if (selectedPerson != null) {
								dto.setPerson(selectedPerson);
								selectOrCreateCase(dto, FacadeProvider.getPersonFacade().getPersonByUuid(selectedPerson.getUuid()), uuid -> {
									if (uuid == null) {
										dto.getSymptoms().setOnsetDate(createForm.getOnsetDate());
										saveCase(dto);
										navigateToView(CaseDataView.VIEW_NAME, dto.getUuid(), null);
									} else {
										navigateToView(CaseDataView.VIEW_NAME, uuid, null);
									}
								});
							}
						}, true);
				}
			}
		});

		return editView;

	}

	private void transferDataToPerson(CaseCreateForm createForm, PersonDto person) {
		person.setFirstName(createForm.getPersonFirstName());
		person.setLastName(createForm.getPersonLastName());
		person.setBirthdateDD(createForm.getBirthdateDD());
		person.setBirthdateMM(createForm.getBirthdateMM());
		person.setBirthdateYYYY(createForm.getBirthdateYYYY());
		person.setSex(createForm.getSex());
		person.setPresentCondition(createForm.getPresentCondition());
		if (StringUtils.isNotEmpty(createForm.getPhone())) {
			person.setPhone(createForm.getPhone());
		}
		if (StringUtils.isNotEmpty(createForm.getEmailAddress())) {
			person.setEmailAddress(createForm.getEmailAddress());
		}
		person.setNationalHealthId(createForm.getNationalHealthId());
		person.setPassportNumber(createForm.getPassportNumber());
	}

	public void selectOrCreateCase(CaseDataDto caseDto, PersonDto person, Consumer<String> selectedCaseUuidConsumer) {
		CaseSimilarityCriteria criteria = CaseSimilarityCriteria.forCase(caseDto, person.getUuid());

		// Check for similar cases for the **given person**.
		// This is a case similarity check for a fixed person and will not return cases where persons are similar.
		List<CaseIndexDto> similarCases = FacadeProvider.getCaseFacade().getSimilarCases(criteria);

		if (similarCases.size() > 0) {
			CasePickOrCreateField pickOrCreateField = new CasePickOrCreateField(caseDto, person, similarCases);
			pickOrCreateField.setWidth(1280, Unit.PIXELS);

			final CommitDiscardWrapperComponent<CasePickOrCreateField> component = new CommitDiscardWrapperComponent<>(pickOrCreateField);
			component.getCommitButton().setCaption(I18nProperties.getCaption(Captions.actionConfirm));
			component.getCommitButton().setEnabled(false);
			component.addCommitListener(() -> {
				CaseIndexDto pickedCase = pickOrCreateField.getValue();
				if (pickedCase != null) {
					selectedCaseUuidConsumer.accept(pickedCase.getUuid());
				} else {
					selectedCaseUuidConsumer.accept(null);
				}
			});

			pickOrCreateField.setSelectionChangeCallback((commitAllowed) -> {
				component.getCommitButton().setEnabled(commitAllowed);
			});

			VaadinUiUtil.showModalPopupWindow(component, I18nProperties.getString(Strings.headingPickOrCreateCase));
		} else {
			selectedCaseUuidConsumer.accept(null);
		}
	}

//	public CommitDiscardWrapperComponent<? extends Component> getCaseCombinedEditComponent(final String caseUuid,
//			final ViewMode viewMode) {
//
//		CaseDataDto caze = findCase(caseUuid);
//		PersonDto person = FacadeProvider.getPersonFacade().getPersonByUuid(caze.getPerson().getUuid());
//
//		CaseDataForm caseEditForm = new CaseDataForm(person, caze.getDisease(), viewMode);
//		caseEditForm.setValue(caze);
//
//		HospitalizationForm hospitalizationForm = new HospitalizationForm(caze, viewMode);
//		hospitalizationForm.setValue(caze.getHospitalization());
//
//		SymptomsForm symptomsForm = new SymptomsForm(caze, caze.getDisease(), person, SymptomsContext.CASE, viewMode);
//		symptomsForm.setValue(caze.getSymptoms());
//
//		EpiDataForm epiDataForm = new EpiDataForm(caze.getDisease(), viewMode);
//		epiDataForm.setValue(caze.getEpiData());
//
//		CommitDiscardWrapperComponent<? extends Component> editView = AbstractEditForm
//				.buildCommitDiscardWrapper(caseEditForm, hospitalizationForm, symptomsForm, epiDataForm);
//
//		editView.addCommitListener(new CommitListener() {
//			@Override
//			public void onCommit() {
//				CaseDataDto cazeDto = caseEditForm.getValue();
//				cazeDto.setHospitalization(hospitalizationForm.getValue());
//				cazeDto.setSymptoms(symptomsForm.getValue());
//				cazeDto.setEpiData(epiDataForm.getValue());
//
//				saveCase(cazeDto);
//			}
//		});
//
//		appendSpecialCommands(caze, editView);
//
//		return editView;
//	}

	public CommitDiscardWrapperComponent<CaseDataForm> getCaseDataEditComponent(final String caseUuid, final ViewMode viewMode) {
		CaseDataDto caze = findCase(caseUuid);
		CaseDataForm caseEditForm = new CaseDataForm(
			caseUuid,
			FacadeProvider.getPersonFacade().getPersonByUuid(caze.getPerson().getUuid()),
			caze.getDisease(),
			caze.getSymptoms(),
			viewMode,
			caze.isPseudonymized());
		caseEditForm.setValue(caze);

		CommitDiscardWrapperComponent<CaseDataForm> editView = new CommitDiscardWrapperComponent<CaseDataForm>(
			caseEditForm,
			UserProvider.getCurrent().hasUserRight(UserRight.CASE_EDIT),
			caseEditForm.getFieldGroup());

		editView.addCommitListener(() -> {
			CaseDataDto oldCase = findCase(caseUuid);
			CaseDataDto cazeDto = caseEditForm.getValue();
			if (cazeDto.getHealthFacility() != null && !cazeDto.getHealthFacility().getUuid().equals(oldCase.getHealthFacility().getUuid())) {
				saveCaseWithFacilityChangedPrompt(cazeDto, oldCase);
			} else {
				saveCase(cazeDto);
			}
		});

		appendSpecialCommands(caze, editView);

		return editView;
	}

	public void showBulkCaseDataEditComponent(Collection<? extends CaseIndexDto> selectedCases) {

		if (selectedCases.size() == 0) {
			new Notification(
				I18nProperties.getString(Strings.headingNoCasesSelected),
				I18nProperties.getString(Strings.messageNoCasesSelected),
				Type.WARNING_MESSAGE,
				false).show(Page.getCurrent());
			return;
		}

		// Check if cases with multiple regions and districts have been selected
		String regionUuid = null, districtUuid = null;
		boolean first = true;
		for (CaseIndexDto selectedCase : selectedCases) {
			String currentRegionUuid =
				selectedCase.getResponsibleRegionUuid() == null ? selectedCase.getRegionUuid() : selectedCase.getResponsibleRegionUuid();
			String currentDistrictUuid =
				selectedCase.getResponsibleDistrictUuid() == null ? selectedCase.getDistrictUuid() : selectedCase.getResponsibleDistrictUuid();

			if (first) {
				regionUuid = currentRegionUuid;
				districtUuid = currentDistrictUuid;
				first = false;
			} else {
				if (!DataHelper.equal(regionUuid, currentDistrictUuid)) {
					regionUuid = null;
				}
				if (!DataHelper.equal(districtUuid, currentDistrictUuid)) {
					districtUuid = null;
				}
			}
			if (regionUuid == null && districtUuid == null)
				break;
		}

		RegionReferenceDto region = regionUuid != null ? FacadeProvider.getRegionFacade().getRegionReferenceByUuid(regionUuid) : null;
		DistrictReferenceDto district = districtUuid != null ? FacadeProvider.getDistrictFacade().getDistrictReferenceByUuid(districtUuid) : null;

		// Create a temporary case in order to use the CommitDiscardWrapperComponent
		CaseBulkEditData bulkEditData = new CaseBulkEditData();
		bulkEditData.setRegion(region);
		bulkEditData.setDistrict(district);

		BulkCaseDataForm form = new BulkCaseDataForm(district, selectedCases);
		form.setValue(bulkEditData);
		final CommitDiscardWrapperComponent<BulkCaseDataForm> editView =
			new CommitDiscardWrapperComponent<BulkCaseDataForm>(form, form.getFieldGroup());

		Window popupWindow = VaadinUiUtil.showModalPopupWindow(editView, I18nProperties.getString(Strings.headingEditCases));

		editView.addCommitListener(() -> {
			CaseBulkEditData updatedBulkEditData = form.getValue();

			boolean diseaseChange = form.getDiseaseCheckBox().getValue();
			boolean classificationChange = form.getClassificationCheckBox().getValue();
			boolean investigationStatusChange = form.getInvestigationStatusCheckBox().getValue();
			boolean outcomeChange = form.getOutcomeCheckBox().getValue();
			boolean surveillanceOfficerChange = district != null && form.getSurveillanceOfficerCheckBox().getValue();
			boolean facilityChange = form.getHealthFacilityCheckbox().getValue();

			CaseFacade caseFacade = FacadeProvider.getCaseFacade();
			if (facilityChange) {
				VaadinUiUtil.showChooseOptionPopup(
					I18nProperties.getCaption(Captions.caseInfrastructureDataChanged),
					new Label(I18nProperties.getString(Strings.messageFacilityMulitChanged)),
					I18nProperties.getCaption(Captions.caseTransferCases),
					I18nProperties.getCaption(Captions.caseEditData),
					500,
					e -> {
						bulkEditWithFacilities(
							selectedCases,
							updatedBulkEditData,
							diseaseChange,
							classificationChange,
							investigationStatusChange,
							outcomeChange,
							surveillanceOfficerChange,
							e.booleanValue(),
							caseFacade);

						popupWindow.close();
						navigateToIndex();
						Notification.show(I18nProperties.getString(Strings.messageCasesEdited), Type.HUMANIZED_MESSAGE);
					});

			} else {
				bulkEdit(
					selectedCases,
					updatedBulkEditData,
					diseaseChange,
					classificationChange,
					investigationStatusChange,
					outcomeChange,
					surveillanceOfficerChange,
					caseFacade);

				popupWindow.close();
				navigateToIndex();
				Notification.show(I18nProperties.getString(Strings.messageCasesEdited), Type.HUMANIZED_MESSAGE);
			}
		});

		editView.addDiscardListener(() -> popupWindow.close());
	}

	private void bulkEdit(
		Collection<? extends CaseIndexDto> selectedCases,
		CaseBulkEditData updatedCaseBulkEditData,
		boolean diseaseChange,
		boolean classificationChange,
		boolean investigationStatusChange,
		boolean outcomeChange,
		boolean surveillanceOfficerChange,
		CaseFacade caseFacade) {

		caseFacade.saveBulkCase(
			selectedCases.stream().map(CaseIndexDto::getUuid).collect(Collectors.toList()),
			updatedCaseBulkEditData,
			diseaseChange,
			classificationChange,
			investigationStatusChange,
			outcomeChange,
			surveillanceOfficerChange);
	}

	private void bulkEditWithFacilities(
		Collection<? extends CaseIndexDto> selectedCases,
		CaseBulkEditData updatedCaseBulkEditData,
		boolean diseaseChange,
		boolean classificationChange,
		boolean investigationStatusChange,
		boolean outcomeChange,
		boolean surveillanceOfficerChange,
		Boolean doTransfer,
		CaseFacade caseFacade) {

		caseFacade.saveBulkEditWithFacilities(
			selectedCases.stream().map(CaseIndexDto::getUuid).collect(Collectors.toList()),
			updatedCaseBulkEditData,
			diseaseChange,
			classificationChange,
			investigationStatusChange,
			outcomeChange,
			surveillanceOfficerChange,
			doTransfer);
	}

	private CaseDataDto changeCaseDto(
		CaseBulkEditData updatedCaseBulkEditData,
		CaseDataDto caseDto,
		boolean diseaseChange,
		boolean classificationChange,
		boolean investigationStatusChange,
		boolean outcomeChange,
		boolean surveillanceOfficerChange) {

		if (diseaseChange) {
			caseDto.setDisease(updatedCaseBulkEditData.getDisease());
			caseDto.setDiseaseDetails(updatedCaseBulkEditData.getDiseaseDetails());
			caseDto.setPlagueType(updatedCaseBulkEditData.getPlagueType());
			caseDto.setDengueFeverType(updatedCaseBulkEditData.getDengueFeverType());
			caseDto.setRabiesType(updatedCaseBulkEditData.getRabiesType());
		}
		if (classificationChange) {
			caseDto.setCaseClassification(updatedCaseBulkEditData.getCaseClassification());
		}
		if (investigationStatusChange) {
			caseDto.setInvestigationStatus(updatedCaseBulkEditData.getInvestigationStatus());
		}
		if (outcomeChange) {
			caseDto.setOutcome(updatedCaseBulkEditData.getOutcome());
		}
		// Setting the surveillance officer is only allowed if all selected cases are in
		// the same district
		if (surveillanceOfficerChange) {
			caseDto.setSurveillanceOfficer(updatedCaseBulkEditData.getSurveillanceOfficer());
		}

		if (Objects.nonNull(updatedCaseBulkEditData.getHealthFacilityDetails())) {
			caseDto.setHealthFacilityDetails(updatedCaseBulkEditData.getHealthFacilityDetails());
		}

		return caseDto;
	}

	private void appendSpecialCommands(CaseDataDto caze, CommitDiscardWrapperComponent<? extends Component> editView) {

		if (UserProvider.getCurrent().hasUserRight(UserRight.CASE_DELETE)) {
			editView.addDeleteListener(() -> {
				try {
					FacadeProvider.getCaseFacade().deleteCase(caze.getUuid());
					UI.getCurrent().getNavigator().navigateTo(CasesView.VIEW_NAME);
				} catch (ExternalSurveillanceToolException e) {
					Notification.show(
						String.format(
							I18nProperties.getString(Strings.ExternalSurveillanceToolGateway_notificationEntryNotDeleted),
							DataHelper.getShortUuid(caze.getUuid())),
						"",
						Type.ERROR_MESSAGE);
				}
			}, I18nProperties.getString(Strings.entityCase));
		}

		// Initialize 'Archive' button
		if (UserProvider.getCurrent().hasUserRight(UserRight.CASE_ARCHIVE)) {
			boolean archived = FacadeProvider.getCaseFacade().isArchived(caze.getUuid());
			Button archiveCaseButton = ButtonHelper.createButton(archived ? Captions.actionDearchive : Captions.actionArchive, e -> {
				editView.commit();
				archiveOrDearchiveCase(caze.getUuid(), !archived);
			}, ValoTheme.BUTTON_LINK);

			editView.getButtonsPanel().addComponentAsFirst(archiveCaseButton);
			editView.getButtonsPanel().setComponentAlignment(archiveCaseButton, Alignment.BOTTOM_LEFT);
		}

		if (UserProvider.getCurrent().hasUserRight(UserRight.CASE_REFER_FROM_POE) && caze.checkIsUnreferredPortHealthCase()) {
			Button btnReferToFacility = ButtonHelper.createButton(Captions.caseReferToFacility, e -> {
				editView.commit();
				CaseDataDto caseDto = findCase(caze.getUuid());
				referFromPointOfEntry(caseDto);
			});

			editView.getButtonsPanel().addComponentAsFirst(btnReferToFacility);
			editView.getButtonsPanel().setComponentAlignment(btnReferToFacility, Alignment.BOTTOM_LEFT);
		}
	}

	public CommitDiscardWrapperComponent<HospitalizationForm> getHospitalizationComponent(final String caseUuid, ViewMode viewMode) {

		CaseDataDto caze = findCase(caseUuid);
		HospitalizationForm hospitalizationForm = new HospitalizationForm(caze, viewMode, caze.isPseudonymized());
		hospitalizationForm.setValue(caze.getHospitalization());

		final CommitDiscardWrapperComponent<HospitalizationForm> editView = new CommitDiscardWrapperComponent<HospitalizationForm>(
			hospitalizationForm,
			UserProvider.getCurrent().hasUserRight(UserRight.CASE_EDIT),
			hospitalizationForm.getFieldGroup());

		editView.addCommitListener(() -> {
			CaseDataDto cazeDto = FacadeProvider.getCaseFacade().getCaseDataByUuid(caseUuid);
			cazeDto.setHospitalization(hospitalizationForm.getValue());
			saveCase(cazeDto);
		});

		return editView;
	}

	public CommitDiscardWrapperComponent<MaternalHistoryForm> getMaternalHistoryComponent(final String caseUuid, ViewMode viewMode) {

		CaseDataDto caze = findCase(caseUuid);
		MaternalHistoryForm form = new MaternalHistoryForm(viewMode, caze.getMaternalHistory().isPseudonymized());
		form.setValue(caze.getMaternalHistory());

		final CommitDiscardWrapperComponent<MaternalHistoryForm> component = new CommitDiscardWrapperComponent<MaternalHistoryForm>(
			form,
			UserProvider.getCurrent().hasUserRight(UserRight.CASE_EDIT),
			form.getFieldGroup());
		component.addCommitListener(() -> {
			CaseDataDto caze1 = FacadeProvider.getCaseFacade().getCaseDataByUuid(caseUuid);
			caze1.setMaternalHistory(form.getValue());
			saveCase(caze1);
		});

		return component;
	}

	public CommitDiscardWrapperComponent<PortHealthInfoForm> getPortHealthInfoComponent(final String caseUuid) {

		CaseDataDto caze = findCase(caseUuid);
		PointOfEntryReferenceDto casePointOfEntry = caze.getPointOfEntry();

		if (casePointOfEntry == null) {
			return null;
		}

		PointOfEntryDto pointOfEntry = FacadeProvider.getPointOfEntryFacade().getByUuid(casePointOfEntry.getUuid());
		PortHealthInfoForm form = new PortHealthInfoForm(pointOfEntry, caze.getPointOfEntryDetails());
		form.setValue(caze.getPortHealthInfo());

		final CommitDiscardWrapperComponent<PortHealthInfoForm> component = new CommitDiscardWrapperComponent<PortHealthInfoForm>(
			form,
			UserProvider.getCurrent().hasUserRight(UserRight.PORT_HEALTH_INFO_EDIT),
			form.getFieldGroup());
		component.addCommitListener(() -> {
			CaseDataDto caze1 = FacadeProvider.getCaseFacade().getCaseDataByUuid(caseUuid);
			caze1.setPortHealthInfo(form.getValue());
			saveCase(caze1);
		});

		return component;
	}

	public CommitDiscardWrapperComponent<SymptomsForm> getSymptomsEditComponent(final String caseUuid, ViewMode viewMode) {

		CaseDataDto caseDataDto = findCase(caseUuid);
		PersonDto person = FacadeProvider.getPersonFacade().getPersonByUuid(caseDataDto.getPerson().getUuid());

		SymptomsForm symptomsForm = new SymptomsForm(
			caseDataDto,
			caseDataDto.getDisease(),
			person,
			SymptomsContext.CASE,
			viewMode,
			UiFieldAccessCheckers.forSensitiveData(caseDataDto.isPseudonymized()));
		symptomsForm.setValue(caseDataDto.getSymptoms());

		CommitDiscardWrapperComponent<SymptomsForm> editView = new CommitDiscardWrapperComponent<SymptomsForm>(
			symptomsForm,
			UserProvider.getCurrent().hasUserRight(UserRight.CASE_EDIT),
			symptomsForm.getFieldGroup());

		editView.addCommitListener(() -> {
			CaseDataDto cazeDto = FacadeProvider.getCaseFacade().getCaseDataByUuid(caseUuid);
			cazeDto.setSymptoms(symptomsForm.getValue());
			saveCase(cazeDto);
		});

		return editView;
	}

	public CommitDiscardWrapperComponent<EpiDataForm> getEpiDataComponent(final String caseUuid, Consumer<Boolean> sourceContactsToggleCallback) {

		CaseDataDto caze = findCase(caseUuid);
		EpiDataForm epiDataForm = new EpiDataForm(caze.getDisease(), CaseDataDto.class, caze.isPseudonymized(), sourceContactsToggleCallback);
		epiDataForm.setValue(caze.getEpiData());

		final CommitDiscardWrapperComponent<EpiDataForm> editView = new CommitDiscardWrapperComponent<EpiDataForm>(
			epiDataForm,
			UserProvider.getCurrent().hasUserRight(UserRight.CASE_EDIT),
			epiDataForm.getFieldGroup());

		editView.addCommitListener(() -> {
			CaseDataDto cazeDto = FacadeProvider.getCaseFacade().getCaseDataByUuid(caseUuid);
			cazeDto.setEpiData(epiDataForm.getValue());
			saveCase(cazeDto);
		});

		return editView;
	}

	public CommitDiscardWrapperComponent<ClinicalCourseForm> getClinicalCourseComponent(String caseUuid) {

		CaseDataDto caze = FacadeProvider.getCaseFacade().getCaseDataByUuid(caseUuid);
		ClinicalCourseForm form = new ClinicalCourseForm(caze.isPseudonymized());
		form.setValue(caze.getClinicalCourse());

		final CommitDiscardWrapperComponent<ClinicalCourseForm> view =
			new CommitDiscardWrapperComponent<>(form, UserProvider.getCurrent().hasUserRight(UserRight.CLINICAL_COURSE_EDIT), form.getFieldGroup());
		view.addCommitListener(() -> {
			if (!form.getFieldGroup().isModified()) {
				CaseDataDto cazeDto = FacadeProvider.getCaseFacade().getCaseDataByUuid(caseUuid);
				cazeDto.setClinicalCourse(form.getValue());
				saveCase(cazeDto);
			}
		});

		return view;
	}

	public void saveCaseWithFacilityChangedPrompt(CaseDataDto caze, CaseDataDto oldCase) {

		VaadinUiUtil.showChooseOptionPopup(
			I18nProperties.getCaption(Captions.caseInfrastructureDataChanged),
			new Label(I18nProperties.getString(Strings.messageFacilityChanged)),
			I18nProperties.getCaption(Captions.caseTransferCase),
			I18nProperties.getCaption(Captions.caseEditData),
			500,
			e -> {
				CaseLogic.handleHospitalization(caze, oldCase, e.booleanValue());
				saveCase(caze);
			});
	}

	public void referFromPointOfEntry(CaseDataDto caze) {

		CaseFacilityChangeForm form = new CaseFacilityChangeForm();
		form.setValue(caze);
		CommitDiscardWrapperComponent<CaseFacilityChangeForm> view = new CommitDiscardWrapperComponent<CaseFacilityChangeForm>(
			form,
			UserProvider.getCurrent().hasUserRight(UserRight.CASE_REFER_FROM_POE),
			form.getFieldGroup());
		view.getCommitButton().setCaption(I18nProperties.getCaption(Captions.caseReferToFacility));

		Window window = VaadinUiUtil.showPopupWindow(view);
		window.setCaption(I18nProperties.getString(Strings.headingReferCaseFromPointOfEntry));

		view.addCommitListener(() -> {
			if (!form.getFieldGroup().isModified()) {
				CaseDataDto dto = form.getValue();
				dto.getHospitalization().setAdmissionDate(new Date());
				FacadeProvider.getCaseFacade().saveCase(dto);
				window.close();
				Notification.show(I18nProperties.getString(Strings.messageCaseReferredFromPoe), Type.ASSISTIVE_NOTIFICATION);
				SormasUI.refreshView();
			}
		});

		Button btnCancel = ButtonHelper.createButton(Captions.actionCancel, e -> {
			window.close();
		});

		view.getButtonsPanel().replaceComponent(view.getDiscardButton(), btnCancel);
	}

	private void archiveOrDearchiveCase(String caseUuid, boolean archive) {

		if (archive) {
			Label contentLabel = new Label(
				String.format(
					I18nProperties.getString(Strings.confirmationArchiveCase),
					I18nProperties.getString(Strings.entityCase).toLowerCase(),
					I18nProperties.getString(Strings.entityCase).toLowerCase()));
			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getString(Strings.headingArchiveCase),
				contentLabel,
				I18nProperties.getString(Strings.yes),
				I18nProperties.getString(Strings.no),
				640,
				e -> {
					if (e.booleanValue() == true) {
						FacadeProvider.getCaseFacade().archiveOrDearchiveCase(caseUuid, true);
						Notification.show(
							String.format(I18nProperties.getString(Strings.messageCaseArchived), I18nProperties.getString(Strings.entityCase)),
							Type.ASSISTIVE_NOTIFICATION);
						navigateToView(CaseDataView.VIEW_NAME, caseUuid, null);
					}
				});
		} else {
			Label contentLabel = new Label(
				String.format(
					I18nProperties.getString(Strings.confirmationDearchiveCase),
					I18nProperties.getString(Strings.entityCase).toLowerCase(),
					I18nProperties.getString(Strings.entityCase).toLowerCase()));
			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getString(Strings.headingDearchiveCase),
				contentLabel,
				I18nProperties.getString(Strings.yes),
				I18nProperties.getString(Strings.no),
				640,
				e -> {
					if (e.booleanValue()) {
						FacadeProvider.getCaseFacade().archiveOrDearchiveCase(caseUuid, false);
						Notification.show(
							String.format(I18nProperties.getString(Strings.messageCaseDearchived), I18nProperties.getString(Strings.entityCase)),
							Type.ASSISTIVE_NOTIFICATION);
						navigateToView(CaseDataView.VIEW_NAME, caseUuid, null);
					}
				});
		}
	}

	public void openClassificationRulesPopup(DiseaseClassificationCriteriaDto diseaseCriteria) {

		VerticalLayout classificationRulesLayout = new VerticalLayout();
		classificationRulesLayout.setMargin(true);

		if (diseaseCriteria != null) {
			if (diseaseCriteria.getSuspectCriteria() != null) {
				Label suspectContent = new Label();
				suspectContent.setContentMode(ContentMode.HTML);
				suspectContent.setWidth(100, Unit.PERCENTAGE);
				suspectContent.setValue(ClassificationHtmlRenderer.createSuspectHtmlString(diseaseCriteria));
				classificationRulesLayout.addComponent(suspectContent);
			}

			if (diseaseCriteria.getProbableCriteria() != null) {
				Label probableContent = new Label();
				probableContent.setContentMode(ContentMode.HTML);
				probableContent.setWidth(100, Unit.PERCENTAGE);
				probableContent.setValue(ClassificationHtmlRenderer.createProbableHtmlString(diseaseCriteria));
				classificationRulesLayout.addComponent(probableContent);
			}

			if (diseaseCriteria.getConfirmedCriteria() != null) {
				Label confirmedContent = new Label();
				confirmedContent.setContentMode(ContentMode.HTML);
				confirmedContent.setWidth(100, Unit.PERCENTAGE);
				confirmedContent.setValue(ClassificationHtmlRenderer.createConfirmedHtmlString(diseaseCriteria));
				classificationRulesLayout.addComponent(confirmedContent);
			}

			if (FacadeProvider.getConfigFacade().isConfiguredCountry(CountryHelper.COUNTRY_CODE_GERMANY)) {
				if (diseaseCriteria.getConfirmedNoSymptomsCriteria() != null) {
					Label confirmedNoSymptomsContent = new Label();
					confirmedNoSymptomsContent.setContentMode(ContentMode.HTML);
					confirmedNoSymptomsContent.setWidth(100, Unit.PERCENTAGE);
					confirmedNoSymptomsContent.setValue(ClassificationHtmlRenderer.createConfirmedNoSymptomsHtmlString(diseaseCriteria));
					classificationRulesLayout.addComponent(confirmedNoSymptomsContent);
				}

				if (diseaseCriteria.getConfirmedUnknownSymptomsCriteria() != null) {
					Label confirmedUnknownSymptomsContent = new Label();
					confirmedUnknownSymptomsContent.setContentMode(ContentMode.HTML);
					confirmedUnknownSymptomsContent.setWidth(100, Unit.PERCENTAGE);
					confirmedUnknownSymptomsContent.setValue(ClassificationHtmlRenderer.createConfirmedUnknownSymptomsHtmlString(diseaseCriteria));
					classificationRulesLayout.addComponent(confirmedUnknownSymptomsContent);
				}
			}

			if (diseaseCriteria.getNotACaseCriteria() != null) {
				Label notACaseContent = new Label();
				notACaseContent.setContentMode(ContentMode.HTML);
				notACaseContent.setWidth(100, Unit.PERCENTAGE);
				notACaseContent.setValue(ClassificationHtmlRenderer.createNotACaseHtmlString(diseaseCriteria));
				classificationRulesLayout.addComponent(notACaseContent);
			}
		}

		Window popupWindow = VaadinUiUtil.showPopupWindow(classificationRulesLayout);
		popupWindow.addCloseListener(e -> {
			popupWindow.close();
		});
		popupWindow.setWidth(860, Unit.PIXELS);
		popupWindow.setHeight(80, Unit.PERCENTAGE);
		popupWindow.setCaption(I18nProperties.getString(Strings.classificationRulesFor) + " " + diseaseCriteria.getDisease().toString());
	}

	public void deleteAllSelectedItems(Collection<? extends CaseIndexDto> selectedRows, Runnable callback) {

		if (selectedRows.size() == 0) {
			new Notification(
				I18nProperties.getString(Strings.headingNoCasesSelected),
				I18nProperties.getString(Strings.messageNoCasesSelected),
				Type.WARNING_MESSAGE,
				false).show(Page.getCurrent());
		} else {
			VaadinUiUtil
				.showDeleteConfirmationWindow(String.format(I18nProperties.getString(Strings.confirmationDeleteCases), selectedRows.size()), () -> {
					int countNotDeletedCases = 0;
					StringBuilder nonDeletableCases = new StringBuilder();
					for (CaseIndexDto selectedRow : selectedRows) {
						try {
							FacadeProvider.getCaseFacade().deleteCase(selectedRow.getUuid());
						} catch (ExternalSurveillanceToolException e) {
							countNotDeletedCases++;
							nonDeletableCases.append(selectedRow.getUuid(), 0, 6).append(", ");
						}
					}
					if (nonDeletableCases.length() > 0) {
						nonDeletableCases = new StringBuilder(" " + nonDeletableCases.substring(0, nonDeletableCases.length() - 2) + ". ");
					}
					callback.run();
					if (countNotDeletedCases == 0) {
						new Notification(
							I18nProperties.getString(Strings.headingCasesDeleted),
							I18nProperties.getString(Strings.messageCasesDeleted),
							Type.HUMANIZED_MESSAGE,
							false).show(Page.getCurrent());
					} else {
						Window response = VaadinUiUtil.showSimplePopupWindow(
							I18nProperties.getString(Strings.headingSomeCasesNotDeleted),
							String.format(
								"%1s <br/> <br/> %2s",
								String.format(
									I18nProperties.getString(Strings.messageCountCasesNotDeleted),
									String.format("<b>%s</b>", countNotDeletedCases),
									String.format("<b>%s</b>", HtmlHelper.cleanHtml(nonDeletableCases.toString()))),
								I18nProperties.getString(Strings.messageCasesNotDeletedReasonExternalSurveillanceTool)),
							ContentMode.HTML);
						response.setWidth(600, Sizeable.Unit.PIXELS);
					}
				});
		}
	}

	public void sendSmsToAllSelectedItems(Collection<? extends CaseIndexDto> selectedRows, Runnable callback) {

		if (selectedRows.size() == 0) {
			new Notification(
				I18nProperties.getString(Strings.headingNoCasesSelected),
				I18nProperties.getString(Strings.messageNoCasesSelected),
				Type.WARNING_MESSAGE,
				false).show(Page.getCurrent());
		} else {
			final List<String> caseUuids = selectedRows.stream().map(caseIndexDto -> caseIndexDto.getUuid()).collect(Collectors.toList());
			final SmsComponent smsComponent =
				new SmsComponent(FacadeProvider.getCaseFacade().countCasesWithMissingContactInformation(caseUuids, MessageType.SMS));
			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getCaption(Captions.messagesSendingSms),
				smsComponent,
				I18nProperties.getCaption(Captions.actionSend),
				I18nProperties.getCaption(Captions.actionCancel),
				640,
				confirmationEvent -> {
					if (confirmationEvent.booleanValue()) {
						FacadeProvider.getCaseFacade().sendMessage(caseUuids, "", smsComponent.getValue(), MessageType.SMS);
						Notification.show(null, I18nProperties.getString(Strings.notificationSmsSent), Type.TRAY_NOTIFICATION);
					}
				});
		}
	}

	public void archiveAllSelectedItems(Collection<? extends CaseIndexDto> selectedRows, Runnable callback) {

		if (selectedRows.size() == 0) {
			new Notification(
				I18nProperties.getString(Strings.headingNoCasesSelected),
				I18nProperties.getString(Strings.messageNoCasesSelected),
				Type.WARNING_MESSAGE,
				false).show(Page.getCurrent());
		} else {
			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getString(Strings.headingConfirmArchiving),
				new Label(String.format(I18nProperties.getString(Strings.confirmationArchiveCases), selectedRows.size())),
				I18nProperties.getString(Strings.yes),
				I18nProperties.getString(Strings.no),
				null,
				e -> {
					if (e.booleanValue() == true) {
						List<String> caseUuids = selectedRows.stream().map(r -> r.getUuid()).collect(Collectors.toList());
						FacadeProvider.getCaseFacade().updateArchived(caseUuids, true);
						callback.run();
						new Notification(
							I18nProperties.getString(Strings.headingCasesArchived),
							I18nProperties.getString(Strings.messageCasesArchived),
							Type.HUMANIZED_MESSAGE,
							false).show(Page.getCurrent());
					}
				});
		}
	}

	public void dearchiveAllSelectedItems(Collection<? extends CaseIndexDto> selectedRows, Runnable callback) {

		if (selectedRows.size() == 0) {
			new Notification(
				I18nProperties.getString(Strings.headingNoCasesSelected),
				I18nProperties.getString(Strings.messageNoCasesSelected),
				Type.WARNING_MESSAGE,
				false).show(Page.getCurrent());
		} else {
			VaadinUiUtil.showConfirmationPopup(
				I18nProperties.getString(Strings.headingConfirmDearchiving),
				new Label(String.format(I18nProperties.getString(Strings.confirmationDearchiveCases), selectedRows.size())),
				I18nProperties.getString(Strings.yes),
				I18nProperties.getString(Strings.no),
				null,
				e -> {
					if (e.booleanValue() == true) {
						List<String> caseUuids = selectedRows.stream().map(r -> r.getUuid()).collect(Collectors.toList());
						FacadeProvider.getCaseFacade().updateArchived(caseUuids, false);
						callback.run();
						new Notification(
							I18nProperties.getString(Strings.headingCasesDearchived),
							I18nProperties.getString(Strings.messageCasesDearchived),
							Type.HUMANIZED_MESSAGE,
							false).show(Page.getCurrent());
					}
				});
		}
	}

	public void openLineListingWindow() {

		Window window = new Window(I18nProperties.getString(Strings.headingLineListing));

		LineListingLayout lineListingForm = new LineListingLayout(window);
		lineListingForm.setSaveCallback(cases -> saveCasesFromLineListing(lineListingForm, cases));

		if (UserProvider.getCurrent().hasUserRight(UserRight.CASE_CHANGE_EPID_NUMBER)) {
			lineListingForm.setWidth(LineListingLayout.DEFAULT_WIDTH, Unit.PIXELS);
		} else {
			lineListingForm.setWidth(LineListingLayout.WITDH_WITHOUT_EPID_NUMBER, Unit.PIXELS);
		}
		window.setContent(lineListingForm);
		window.setModal(true);
		window.setPositionX((int) Math.max(0, (Page.getCurrent().getBrowserWindowWidth() - lineListingForm.getWidth())) / 2);
		window.setPositionY(70);
		window.setResizable(false);

		UI.getCurrent().addWindow(window);
	}

	private void saveCasesFromLineListing(LineListingLayout lineListingForm, List<LineListingLayout.CaseLineDto> cases) {
		try {
			lineListingForm.validate();
		} catch (ValidationRuntimeException e) {
			Notification.show(I18nProperties.getString(Strings.errorFieldValidationFailed), "", Type.ERROR_MESSAGE);
			return;
		}

		for (LineListingLayout.CaseLineDto caseLineDto : cases) {
			CaseDataDto newCase = CaseDataDto.build(PersonDto.build().toReference(), caseLineDto.getDisease());

			newCase.setDiseaseDetails(caseLineDto.getDiseaseDetails());
			newCase.setResponsibleRegion(caseLineDto.getRegion());
			newCase.setResponsibleDistrict(caseLineDto.getDistrict());
			newCase.setReportDate(DateHelper8.toDate(caseLineDto.getDateOfReport()));
			newCase.setEpidNumber(caseLineDto.getEpidNumber());
			newCase.setResponsibleCommunity(caseLineDto.getCommunity());
			newCase.setFacilityType(caseLineDto.getFacilityType());
			newCase.setHealthFacility(caseLineDto.getFacility());
			newCase.setHealthFacilityDetails(caseLineDto.getFacilityDetails());

			if (caseLineDto.getDateOfOnset() != null) {
				newCase.getSymptoms().setOnsetDate(DateHelper8.toDate(caseLineDto.getDateOfOnset()));
			}

			newCase.setReportingUser(UserProvider.getCurrent().getUserReference());

			final PersonDto newPerson = PersonDto.build();
			newPerson.setFirstName(caseLineDto.getPerson().getFirstName());
			newPerson.setLastName(caseLineDto.getPerson().getLastName());
			if (caseLineDto.getPerson().getBirthDate() != null) {
				newPerson.setBirthdateYYYY(caseLineDto.getPerson().getBirthDate().getDateOfBirthYYYY());
				newPerson.setBirthdateMM(caseLineDto.getPerson().getBirthDate().getDateOfBirthMM());
				newPerson.setBirthdateDD(caseLineDto.getPerson().getBirthDate().getDateOfBirthDD());
			}
			newPerson.setSex(caseLineDto.getPerson().getSex());

			ControllerProvider.getPersonController()
				.selectOrCreatePerson(newPerson, I18nProperties.getString(Strings.infoSelectOrCreatePersonForCase), selectedPerson -> {
					if (selectedPerson != null) {
						newCase.setPerson(selectedPerson);

						selectOrCreateCase(newCase, FacadeProvider.getPersonFacade().getPersonByUuid(selectedPerson.getUuid()), uuid -> {
							if (uuid == null) {
								FacadeProvider.getCaseFacade().saveCase(newCase);
								Notification.show(I18nProperties.getString(Strings.messageCaseCreated), Type.ASSISTIVE_NOTIFICATION);
							}
						});
					}
				}, true);
		}

		lineListingForm.closeWindow();
		ControllerProvider.getCaseController().navigateToIndex();
	}

	public VerticalLayout getCaseViewTitleLayout(CaseDataDto caseData) {
		VerticalLayout titleLayout = new VerticalLayout();
		titleLayout.addStyleNames(CssStyles.LAYOUT_MINIMAL, CssStyles.VSPACE_4, CssStyles.VSPACE_TOP_4);
		titleLayout.setSpacing(false);

		Label diseaseLabel = new Label(DiseaseHelper.toString(caseData.getDisease(), caseData.getDiseaseDetails()));
		CssStyles.style(diseaseLabel, CssStyles.H3, CssStyles.VSPACE_NONE, CssStyles.VSPACE_TOP_NONE);
		titleLayout.addComponents(diseaseLabel);

		Label classificationLabel = new Label(caseData.getCaseClassification().toString());
		classificationLabel.addStyleNames(CssStyles.H3, CssStyles.VSPACE_NONE, CssStyles.VSPACE_TOP_NONE);
		titleLayout.addComponent(classificationLabel);

		String shortUuid = DataHelper.getShortUuid(caseData.getUuid());
		String casePersonFullName = caseData.getPerson().getCaption();
		StringBuilder caseLabelSb = new StringBuilder();
		if (StringUtils.isNotBlank(casePersonFullName)) {
			caseLabelSb.append(casePersonFullName);

			PersonDto casePerson = FacadeProvider.getPersonFacade().getPersonByUuid(caseData.getPerson().getUuid());
			if (casePerson.getBirthdateDD() != null && casePerson.getBirthdateMM() != null && casePerson.getBirthdateYYYY() != null) {
				caseLabelSb.append(" (* ")
					.append(
						PersonHelper.formatBirthdate(
							casePerson.getBirthdateDD(),
							casePerson.getBirthdateMM(),
							casePerson.getBirthdateYYYY(),
							I18nProperties.getUserLanguage()))
					.append(")");
			}
		}
		caseLabelSb.append(caseLabelSb.length() > 0 ? " (" + shortUuid + ")" : shortUuid);
		Label caseLabel = new Label(caseLabelSb.toString());
		caseLabel.addStyleNames(CssStyles.H2, CssStyles.VSPACE_NONE, CssStyles.VSPACE_TOP_NONE, CssStyles.LABEL_PRIMARY);
		titleLayout.addComponent(caseLabel);

		return titleLayout;
	}

	public void sendCasesToExternalSurveillanceTool(Collection<? extends CaseIndexDto> selectedCases, Runnable reloadCallback) {
		List<String> selectedUuids = selectedCases.stream().map(CaseIndexDto::getUuid).collect(Collectors.toList());

		// Show an error when at least one selected case is not a CORONAVIRUS case
		Optional<? extends CaseIndexDto> nonCoronavirusCase = selectedCases.stream().filter(c -> c.getDisease() != Disease.CORONAVIRUS).findFirst();
		if (nonCoronavirusCase.isPresent()) {
			Notification.show(
				String.format(
					I18nProperties.getString(Strings.errorExternalSurveillanceToolNonCoronavirusCase),
					DataHelper.getShortUuid(nonCoronavirusCase.get().getUuid()),
					I18nProperties.getEnumCaption(Disease.CORONAVIRUS)),
				"",
				Type.ERROR_MESSAGE);
			return;
		}

		// Show an error when at least one selected case is not owned by this server because ownership has been handed over
		String notSharableUuid = FacadeProvider.getCaseFacade().getFirstUuidNotShareableWithExternalReportingTools(selectedUuids);
		if (notSharableUuid != null) {
			Notification.show(
				String
					.format(I18nProperties.getString(Strings.errorExternalSurveillanceToolCaseNotSharable), DataHelper.getShortUuid(notSharableUuid)),
				"",
				Type.ERROR_MESSAGE);
			return;
		}

		ExternalSurveillanceServiceGateway.sendCasesToExternalSurveillanceTool(selectedUuids, reloadCallback);
	}

}
