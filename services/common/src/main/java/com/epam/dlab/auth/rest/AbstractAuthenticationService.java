package com.epam.dlab.auth.rest;

import java.util.UUID;

import javax.ws.rs.core.Response;

import com.epam.dlab.auth.UserInfo;
import com.epam.dlab.dto.UserCredentialDTO;

import io.dropwizard.Configuration;

public abstract class AbstractAuthenticationService<C extends Configuration> extends ConfigurableResource<C> {

	public final static String HTML_REDIRECT_HEAD = "<html><head><meta http-equiv=\"refresh\" content=\"0; url=%s\" /></head></html>";
	
	public final static String ACCESS_TOKEN_PARAMETER_PATTERN = "[&]?access_token=([^&]$|[^&]*)";

	public static String removeAccessTokenFromUrl(String url) {
		return url.replaceAll(ACCESS_TOKEN_PARAMETER_PATTERN, "").replace("?&", "?").replaceFirst("\\?$", "");
	}
	
	public static String addAccessTokenToUrl(String url, String token) {
		StringBuilder sb = new StringBuilder(url);
		if( ! url.contains("?")) {
			sb.append("?");
		} else {
			sb.append("&");			
		}
		sb.append("access_token=").append(token);
		return sb.toString();
	}
	
	public AbstractAuthenticationService(C config) {
		super(config);
	}

	public abstract String login(UserCredentialDTO credential);
	public abstract UserInfo getUserInfo(String access_token);
	public abstract Response logout(String access_token);

	public UserInfo forgetAccessToken(String token) {
		return AuthorizedUsers.getInstance().removeUserInfo(token);
	}
	
	public void rememberUserInfo(String token, UserInfo user) {
		AuthorizedUsers.getInstance().addUserInfo(token, user.withToken(token));
	}
	
	public boolean isAccessTokenAvailable(String token) {
		UserInfo ui = AuthorizedUsers.getInstance().getUserInfo(token);
		return ui != null;
	}
	
	public static String getRandomToken() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
	
}