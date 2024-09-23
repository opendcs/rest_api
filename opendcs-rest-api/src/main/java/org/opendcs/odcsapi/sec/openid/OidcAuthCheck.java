/*
 *  Copyright 2024 OpenDCS Consortium and its Contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License")
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.opendcs.odcsapi.sec.openid;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.JWKSourceBuilder;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.DefaultJOSEObjectTypeVerifier;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimNames;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.opendcs.odcsapi.dao.ApiAuthorizationDAI;
import org.opendcs.odcsapi.hydrojson.DbInterface;
import org.opendcs.odcsapi.sec.OpenDcsApiRoles;
import org.opendcs.odcsapi.sec.OpenDcsPrincipal;
import org.opendcs.odcsapi.sec.OpenDcsSecurityContext;
import org.opendcs.odcsapi.sec.AuthorizationCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class OidcAuthCheck implements AuthorizationCheck
{

	private static final Logger LOGGER = LoggerFactory.getLogger(OidcAuthCheck.class);
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_PREFIX = "Bearer ";

	private JWTClaimsSet getClaimsSet(String accessToken)
			throws MalformedURLException, BadJOSEException, ParseException, JOSEException
	{
		// Nimbus API documentation taken from:
		// https://connect2id.com/products/nimbus-jose-jwt/examples/validating-jwt-access-tokens
		ConfigurableJWTProcessor<SecurityContext> jwtProcessor = new DefaultJWTProcessor<>();
		// Set the required "typ" header "at+jwt" for access tokens
		jwtProcessor.setJWSTypeVerifier(new DefaultJOSEObjectTypeVerifier<>(new JOSEObjectType("at+jwt")));
		String jwkSetUrl = System.getenv("OIDC_JWK_SET_URL");
		JWKSource<SecurityContext> keySource = JWKSourceBuilder
				.create(new URL(jwkSetUrl))
				.retrying(true)
				.build();
		// The expected JWS algorithm of the access tokens (agreed out-of-band)
		JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;
		JWSKeySelector<SecurityContext> keySelector = new JWSVerificationKeySelector<>(expectedJWSAlg, keySource);
		jwtProcessor.setJWSKeySelector(keySelector);
		String issuer = System.getenv("OIDC_ISSUER");
		jwtProcessor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
				new JWTClaimsSet.Builder().issuer(issuer).build(),
				new HashSet<>(Arrays.asList(
						JWTClaimNames.SUBJECT,
						JWTClaimNames.ISSUED_AT,
						JWTClaimNames.EXPIRATION_TIME,
						"scp",
						"cid",
						JWTClaimNames.JWT_ID))));
		return jwtProcessor.process(accessToken, null);
	}


	@Override
	public OpenDcsSecurityContext authorize(ContainerRequestContext requestContext, HttpServletRequest httpServletRequest)
	{
		String authorizationHeader = requestContext.getHeaderString(AUTHORIZATION_HEADER);
		if(authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX))
		{
			throw new NotAuthorizedException("No authorization header.");
		}
		else
		{
			try(DbInterface dbInterface = new DbInterface();
				ApiAuthorizationDAI authorizationDao = dbInterface.getAuthorizationDao())
			{
				String token = authorizationHeader.substring(BEARER_PREFIX.length());
				JWTClaimsSet claimsSet = getClaimsSet(token);
				String subject = claimsSet.getSubject();
				Set<OpenDcsApiRoles> roles = authorizationDao.getRoles(subject);
				OpenDcsPrincipal openDcsPrincipal = new OpenDcsPrincipal(subject, roles);
				return new OpenDcsSecurityContext(openDcsPrincipal,
						httpServletRequest.isSecure(), BEARER_PREFIX);
			}
			catch(ParseException | JOSEException | BadJOSEException e)
			{
				LOGGER.warn("Token processing error: ", e);
				throw new NotAuthorizedException("Invalid JWT.");
			}
			catch(MalformedURLException e)
			{
				throw new IllegalStateException("Invalid OIDC authentication URL", e);
			}
			catch(Exception e)
			{
				throw new ServerErrorException("Error accessing database to determine user roles",
						Response.Status.INTERNAL_SERVER_ERROR, e);
			}
		}
	}
}
