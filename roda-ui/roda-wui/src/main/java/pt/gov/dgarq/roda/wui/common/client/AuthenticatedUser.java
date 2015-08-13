/**
 * 
 */
package pt.gov.dgarq.roda.wui.common.client;

import pt.gov.dgarq.roda.core.data.User;

/**
 * @author Luis Faria
 * 
 */
public class AuthenticatedUser extends User {

	private boolean guest;
	private String pgt;
	
	

	public String getPgt() {
		return pgt;
	}

	public void setPgt(String pgt) {
		this.pgt = pgt;
	}

	/**
	 * @deprecated Only for GWT serialization purposes
	 * 
	 */
	@Deprecated
	public AuthenticatedUser() {
		super();
	}

	/**
	 * Create a new authenticated user
	 * 
	 * @param user
	 * @param guest
	 */
	public AuthenticatedUser(User user, boolean guest) {
		super(user);
		this.guest = guest;
	}

	/**
	 * Is authenticated user a guest
	 * 
	 * @return
	 */
	public boolean isGuest() {
		return guest;
	}

	/**
	 * Set if authenticated user is a guest
	 * 
	 * @param guest
	 */
	public void setGuest(boolean guest) {
		this.guest = guest;
	}

	public boolean hasRole(String role) {
		boolean found = false;
		String[] roles = this.getRoles();
		for (int i = 0; i < roles.length && !found; i++) {
			if (roles[i].equals(role)) {
				found = true;
			}
		}
		return found;
	}

	/**
	 * Check is authenticated user as all indicated roles
	 * 
	 * @param roles
	 * @return
	 */
	public boolean hasAllRoles(String[] roles) {
		boolean hasAll = true;
		for (int i = 0; i < roles.length && hasAll; i++) {
			hasAll = hasRole(roles[i]);
		}
		return hasAll;
	}

	/**
	 * Check is authenticated user as at least one of the indicated roles
	 * 
	 * @param roles
	 * @return
	 */
	public boolean hasAtLeastOneRole(String[] roles) {
		boolean found = false;
		for (int i = 0; i < roles.length && !found; i++) {
			found = hasRole(roles[i]);
		}
		return found;
	}

}
