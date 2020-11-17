package de.symeda.sormas.ui.docgeneration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.ui.Notification;

import de.symeda.sormas.api.FacadeProvider;
import de.symeda.sormas.api.docgeneneration.EventDocumentFacade;
import de.symeda.sormas.api.event.EventReferenceDto;

public class EventDocumentLayout extends AbstractDocgenerationLayout {

	private final EventReferenceDto eventReferenceDto;

	public EventDocumentLayout(EventReferenceDto eventReferenceDto) {
		super("Ereignis-Formular");
		this.eventReferenceDto = eventReferenceDto;
	}

	@Override
	protected List<String> getAvailableTemplates() {
		return FacadeProvider.getEventDocumentFacade().getAvailableTemplates();
	}

	@Override
	protected String generateFilename(String templateFile) {
		String uuid = eventReferenceDto.getUuid();
		return uuid.substring(0, Math.min(5, uuid.length())) + "_" + templateFile;
	}

	@Override
	protected List<String> getAdditionalVariables(String templateFile) throws IOException {
		return FacadeProvider.getEventDocumentFacade().getAdditionalVariables(templateFile);
	}

	@Override
	protected StreamResource createStreamResource(String templateFile, String filename) {
		return new StreamResource((StreamResource.StreamSource) () -> {
			EventDocumentFacade eventDocumentFacade = FacadeProvider.getEventDocumentFacade();
			try {
				return new ByteArrayInputStream(
					eventDocumentFacade.getGeneratedDocument(templateFile, eventReferenceDto, readAdditionalVariables()).getBytes());
			} catch (IOException | IllegalArgumentException e) {
				new Notification("Document generation failed", e.getMessage(), Notification.Type.ERROR_MESSAGE).show(Page.getCurrent());
				return null;
			}
		}, filename);
	}
}
