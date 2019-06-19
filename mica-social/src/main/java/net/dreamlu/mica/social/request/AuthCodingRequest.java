package net.dreamlu.mica.social.request;

import com.fasterxml.jackson.databind.JsonNode;
import net.dreamlu.http.HttpRequest;
import net.dreamlu.mica.social.config.AuthConfig;
import net.dreamlu.mica.social.config.AuthSource;
import net.dreamlu.mica.social.exception.AuthException;
import net.dreamlu.mica.social.model.AuthToken;
import net.dreamlu.mica.social.model.AuthUser;
import net.dreamlu.mica.social.model.AuthUserGender;
import net.dreamlu.mica.social.utils.UrlBuilder;

/**
 * Cooding登录
 *
 * @author yadong.zhang (yadong.zhang0415(a)gmail.com), L.cm
 * @version 1.0
 * @since 1.8
 */
public class AuthCodingRequest extends BaseAuthRequest {

	public AuthCodingRequest(AuthConfig config) {
		super(config, AuthSource.CODING);
	}

	@Override
	public String authorize() {
		return UrlBuilder.getCodingAuthorizeUrl(config.getClientId(), config.getRedirectUri());
	}

	@Override
	protected AuthToken getAccessToken(String code) {
		String accessTokenUrl = UrlBuilder.getCodingAccessTokenUrl(config.getClientId(), config.getClientSecret(), code);

		JsonNode jsonNode = HttpRequest.get(accessTokenUrl)
			.execute()
			.asJsonNode();
		if (jsonNode.get("code").asInt() != 0) {
			throw new AuthException("Unable to get token from coding using code [" + code + "]");
		}
		return AuthToken.builder()
			.accessToken(jsonNode.get("access_token").asText())
			.build();
	}

	@Override
	protected AuthUser getUserInfo(AuthToken authToken) {
		String accessToken = authToken.getAccessToken();

		JsonNode jsonNode = HttpRequest.get(UrlBuilder.getCodingUserInfoUrl(accessToken))
			.execute()
			.asJsonNode();
		if (jsonNode.get("code").asInt() != 0) {
			throw new AuthException(jsonNode.get("msg").asText());
		}
		JsonNode data = jsonNode.get("data");
		return AuthUser.builder()
			.uuid(data.get("id").asText())
			.username(data.get("name").asText())
			.avatar("https://coding.net/" + data.get("avatar").asText())
			.blog("https://coding.net/" + data.get("path").asText())
			.nickname(data.get("name").asText())
			.company(data.get("company").asText())
			.location(data.get("location").asText())
			.gender(AuthUserGender.getRealGender(data.get("sex").asText()))
			.email(data.get("email").asText())
			.remark(data.get("slogan").asText())
			.token(authToken)
			.source(AuthSource.CODING)
			.build();
	}
}