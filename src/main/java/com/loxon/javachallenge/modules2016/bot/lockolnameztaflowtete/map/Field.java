package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2016.bot.enums.FieldTeam;

/**
 * Class representing a field in a map.
 * Storing a field's @link{ObjectType} and @link{FieldTeam} values.
 * 
 * @author ihorvath
 *
 */
public class Field {
	
	private ObjectType objectType;
	
	private FieldTeam team;

	public ObjectType getObjectType() {
		return objectType;
	}

	public Field setObjectType(ObjectType objectType) {
		this.objectType = objectType;
		return this;
	}

	public FieldTeam getTeam() {
		return team;
	}

	public Field setTeam(FieldTeam team) {
		this.team = team;
		return this;
	}
	
	
}
