/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.lms.model;

import com.liferay.portal.kernel.bean.AutoEscape;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.BaseModel;
import com.liferay.portal.model.CacheModel;
import com.liferay.portal.service.ServiceContext;

import com.liferay.portlet.expando.model.ExpandoBridge;

import java.io.Serializable;

import java.util.Date;

/**
 * The base model interface for the P2pActivity service. Represents a row in the &quot;Lms_P2pActivity&quot; database table, with each column mapped to a property of this class.
 *
 * <p>
 * This interface and its corresponding implementation {@link com.liferay.lms.model.impl.P2pActivityModelImpl} exist only as a container for the default property accessors generated by ServiceBuilder. Helper methods and all application logic should be put in {@link com.liferay.lms.model.impl.P2pActivityImpl}.
 * </p>
 *
 * @author TLS
 * @see P2pActivity
 * @see com.liferay.lms.model.impl.P2pActivityImpl
 * @see com.liferay.lms.model.impl.P2pActivityModelImpl
 * @generated
 */
public interface P2pActivityModel extends BaseModel<P2pActivity> {
	/*
	 * NOTE FOR DEVELOPERS:
	 *
	 * Never modify or reference this interface directly. All methods that expect a p2p activity model instance should use the {@link P2pActivity} interface instead.
	 */

	/**
	 * Returns the primary key of this p2p activity.
	 *
	 * @return the primary key of this p2p activity
	 */
	public long getPrimaryKey();

	/**
	 * Sets the primary key of this p2p activity.
	 *
	 * @param primaryKey the primary key of this p2p activity
	 */
	public void setPrimaryKey(long primaryKey);

	/**
	 * Returns the uuid of this p2p activity.
	 *
	 * @return the uuid of this p2p activity
	 */
	@AutoEscape
	public String getUuid();

	/**
	 * Sets the uuid of this p2p activity.
	 *
	 * @param uuid the uuid of this p2p activity
	 */
	public void setUuid(String uuid);

	/**
	 * Returns the p2p activity ID of this p2p activity.
	 *
	 * @return the p2p activity ID of this p2p activity
	 */
	public long getP2pActivityId();

	/**
	 * Sets the p2p activity ID of this p2p activity.
	 *
	 * @param p2pActivityId the p2p activity ID of this p2p activity
	 */
	public void setP2pActivityId(long p2pActivityId);

	/**
	 * Returns the act ID of this p2p activity.
	 *
	 * @return the act ID of this p2p activity
	 */
	public long getActId();

	/**
	 * Sets the act ID of this p2p activity.
	 *
	 * @param actId the act ID of this p2p activity
	 */
	public void setActId(long actId);

	/**
	 * Returns the user ID of this p2p activity.
	 *
	 * @return the user ID of this p2p activity
	 */
	public long getUserId();

	/**
	 * Sets the user ID of this p2p activity.
	 *
	 * @param userId the user ID of this p2p activity
	 */
	public void setUserId(long userId);

	/**
	 * Returns the user uuid of this p2p activity.
	 *
	 * @return the user uuid of this p2p activity
	 * @throws SystemException if a system exception occurred
	 */
	public String getUserUuid() throws SystemException;

	/**
	 * Sets the user uuid of this p2p activity.
	 *
	 * @param userUuid the user uuid of this p2p activity
	 */
	public void setUserUuid(String userUuid);

	/**
	 * Returns the file entry ID of this p2p activity.
	 *
	 * @return the file entry ID of this p2p activity
	 */
	public long getFileEntryId();

	/**
	 * Sets the file entry ID of this p2p activity.
	 *
	 * @param fileEntryId the file entry ID of this p2p activity
	 */
	public void setFileEntryId(long fileEntryId);

	/**
	 * Returns the count corrections of this p2p activity.
	 *
	 * @return the count corrections of this p2p activity
	 */
	public long getCountCorrections();

	/**
	 * Sets the count corrections of this p2p activity.
	 *
	 * @param countCorrections the count corrections of this p2p activity
	 */
	public void setCountCorrections(long countCorrections);

	/**
	 * Returns the description of this p2p activity.
	 *
	 * @return the description of this p2p activity
	 */
	@AutoEscape
	public String getDescription();

	/**
	 * Sets the description of this p2p activity.
	 *
	 * @param description the description of this p2p activity
	 */
	public void setDescription(String description);

	/**
	 * Returns the date of this p2p activity.
	 *
	 * @return the date of this p2p activity
	 */
	public Date getDate();

	/**
	 * Sets the date of this p2p activity.
	 *
	 * @param date the date of this p2p activity
	 */
	public void setDate(Date date);

	/**
	 * Returns the asignations completed of this p2p activity.
	 *
	 * @return the asignations completed of this p2p activity
	 */
	public boolean getAsignationsCompleted();

	/**
	 * Returns <code>true</code> if this p2p activity is asignations completed.
	 *
	 * @return <code>true</code> if this p2p activity is asignations completed; <code>false</code> otherwise
	 */
	public boolean isAsignationsCompleted();

	/**
	 * Sets whether this p2p activity is asignations completed.
	 *
	 * @param asignationsCompleted the asignations completed of this p2p activity
	 */
	public void setAsignationsCompleted(boolean asignationsCompleted);

	public boolean isNew();

	public void setNew(boolean n);

	public boolean isCachedModel();

	public void setCachedModel(boolean cachedModel);

	public boolean isEscapedModel();

	public Serializable getPrimaryKeyObj();

	public void setPrimaryKeyObj(Serializable primaryKeyObj);

	public ExpandoBridge getExpandoBridge();

	public void setExpandoBridgeAttributes(ServiceContext serviceContext);

	public Object clone();

	public int compareTo(P2pActivity p2pActivity);

	public int hashCode();

	public CacheModel<P2pActivity> toCacheModel();

	public P2pActivity toEscapedModel();

	public String toString();

	public String toXmlString();
}