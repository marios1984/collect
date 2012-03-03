/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.granite.messaging.amf.io.util.externalizer.annotation.ExternalizedProperty;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.Code;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.Date;
import org.openforis.idm.model.Field;
import org.openforis.idm.model.TaxonOccurrence;
import org.openforis.idm.model.Time;

/**
 * @author M. Togna
 * 
 */
public class AttributeProxy extends NodeProxy {

	private transient Attribute<? extends AttributeDefinition, ?> attribute;

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public AttributeProxy(Attribute attribute) {
		super(attribute);
		this.attribute = attribute;
	}

	@ExternalizedProperty
	public Object getValue() {
		Object val = attribute.getValue();
		if (val != null) {
			if (val instanceof Code) {
				return new CodeProxy((Code) val);
			} else if (val instanceof Coordinate) {
				return new CoordinateProxy((Coordinate) val);
			} else if (val instanceof Date) {
				return new DateProxy((Date) val);
			} else if (val instanceof TaxonOccurrence) {
				return new TaxonOccurrenceProxy((TaxonOccurrence) val);
			} else if (val instanceof Time) {
				return new TimeProxy((Time) val);
			} else {
				return val;
			}
		} else {
			return null;
		}
	}

	@ExternalizedProperty
	public boolean isEmpty() {
		return attribute.isEmpty();
	}

	@ExternalizedProperty
	public ValidationResultsProxy getValidationResultsProxy(){
		return new ValidationResultsProxy(attribute.validateValue());
	}
	
//	 @ExternalizedProperty
//	 public boolean isRelevant() {
//	 return attribute.isRelevant();
//	 }
//	
//	 @ExternalizedProperty
//	 public boolean isRequired() {
//	 return attribute.isRequired();
//	 }

	@ExternalizedProperty
	public List<FieldProxy> getFields() {
		int fieldCount = attribute.getFieldCount();
		List<FieldProxy> result = new ArrayList<FieldProxy>(fieldCount);
		for (int i = 0; i < fieldCount; i++) {
			Field<?> field = attribute.getField(i);
			result.add(i, new FieldProxy(field));
		}
		return result;
	}
	
	

}
