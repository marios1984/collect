package org.openforis.collect.persistence;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.commons.io.IOUtils;
import org.jooq.Record;
import org.openforis.collect.manager.SurveyMigrator;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.CollectSurveyContext;
import org.openforis.collect.model.SurveySummary;
import org.openforis.collect.persistence.jooq.JooqDaoSupport;
import org.openforis.collect.persistence.xml.CollectSurveyIdmlBinder;
import org.openforis.idm.metamodel.Survey;
import org.openforis.idm.metamodel.xml.IdmlParseException;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *  @author S. Ricci
 */
abstract class SurveyBaseDao extends JooqDaoSupport {
	
	@Autowired
	protected CollectSurveyContext surveyContext;
	
	public void init() {
	}

	protected abstract CollectSurvey processSurveyRow(Record row);

	protected abstract SurveySummary processSurveySummaryRow(Record row);
	
	public CollectSurvey unmarshalIdml(String idml) throws IdmlParseException {
		byte[] bytes;
		try {
			bytes = idml.getBytes("UTF-8");
			ByteArrayInputStream is = new ByteArrayInputStream(bytes);
			return unmarshalIdml(is);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	public CollectSurvey unmarshalIdml(InputStream is) throws IdmlParseException {
		InputStreamReader reader = new InputStreamReader(is);
		return unmarshalIdml(reader);
	}

	public CollectSurvey unmarshalIdml(Reader reader) throws IdmlParseException {
		CollectSurveyIdmlBinder binder = new CollectSurveyIdmlBinder(surveyContext);
		try {
			CollectSurvey survey = (CollectSurvey) binder.unmarshal(reader);
			SurveyMigrator migrator = getSurveyMigrator();
			migrator.migrate(survey);
			return survey;
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}

	public String marshalSurvey(Survey survey) throws SurveyImportException {
		try {
			// Serialize Survey to XML
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			marshalSurvey(survey, os);
			return os.toString("UTF-8");
		} catch (IOException e) {
			throw new SurveyImportException("Error marshalling survey", e);
		}
	}
	
	public void marshalSurvey(Survey survey, OutputStream os) throws SurveyImportException {
		try {
			CollectSurveyIdmlBinder binder = new CollectSurveyIdmlBinder(surveyContext);
			binder.marshal(survey, os);
		} catch (IOException e) {
			throw new SurveyImportException("Error marshalling survey", e);
		}
	}
	
	protected SurveyMigrator getSurveyMigrator() {
		return new SurveyMigrator();
	}
	
	public CollectSurveyContext getSurveyContext() {
		return surveyContext;
	}
	
	public void setSurveyContext(CollectSurveyContext surveyContext) {
		this.surveyContext = surveyContext;
	}
	
}
