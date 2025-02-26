/*
 *  Copyright 2025 OpenDCS Consortium and its Contributors
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

package org.opendcs.odcsapi.beans;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;

/** Used to encapsulate a raw message returned by GET message or sent to POST decode */
@Schema(description = "Encapsulates a raw message, including metadata about the message and transmission details.")
public final class ApiRawMessage
{
	// Attributes - always present:
	@Schema(description = "Flags representing the status or state of the raw message.")
	private long flags = 0L;

	@Schema(description = "Identifier for the platform associated with this message.")
	private String platformId = null;

	// GOES fields:
	@Schema(description = "Sequence number of the message if applicable.")
	private Integer sequenceNum = null;

	@Schema(description = "Local time when the message was received.")
	private Date localRecvTime = null;

	@Schema(description = "Start time of the carrier signal for this message.")
	private Date carrierStart = null;

	@Schema(description = "Stop time of the carrier signal for this message.")
	private Date carrierStop = null;

	@Schema(description = "Baud rate of the carrier signal.")
	private Integer baud = null;

	@Schema(description = "Percentage of good phase signals received.")
	private Double goodPhasePct = null;

	@Schema(description = "Frequency offset of the signal in Hz.")
	private Double freqOffset = null;

	@Schema(description = "Strength of the signal received.")
	private Double signalStrength = null;

	@Schema(description = "Phase noise level of the signal received.")
	private Double phaseNoise = null;

	@Schema(description = "Timestamp when the message was transmitted.")
	private Date xmitTime = null;

	// Iridium fields:
	@Schema(description = "Mobile Originating Message Sequence Number for Iridium messages.")
	private Integer momsn = null;

	@Schema(description = "Mobile Terminating Message Sequence Number for Iridium messages.")
	private Integer mtmsn = null;

	@Schema(description = "Call Data Record (CDR) reference number for tracking.")
	private Long cdrReference = null;

	@Schema(description = "Status of the Iridium session.")
	private Integer sessionStatus = null;

	// Base64 encoded binary message to preserve original whitespace
	@Schema(description = "Base64-encoded representation of the raw binary message to preserve formatting and content.")
	private String base64 = null;

	public String getBase64()
	{
		return base64;
	}

	public void setBase64(String base64)
	{
		this.base64 = base64;
	}

	public long getFlags()
	{
		return flags;
	}

	public void setFlags(long flags)
	{
		this.flags = flags;
	}

	public String getPlatformId()
	{
		return platformId;
	}

	public void setPlatformId(String platformId)
	{
		this.platformId = platformId;
	}

	public Integer getSequenceNum()
	{
		return sequenceNum;
	}

	public void setSequenceNum(Integer sequenceNum)
	{
		this.sequenceNum = sequenceNum;
	}

	public Date getLocalRecvTime()
	{
		return localRecvTime;
	}

	public void setLocalRecvTime(Date localRecvTime)
	{
		this.localRecvTime = localRecvTime;
	}

	public Date getCarrierStart()
	{
		return carrierStart;
	}

	public void setCarrierStart(Date carrierStart)
	{
		this.carrierStart = carrierStart;
	}

	public Date getCarrierStop()
	{
		return carrierStop;
	}

	public void setCarrierStop(Date carrierStop)
	{
		this.carrierStop = carrierStop;
	}

	public Integer getBaud()
	{
		return baud;
	}

	public void setBaud(Integer baud)
	{
		this.baud = baud;
	}

	public Double getGoodPhasePct()
	{
		return goodPhasePct;
	}

	public void setGoodPhasePct(Double goodPhasePct)
	{
		this.goodPhasePct = goodPhasePct;
	}

	public Double getFreqOffset()
	{
		return freqOffset;
	}

	public void setFreqOffset(Double freqOffset)
	{
		this.freqOffset = freqOffset;
	}

	public Double getSignalStrength()
	{
		return signalStrength;
	}

	public void setSignalStrength(Double signalStrength)
	{
		this.signalStrength = signalStrength;
	}

	public Double getPhaseNoise()
	{
		return phaseNoise;
	}

	public void setPhaseNoise(Double phaseNoise)
	{
		this.phaseNoise = phaseNoise;
	}

	public Date getXmitTime()
	{
		return xmitTime;
	}

	public void setXmitTime(Date xmitTime)
	{
		this.xmitTime = xmitTime;
	}

	public Integer getMomsn()
	{
		return momsn;
	}

	public void setMomsn(Integer momsn)
	{
		this.momsn = momsn;
	}

	public Integer getMtmsn()
	{
		return mtmsn;
	}

	public void setMtmsn(Integer mtmsn)
	{
		this.mtmsn = mtmsn;
	}

	public Long getCdrReference()
	{
		return cdrReference;
	}

	public void setCdrReference(Long cdrReference)
	{
		this.cdrReference = cdrReference;
	}

	public Integer getSessionStatus()
	{
		return sessionStatus;
	}

	public void setSessionStatus(Integer sessionStatus)
	{
		this.sessionStatus = sessionStatus;
	}

}
