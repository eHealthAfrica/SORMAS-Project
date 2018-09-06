package de.symeda.sormas.app.backend.event;

import java.util.List;

import de.symeda.sormas.api.caze.CaseReferenceDto;
import de.symeda.sormas.api.event.EventParticipantDto;
import de.symeda.sormas.api.event.EventParticipantReferenceDto;
import de.symeda.sormas.api.person.PersonDto;
import de.symeda.sormas.app.backend.caze.Case;
import de.symeda.sormas.app.backend.caze.CaseDtoHelper;
import de.symeda.sormas.app.backend.common.AdoDtoHelper;
import de.symeda.sormas.app.backend.common.DaoException;
import de.symeda.sormas.app.backend.common.DatabaseHelper;
import de.symeda.sormas.app.backend.person.Person;
import de.symeda.sormas.app.backend.person.PersonDtoHelper;
import de.symeda.sormas.app.rest.RetroProvider;
import retrofit2.Call;

public class EventParticipantDtoHelper extends AdoDtoHelper<EventParticipant, EventParticipantDto> {

    private PersonDtoHelper personHelper = new PersonDtoHelper();

    @Override
    protected Class<EventParticipant> getAdoClass() {
        return EventParticipant.class;
    }

    @Override
    protected Class<EventParticipantDto> getDtoClass() {
        return EventParticipantDto.class;
    }

    @Override
    protected Call<List<EventParticipantDto>> pullAllSince(long since) {
        return RetroProvider.getEventParticipantFacade().pullAllSince(since);
    }

    @Override
    protected Call<List<EventParticipantDto>> pullByUuids(List<String> uuids) {
        return RetroProvider.getEventParticipantFacade().pullByUuids(uuids);
    }

    @Override
    protected Call<Integer> pushAll(List<EventParticipantDto> eventParticipantDtos) {
        return RetroProvider.getEventParticipantFacade().pushAll(eventParticipantDtos);
    }

    @Override
    public void fillInnerFromDto(EventParticipant target, EventParticipantDto source) {
        if (source.getEvent() != null) {
            target.setEvent(DatabaseHelper.getEventDao().queryUuid(source.getEvent().getUuid()));
        } else {
            target.setEvent(null);
        }

        if (source.getPerson() != null) {
            target.setPerson(DatabaseHelper.getPersonDao().queryUuid(source.getPerson().getUuid()));
        } else {
            target.setPerson(null);
        }

        target.setInvolvementDescription(source.getInvolvementDescription());

        target.setResultingCaseUuid(source.getResultingCase() != null ? source.getResultingCase().getUuid() : null);
    }

    @Override
    public void fillInnerFromAdo(EventParticipantDto target, EventParticipant source) {
        if (source.getEvent() != null) {
            Event event = DatabaseHelper.getEventDao().queryForId(source.getEvent().getId());
            target.setEvent(EventDtoHelper.toReferenceDto(event));
        } else {
            target.setEvent(null);
        }

        if (source.getPerson() != null) {
            Person person = DatabaseHelper.getPersonDao().queryForId(source.getPerson().getId());
            target.setPerson(personHelper.adoToDto(person));
        } else {
            target.setPerson(null);
        }

        // The resulting case is NOT set to null if resultingCaseUuid is null because it could be possible
        // that a resulting case is present in the main database and simply not synchronized to the app instance
        if (source.getResultingCaseUuid() != null) {
            Case resultingCase = DatabaseHelper.getCaseDao().queryUuidBasic(source.getResultingCaseUuid());
            target.setResultingCase(CaseDtoHelper.toReferenceDto(resultingCase));
        }

        target.setInvolvementDescription(source.getInvolvementDescription());
    }

    public static EventParticipantReferenceDto toReferenceDto(EventParticipant ado) {
        if (ado == null) {
            return null;
        }
        EventParticipantReferenceDto dto = new EventParticipantReferenceDto(ado.getUuid());

        return dto;
    }
}
