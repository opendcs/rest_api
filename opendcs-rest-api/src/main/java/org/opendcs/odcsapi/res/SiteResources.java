/*
 *  Copyright 2023 OpenDCS Consortium
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
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

package org.opendcs.odcsapi.res;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import decodes.db.DatabaseException;
import decodes.db.Site;
import decodes.db.SiteList;
import decodes.db.SiteName;
import decodes.sql.DbKey;
import decodes.tsdb.DbIoException;
import decodes.tsdb.NoSuchObjectException;
import opendcs.dai.SiteDAI;
import org.opendcs.odcsapi.beans.ApiSite;
import org.opendcs.odcsapi.beans.ApiSiteRef;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.sec.AuthorizationCheck;

@Path("/")
public class SiteResources extends OpenDcsResource
{
	@Context HttpHeaders httpHeaders;

	@GET
	@Path("siterefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response geSiteRefs()
		throws DbException
	{
		try (SiteDAI dai = getLegacyTimeseriesDB().makeSiteDAO())
		{
			SiteList sites = new SiteList();
			dai.read(sites);
			List<ApiSiteRef> siteRefs = map(sites);
			return Response.status(HttpServletResponse.SC_OK).entity(siteRefs).build();
		}
		catch (DbIoException ex)
		{
			throw new DbException("Unable to retrieve sites", ex);
		}
	}

	static List<ApiSiteRef> map(SiteList sites)
	{
		List<ApiSiteRef> retList = new ArrayList<>();
		for(Iterator<Site> it = sites.iterator(); it.hasNext(); )
		{
			final Site site = it.next();
			ApiSiteRef siteRef = new ApiSiteRef();
			siteRef.setSiteId(site.getId().getValue());
			siteRef.setPublicName(site.getPublicName());
			siteRef.setDescription(site.getDescription());
			HashMap<String, String> siteNames = new HashMap<>();
			for(Iterator<SiteName> iter = site.getNames(); iter.hasNext(); )
			{
				final SiteName sn = iter.next();
				siteNames.put(sn.getNameType(), sn.getNameValue());
			}
			siteRef.setSitenames(siteNames);
			retList.add(siteRef);
		}
		return retList;
	}

	@GET
	@Path("site")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response geSiteFull(@QueryParam("siteid") Long siteId)
		throws WebAppException, DbException
	{
		if (siteId == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required siteid parameter.");

		try (SiteDAI dai = getLegacyTimeseriesDB().makeSiteDAO())
		{
			Site returnedSite = dai.getSiteById(DbKey.createDbKey(siteId));
			return Response.status(HttpServletResponse.SC_OK)
					.entity(map(returnedSite)).build();
		}
		catch(DbIoException | NoSuchObjectException e)
		{
			throw new DbException("Unable to retrieve site by ID", e);
		}
	}

	static ApiSite map(Site site)
	{
		ApiSite returnSite = new ApiSite();
		returnSite.setSiteId(site.getId().getValue());
		returnSite.setLocationtype(site.getLocationType());
		returnSite.setElevation(site.getElevation());
		returnSite.setElevUnits(site.getElevationUnits());
		returnSite.setActive(site.isActive());
		returnSite.setDescription(site.getDescription());
		returnSite.setLastModified(site.getLastModifyTime());
		returnSite.setCountry(site.country);
		returnSite.setState(site.state);
		returnSite.setNearestCity(site.nearestCity);
		returnSite.setLatitude(site.latitude);
		returnSite.setLongitude(site.longitude);
		returnSite.setTimezone(site.timeZoneAbbr);
		returnSite.setRegion(site.region);
		returnSite.setPublicName(site.getPublicName());
		HashMap<String, String> siteNames = new HashMap<>();
		for(Iterator<SiteName> iter = site.getNames(); iter.hasNext(); )
		{
			final SiteName sn = iter.next();
			siteNames.put(sn.getNameType(), sn.getNameValue());
		}
		returnSite.setSitenames(siteNames);
		return returnSite;
	}

	@POST
	@Path("site")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postSite(ApiSite site)
		throws DbException, WebAppException
	{
		try (SiteDAI dai = getLegacyTimeseriesDB().makeSiteDAO())
		{
			if (site == null) {
				throw new WebAppException(ErrorCodes.MISSING_ID, "Missing required site parameter.");
			}
			Site dbSite = map(site);
			dai.writeSite(dbSite);
			site.setSiteId(dbSite.getId().getValue());
			return Response.status(HttpServletResponse.SC_OK)
				.entity(site).build();
		}
		catch(DatabaseException | DbIoException e)
		{
			throw new DbException("Unable to store site", e);
		}
	}

	static Site map(ApiSite site) throws DatabaseException
	{
		Site returnSite = new Site();
		if (site.getSiteId() != null)
		{
			returnSite.setId(DbKey.createDbKey(site.getSiteId()));
		}
		returnSite.setLocationType(site.getLocationType());
		returnSite.setElevation(site.getElevation());
		returnSite.setElevationUnits(site.getElevUnits());
		returnSite.setActive(site.isActive());
		returnSite.setDescription(site.getDescription());
		returnSite.setLastModifyTime(site.getLastModified());
		returnSite.country = site.getCountry();
		returnSite.state = site.getState();
		returnSite.isNew = true;
		returnSite.nearestCity = site.getNearestCity();
		returnSite.latitude = site.getLatitude();
		returnSite.longitude = site.getLongitude();
		returnSite.timeZoneAbbr = site.getTimezone();
		returnSite.setPublicName(site.getPublicName());
		for (Map.Entry<String, String> entry : site.getSitenames().entrySet())
		{
			Site newSite = new Site();
			newSite.setLocationType(entry.getKey());
			newSite.setPublicName(entry.getValue());
			SiteName sn = new SiteName(newSite, entry.getKey());
			sn.setNameValue(entry.getValue());
			returnSite.addName(sn);
		}
		return returnSite;
	}

	@DELETE
	@Path("site")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleteSite(@QueryParam("siteid") Long siteId) throws DbException
	{
		try (SiteDAI dai = getLegacyTimeseriesDB().makeSiteDAO())
		{
			if (siteId == null)
				throw new WebAppException(ErrorCodes.MISSING_ID,
					"Missing required siteid parameter.");
			dai.deleteSite(DbKey.createDbKey(siteId));
			return Response.status(HttpServletResponse.SC_OK)
					.entity("ID " + siteId + " deleted").build();
		}
		catch(DbIoException | WebAppException e)
		{
			throw new DbException("Unable to delete site", e);
		}
	}
	

}
