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
import decodes.db.DbEnum;
import decodes.db.EnumList;
import decodes.db.EnumValue;
import decodes.sql.DbKey;
import decodes.tsdb.DbIoException;
import opendcs.dai.EnumDAI;
import org.opendcs.odcsapi.beans.ApiRefList;
import org.opendcs.odcsapi.beans.ApiRefListItem;
import org.opendcs.odcsapi.beans.ApiSeason;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.sec.AuthorizationCheck;

/**
 * HTTP Resources relating to reference lists and seasons
 * @author mmaloney
 *
 */
@Path("/")
public class ReflistResources extends OpenDcsResource
{
	@Context HttpHeaders httpHeaders;

	@GET
	@Path("reflists")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getRefLists(@QueryParam("name") String listNames) throws DbException
	{
		try (EnumDAI dai = getLegacyTimeseriesDB().makeEnumDAO())
		{
			DbEnum returnedEnum = dai.getEnum("season");
			HashMap<String, ApiRefList> ret = new HashMap<>();
			for (EnumValue enumVal : returnedEnum.values())
			{
				ApiRefList refList = new ApiRefList();
				refList.setReflistId(returnedEnum.getId().getValue());
				refList.setDescription(enumVal.getDescription());
				refList.setDefaultValue(enumVal.getValue());
				ret.put(enumVal.getValue(), refList);
			}

			ArrayList<String> searches = getSearchTerms(listNames);
			if (!searches.isEmpty())
			{
				ArrayList<String> toRm = new ArrayList<>();
			nextName:
				for(String rlname : ret.keySet())
				{
					for(String term : searches)
						if (rlname.equalsIgnoreCase(term))
							continue nextName;
					toRm.add(rlname);
				}
				for(String rm : toRm)
					ret.remove(rm);
			}
					
			return Response.status(HttpServletResponse.SC_OK).entity(ret).build();
		}
		catch(DbIoException e)
		{
			throw new DbException("Unable to retrieve reference lists", e);
		}
	}
	
	@POST
	@Path("reflist")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postRefList(ApiRefList reflist) throws DbException
	{
		try (EnumDAI dai = getLegacyTimeseriesDB().makeEnumDAO())
		{
			dai.writeEnumList(mapToEnum(reflist));
			return Response.status(HttpServletResponse.SC_OK)
					.entity("Successfully stored reference list")
					.build();
		}
		catch(DbIoException | DatabaseException e)
		{
			throw new DbException("Unable to write reference list", e);
		}
	}

	static EnumList mapToEnum(ApiRefList refList) throws DatabaseException
	{
		EnumList el = new EnumList();
		for (Map.Entry<String, ApiRefListItem> item : refList.getItems().entrySet())
		{
			DbEnum ev = new DbEnum(item.getKey());
			ev.setDescription(item.getValue().getDescription());
			ev.setDefault(refList.getDefaultValue());
			ev.setId(DbKey.createDbKey(refList.getReflistId()));
			el.addEnum(ev);
		}
		return el;
	}

	@DELETE
	@Path("reflist")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleReflist(@QueryParam("reflistid") Long reflistId) throws DbException
	{
		try (EnumDAI dai = getLegacyTimeseriesDB().makeEnumDAO())
		{
//			dao.deleteRefList(reflistId);

//			return Response.status(HttpServletResponse.SC_OK).entity("reflist with ID " + reflistId + " deleted").build();
			return Response.status(HttpServletResponse.SC_NOT_IMPLEMENTED).build();
		}
	}

	@GET
	@Path("seasons")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getSeasons() throws DbException
	{
		try (EnumDAI dai = getLegacyTimeseriesDB().makeEnumDAO())
		{
			EnumList input = new EnumList();
			dai.readEnumList(input);
			return Response.status(HttpServletResponse.SC_OK).entity(mapSeasons(input)).build();
		}
		catch(DbIoException e)
		{
			throw new DbException("Unable to retrieve seasons", e);
		}
	}

	static ArrayList<ApiSeason> mapSeasons(EnumList seasons)
	{
		ArrayList<ApiSeason> ret = new ArrayList<>();
		for (DbEnum ev : seasons.getEnumList())
		{
			ApiSeason as = new ApiSeason();
			as.setName(ev.getUniqueName());
			ret.add(as);
		}
		return ret;
	}

	@GET
	@Path("season")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getSeason(@QueryParam("abbr") String abbr)
		throws DbException
	{
		try (EnumDAI dai = getLegacyTimeseriesDB().makeEnumDAO())
		{
			EnumList input = new EnumList();
			dai.readEnumList(input);
//			return Response.status(HttpServletResponse.SC_OK).entity(dao.getSeason(abbr)).build();
			return Response.status(HttpServletResponse.SC_NOT_IMPLEMENTED).build();
		}
		catch(DbIoException e)
		{
			throw new DbException(String.format("Unable to retrieve season with abbreviation: %s", abbr), e);
		}
	}

	@POST
	@Path("season")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postSeason(@QueryParam("fromabbr") String fromAbbr, ApiSeason season)
		throws DbException
	{
		try (EnumDAI dai = getLegacyTimeseriesDB().makeEnumDAO())
		{
//			if (fromAbbr != null)
//				reflistDAO.deleteSeason(fromAbbr);
//			reflistDAO.writeSeason(season);
//			return Response.status(HttpServletResponse.SC_OK)
//					.entity(String.format("The season (%s) has been saved successfully", season.getAbbr()))
//					.build();
			return Response.status(HttpServletResponse.SC_NOT_IMPLEMENTED).build();
		}
	}

	static DbEnum map(ApiSeason season, String fromAbbr)
	{
		DbEnum ret = new DbEnum(season.getName());
		ret.setDefault(fromAbbr);
		return ret;
	}

	@DELETE
	@Path("season")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deleteSeason(@QueryParam("abbr") String abbr)
		throws WebAppException, DbException
	{
		if (abbr == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, "Provide 'abbr' argument to delete a season.");
		
		// Use username and password to attempt to connect to the database
		try (EnumDAI dai = getLegacyTimeseriesDB().makeEnumDAO())
		{
//			reflistDAO.deleteSeason(abbr);
//			return Response.status(HttpServletResponse.SC_OK).entity("Deleted season " + abbr).build();
			return Response.status(HttpServletResponse.SC_NOT_IMPLEMENTED).build();
		}
	}

	
	/**
	 * Passed a list like ("one", "two", "three"), return the quoted strings as an array list.
	 * Parans may also be square brackets.
	 * Parens may be omitted. E.g. one,two,three
	 * Terms are optionally enclosed in quotes.
	 * @param theArg
	 * @return ArrayList of search terms. Empty if empty string.
	 */
	private ArrayList<String> getSearchTerms(String theArg)
	{
		ArrayList<String> ret = new ArrayList<>();
		
		if (theArg == null)
			return ret;
		theArg = theArg.trim();
		if (theArg.isEmpty())
			return ret;
		
		if (theArg.charAt(0) == '[' || theArg.charAt(0) == '(')
		{
		    theArg = theArg.substring(1);
			int idx = findCloseBracket(theArg);
			if (idx < theArg.length())
			    theArg = theArg.substring(0, idx);
		}
		
		// Now parse into quoted strings
		int start = 0;
		while(start < theArg.length())
		{
			char c = theArg.charAt(start);
			if (c == '"')
			{
				int end = start+1;
				boolean escaped = false;
				while(end < theArg.length() && 
					(theArg.charAt(end) != '"' || escaped))
				{
					if (!escaped && theArg.charAt(end) == '\\')
						escaped = true;
					else
						escaped = false;
					end++;
				}
				ret.add(theArg.substring(start+1, end).toLowerCase());
				start = end+1;
			}
			else if (Character.isLetterOrDigit(c)||c=='-'||c=='+')
			{
				int end = start+1;
				boolean escaped = false;
				while(end < theArg.length() && (theArg.charAt(end) != ',' || escaped))
				{
					if (!escaped && theArg.charAt(end) == '\\')
						escaped = true;
					else
						escaped = false;
					end++;
				}
				ret.add(theArg.substring(start, end).toLowerCase());
				start = end;
			}
			else
			{
				if (c == ',' || c == ' ' || c == '\t')
					start++;
				else
				{
					System.err.println("Parse error in argument '" + theArg + "' at position " + start);
					return ret;
				}
			}
		}
		
		return ret;
	}

	
	/**
	 * Pass string that has been trimmed to start just AFTER the open bracket.
	 * @param arg
	 * @return the index of the close bracket or length of string if not found.
	 */
	private int findCloseBracket(String arg)
	{
		int level = 1;
		int idx = 0;
		for(; idx < arg.length() && level > 0; idx++)
		{
			char c = arg.charAt(idx);
			if (c == '[' || c == '(')
				level++;
			else if (c == ']' || c == ')')
			{
				if (--level == 0)
					return idx;
			}
		}
		return idx;
	}	

}
