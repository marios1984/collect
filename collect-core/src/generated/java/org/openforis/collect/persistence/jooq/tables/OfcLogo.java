/**
 * This class is generated by jOOQ
 */
package org.openforis.collect.persistence.jooq.tables;

/**
 * This class is generated by jOOQ.
 */
@javax.annotation.Generated(value    = {"http://www.jooq.org", "2.0.1"},
                            comments = "This class is generated by jOOQ")
public class OfcLogo extends org.jooq.impl.UpdatableTableImpl<org.openforis.collect.persistence.jooq.tables.records.OfcLogoRecord> {

	private static final long serialVersionUID = 427034416;

	/**
	 * The singleton instance of ofc_logo
	 */
	public static final org.openforis.collect.persistence.jooq.tables.OfcLogo OFC_LOGO = new org.openforis.collect.persistence.jooq.tables.OfcLogo();

	/**
	 * The class holding records for this type
	 */
	private static final java.lang.Class<org.openforis.collect.persistence.jooq.tables.records.OfcLogoRecord> __RECORD_TYPE = org.openforis.collect.persistence.jooq.tables.records.OfcLogoRecord.class;

	/**
	 * The class holding records for this type
	 */
	@Override
	public java.lang.Class<org.openforis.collect.persistence.jooq.tables.records.OfcLogoRecord> getRecordType() {
		return __RECORD_TYPE;
	}

	/**
	 * An uncommented item
	 * 
	 * PRIMARY KEY
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcLogoRecord, java.lang.Integer> POS = createField("pos", org.jooq.impl.SQLDataType.INTEGER, this);

	/**
	 * An uncommented item
	 */
	public final org.jooq.TableField<org.openforis.collect.persistence.jooq.tables.records.OfcLogoRecord, byte[]> IMAGE = createField("image", org.jooq.impl.SQLDataType.BLOB, this);

	/**
	 * No further instances allowed
	 */
	private OfcLogo() {
		super("ofc_logo", org.openforis.collect.persistence.jooq.Collect.COLLECT);
	}

	/**
	 * No further instances allowed
	 */
	private OfcLogo(java.lang.String alias) {
		super(alias, org.openforis.collect.persistence.jooq.Collect.COLLECT, org.openforis.collect.persistence.jooq.tables.OfcLogo.OFC_LOGO);
	}

	@Override
	public org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcLogoRecord> getMainKey() {
		return org.openforis.collect.persistence.jooq.Keys.ofc_logo_pkey;
	}

	@Override
	@SuppressWarnings("unchecked")
	public java.util.List<org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcLogoRecord>> getKeys() {
		return java.util.Arrays.<org.jooq.UniqueKey<org.openforis.collect.persistence.jooq.tables.records.OfcLogoRecord>>asList(org.openforis.collect.persistence.jooq.Keys.ofc_logo_pkey);
	}

	@Override
	public org.openforis.collect.persistence.jooq.tables.OfcLogo as(java.lang.String alias) {
		return new org.openforis.collect.persistence.jooq.tables.OfcLogo(alias);
	}
}
