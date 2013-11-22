/*
 * SonarQube, open source software quality management tool.
 * Copyright (C) 2008-2013 SonarSource
 * mailto:contact AT sonarsource DOT com
 *
 * SonarQube is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * SonarQube is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.sonar.core.technicaldebt.db;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;

import java.io.Serializable;
import java.util.Date;

public class CharacteristicDto implements Serializable {

  private Long id;
  private String kee;
  private String name;
  private Integer parentId;
  private Integer characteristicOrder;
  private Integer ruleId;
  private String functionKey;
  private Double factorValue;
  private String factorUnit;
  private Double offsetValue;
  private String offsetUnit;
  private Date createdAt;
  private Date updatedAt;
  private boolean enabled;

  public Long getId() {
    return id;
  }

  public CharacteristicDto setId(Long id) {
    this.id = id;
    return this;
  }

  @CheckForNull
  public String getKey() {
    return kee;
  }

  public CharacteristicDto setKey(@Nullable String s) {
    this.kee = s;
    return this;
  }

  @CheckForNull
  public String getName() {
    return name;
  }

  public CharacteristicDto setName(@Nullable String s) {
    this.name = s;
    return this;
  }

  @CheckForNull
  public Integer getParentId() {
    return parentId;
  }

  public CharacteristicDto setParentId(@Nullable Integer i) {
    this.parentId = i;
    return this;
  }

  @CheckForNull
  public Integer getOrder() {
    return characteristicOrder;
  }

  public CharacteristicDto setOrder(@Nullable Integer i) {
    this.characteristicOrder = i;
    return this;
  }

  @CheckForNull
  public Integer getRuleId() {
    return ruleId;
  }

  public CharacteristicDto setRuleId(@Nullable Integer ruleId) {
    this.ruleId = ruleId;
    return this;
  }

  @CheckForNull
  public String getFunction() {
    return functionKey;
  }

  public CharacteristicDto setFunction(@Nullable String function) {
    this.functionKey = function;
    return this;
  }

  @CheckForNull
  public Double getFactorValue() {
    return factorValue;
  }

  public CharacteristicDto setFactorValue(Double factor) {
    this.factorValue = factor;
    return this;
  }

  @CheckForNull
  public String getFactorUnit() {
    return factorUnit;
  }

  public CharacteristicDto setFactorUnit(@Nullable String factorUnit) {
    this.factorUnit = factorUnit;
    return this;
  }

  @CheckForNull
  public Double getOffsetValue() {
    return offsetValue;
  }

  public CharacteristicDto setOffsetValue(@Nullable Double offset) {
    this.offsetValue = offset;
    return this;
  }

  @CheckForNull
  public String getOffsetUnit() {
    return offsetUnit;
  }

  public CharacteristicDto setOffsetUnit(@Nullable String offsetUnit) {
    this.offsetUnit = offsetUnit;
    return this;
  }

  public Date getCreatedAt() {
    return createdAt;
  }

  public CharacteristicDto setCreatedAt(Date createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  @CheckForNull
  public Date getUpdatedAt() {
    return updatedAt;
  }

  public CharacteristicDto setUpdatedAt(@Nullable Date updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public CharacteristicDto setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

}
