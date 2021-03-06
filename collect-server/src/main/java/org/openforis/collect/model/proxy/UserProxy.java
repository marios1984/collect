/**
 * 
 */
package org.openforis.collect.model.proxy;

import java.util.ArrayList;
import java.util.List;

import org.openforis.collect.Proxy;
import org.openforis.collect.model.User;

/**
 * 
 * @author S. Ricci
 *
 */
public class UserProxy implements Proxy {

	private Boolean enabled;
	private Integer id;
	private String name;
	private String password;
	private List<String> roles;

	public UserProxy(User user) {
		super();
		this.enabled = user.getEnabled();
		this.id = user.getId();
		this.name = user.getName();
		this.roles = user.getRoles();
		//password is not initialized, so the client will not know its value
	}

	public static List<UserProxy> fromList(List<User> users) {
		List<UserProxy> result = new ArrayList<UserProxy>();
		if ( users != null ) {
			for (User user : users) {
				UserProxy proxy = new UserProxy(user);
				result.add(proxy);
			}
		}
		return result;
	}
	
	public User toUser() {
		User user = new User();
		user.setEnabled(enabled);
		user.setId(id);
		user.setName(name);
		user.setPassword(password);
		user.setRoles(roles);
		return user;
	}
	
	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
