package org.openforis.collect.persistence;


import static org.openforis.collect.persistence.jooq.Sequences.OFC_USER_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Sequences.OFC_USER_ROLE_ID_SEQ;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER;
import static org.openforis.collect.persistence.jooq.Tables.OFC_USER_ROLE;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import org.jooq.DeleteQuery;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.SimpleSelectQuery;
import org.jooq.StoreQuery;
import org.jooq.impl.Factory;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.UserDao.JooqFactory;
import org.openforis.collect.persistence.jooq.MappingJooqDaoSupport;
import org.openforis.collect.persistence.jooq.MappingJooqFactory;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRecord;
import org.openforis.collect.persistence.jooq.tables.records.OfcUserRoleRecord;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author M. Togna
 * @author S. Ricci
 * 
 */
@Transactional
public class UserDao extends MappingJooqDaoSupport<User, JooqFactory> {

	public UserDao() {
		super(UserDao.JooqFactory.class);
	}

	@Transactional
	public User loadById(int id){
		JooqFactory jf = getMappingJooqFactory();
		SimpleSelectQuery<?> query = jf.selectByIdQuery(id);
		Record r = query.fetchOne();
		if(r == null){
			return null;
		} else {
			User user = jf.fromRecord(r);
			return user;
		}
	}
	
	@Transactional
	public User loadByUserName(String userName){
		JooqFactory jf = getMappingJooqFactory();
		SimpleSelectQuery<?> query = jf.selectByFieldQuery(OFC_USER.USERNAME, userName); 
		Record r = query.fetchOne();
		if(r == null){
			return null;
		} else {
			User user = jf.fromRecord(r);
			return user;
		}
	}
	
	@Transactional
	public List<User> loadAll() {
		JooqFactory jf = getMappingJooqFactory();
		Result<OfcUserRecord> r = jf.selectFrom(OFC_USER).fetch();
		if (r == null) {
			return null;
		} else {
			List<User> users = jf.fromResult(r);
			return users;
		}
	}
	
	@Transactional
	public int getUserId(String username) {
		Factory jooqFactory = getJooqFactory();
		Record record = jooqFactory.select(OFC_USER.ID).from(OFC_USER).where(OFC_USER.USERNAME.equal(username)).fetchOne();
		Integer id = record.getValueAsInteger(OFC_USER.ID);
		return id;
	}

	@Override
	public void insert(User user) {
		super.insert(user);
		JooqFactory jf = getMappingJooqFactory();
		jf.saveRoles(user);
	}

	@Override
	public void update(User user) {
		super.update(user);
		JooqFactory jf = getMappingJooqFactory();
		jf.saveRoles(user);
	}

	@Override
	public void delete(int id) {
		JooqFactory jf = getMappingJooqFactory();
		jf.deleteRoles(id);
		super.delete(id);
	}
	
	public static class JooqFactory extends MappingJooqFactory<User> {

		private static final long serialVersionUID = 1L;

		public JooqFactory(Connection conn) {
			super(conn, OFC_USER.ID, OFC_USER_ID_SEQ, User.class);
		}

		@Override
		protected void setId(User user, int id) {
			user.setId(id);
		}

		@Override
		protected Integer getId(User user) {
			return user.getId();
		}

		@Override
		protected void fromRecord(Record r, User user) {
			user.setId(r.getValueAsInteger(OFC_USER.ID));
			user.setName(r.getValueAsString(OFC_USER.USERNAME));
			user.setPassword(r.getValueAsString(OFC_USER.PASSWORD));
			
			loadRoles(user);
		}

		@Override
		protected void fromObject(User user, StoreQuery<?> q) {
			q.addValue(OFC_USER.ID, user.getId());
			q.addValue(OFC_USER.USERNAME, user.getName());
			q.addValue(OFC_USER.PASSWORD, user.getPassword());
		}
		
		protected void loadRoles(User user) {
			SimpleSelectQuery<OfcUserRoleRecord> query = selectQuery(OFC_USER_ROLE);
			query.addConditions(OFC_USER_ROLE.USER_ID.equal(user.getId()));
			Result<OfcUserRoleRecord> result = query.fetch();
			List<String> roles = new ArrayList<String>();
			for (OfcUserRoleRecord ofcUserRoleRecord : result) {
				String role = ofcUserRoleRecord.getRole();
				roles.add(role);
			}
			user.setRoles(roles);
		}
		
		protected void saveRoles(User user) {
			Integer userId = user.getId();
			deleteRoles(userId);
			List<String> roles = user.getRoles();
			for (String role : roles) {
				int userRoleId = nextval(OFC_USER_ROLE_ID_SEQ).intValue();
				insertInto(OFC_USER_ROLE, 
							OFC_USER_ROLE.ID, 
							OFC_USER_ROLE.USER_ID, 
							OFC_USER_ROLE.ROLE)
					.values(userRoleId, userId, role)
					.execute();
			}
		}
		
		protected void deleteRoles(int userId) {
			DeleteQuery<OfcUserRoleRecord> deleteQuery = deleteQuery(OFC_USER_ROLE);
			deleteQuery.addConditions(OFC_USER_ROLE.USER_ID.equal(userId));
			deleteQuery.execute();
		}
	}
}
