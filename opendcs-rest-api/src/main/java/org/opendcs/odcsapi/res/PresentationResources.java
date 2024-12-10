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
import java.util.Vector;

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

import decodes.db.DataPresentation;
import decodes.db.DataType;
import decodes.db.DatabaseException;
import decodes.db.DatabaseIO;
import decodes.db.PresentationGroup;
import decodes.db.PresentationGroupList;
import decodes.sql.DbKey;
import org.opendcs.odcsapi.beans.ApiPresentationElement;
import org.opendcs.odcsapi.beans.ApiPresentationGroup;
import org.opendcs.odcsapi.beans.ApiPresentationRef;
import org.opendcs.odcsapi.dao.DbException;
import org.opendcs.odcsapi.errorhandling.ErrorCodes;
import org.opendcs.odcsapi.errorhandling.WebAppException;
import org.opendcs.odcsapi.sec.AuthorizationCheck;

@Path("/")
public class PresentationResources extends OpenDcsResource
{
	@Context HttpHeaders httpHeaders;

	@GET
	@Path("presentationrefs")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
 	public Response getPresentationRefs() throws DbException
	{
		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			PresentationGroupList groupList = new PresentationGroupList();
			dbio.readPresentationGroupList(groupList);
			return Response.status(HttpServletResponse.SC_OK).entity(map(groupList)).build();
		}
		catch (DatabaseException e)
		{
			throw new DbException("Unable to retrieve presentation groups", e);
		}
	}

	static ArrayList<ApiPresentationRef> map(PresentationGroupList groupList)
	{
		ArrayList<ApiPresentationRef> ret = new ArrayList<>();
		for (PresentationGroup group : groupList.getVector())
		{
			ApiPresentationRef presRef = new ApiPresentationRef();
			presRef.setGroupId(group.getId().getValue());
			presRef.setName(group.groupName);
			presRef.setInheritsFrom(group.inheritsFrom);
			presRef.setProduction(group.isProduction);
			if (group.parent != null)
				presRef.setInheritsFromId(group.parent.getId().getValue());
			presRef.setLastModified(group.lastModifyTime);
			ret.add(presRef);
		}
		return ret;
	}

	@GET
	@Path("presentation")
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_GUEST})
	public Response getPresentation(@QueryParam("groupid") Long groupId)
		throws WebAppException, DbException
	{
		if (groupId == null)
			throw new WebAppException(ErrorCodes.MISSING_ID, 
				"Missing required groupid parameter.");
		
		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			PresentationGroup group = new PresentationGroup();
			group.setId(DbKey.createDbKey(groupId));
			dbio.readPresentationGroup(group);
			return Response.status(HttpServletResponse.SC_OK).entity(map(group)).build();
		}
		catch (DatabaseException e)
		{
			throw new DbException(String.format("Unable to retrieve presentation group with ID: %s", groupId), e);
		}
	}

	static ApiPresentationGroup map(PresentationGroup group)
	{
		ApiPresentationGroup presGrp = new ApiPresentationGroup();
		presGrp.setLastModified(group.lastModifyTime);
		presGrp.setName(group.groupName);
		presGrp.setProduction(group.isProduction);
		if (group.parent != null)
		{
			presGrp.setInheritsFrom(group.parent.groupName);
			presGrp.setInheritsFromId(group.parent.getId().getValue());
		}
		presGrp.setGroupId(group.getId().getValue());
		presGrp.setElements(map(group.dataPresentations));
		return presGrp;
	}

	static ArrayList<ApiPresentationElement> map(Vector<DataPresentation> dataPresentations)
	{
		ArrayList<ApiPresentationElement> ret = new ArrayList<>();
		for(DataPresentation dp : dataPresentations)
		{
			ApiPresentationElement ape = new ApiPresentationElement();
			ape.setDataTypeCode(dp.getDataType().getCode());
			ape.setDataTypeStd(dp.getDataType().getStandard());
			ape.setFractionalDigits(dp.getMaxDecimals());
			ape.setMax(dp.getMaxValue());
			ape.setMin(dp.getMinValue());
			ape.setUnits(dp.getUnitsAbbr());
			ret.add(ape);
		}
		return ret;
	}

	@POST
	@Path("presentation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response postPresentation(ApiPresentationGroup presGrp) throws DbException
	{
		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			dbio.writePresentationGroup(map(presGrp));
			return Response.status(HttpServletResponse.SC_OK)
					.entity("Successfully stored presentation group")
					.build();
		}
		catch (DatabaseException e)
		{
			throw new DbException("Unable to store presentation group", e);
		}
	}

	static PresentationGroup map(ApiPresentationGroup presGrp) throws DatabaseException
	{
		PresentationGroup group = new PresentationGroup();
		group.lastModifyTime = presGrp.getLastModified();
		group.groupName = presGrp.getName();
		group.setId(DbKey.createDbKey(presGrp.getGroupId()));
		group.isProduction = presGrp.isProduction();
		group.inheritsFrom = presGrp.getInheritsFrom();
		PresentationGroup apiGroup = new PresentationGroup();
		apiGroup.groupName = presGrp.getInheritsFrom();
		if (presGrp.getInheritsFromId() != null)
		{
			apiGroup.setId(DbKey.createDbKey(presGrp.getInheritsFromId()));
		}
		group.parent = apiGroup;
		group.dataPresentations = map(presGrp.getElements());

		return group;
	}

	static Vector<DataPresentation> map(ArrayList<ApiPresentationElement> elements) {
		Vector<DataPresentation> ret = new Vector<>();

		for (ApiPresentationElement ape : elements)
		{
			DataPresentation dataPres = new DataPresentation();
			dataPres.setUnitsAbbr(ape.getUnits());
			dataPres.setDataType(new DataType(ape.getDataTypeStd(), ape.getDataTypeCode()));
			dataPres.setMaxDecimals(ape.getFractionalDigits());
			dataPres.setMinValue(ape.getMin());
			dataPres.setMaxValue(ape.getMax());
			ret.add(dataPres);
		}
		return ret;
	}

	@DELETE
	@Path("presentation")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@RolesAllowed({AuthorizationCheck.ODCS_API_ADMIN, AuthorizationCheck.ODCS_API_USER})
	public Response deletePresentation(@QueryParam("groupid") Long groupId) throws DbException
	{
		try
		{
			DatabaseIO dbio = getLegacyDatabase();
			PresentationGroup group = new PresentationGroup();
			group.setId(DbKey.createDbKey(groupId));

			// TODO: Add support for this check
//			String s = dao.routSpecsUsing(groupId);
//			if (s != null)
//				return ApiHttpUtil.createResponse("Cannot delete presentation group " + groupId
//						+ " because it is used by the following routing specs: "
//						+ s, ErrorCodes.NOT_ALLOWED);
			dbio.deletePresentationGroup(group);
			return Response.status(HttpServletResponse.SC_OK)
					.entity("Presentation Group with ID " + groupId + " deleted")
					.build();
		}
		catch (DatabaseException e)
		{
			throw new DbException("Unable to delete presentation group", e);
		}
	}
}
