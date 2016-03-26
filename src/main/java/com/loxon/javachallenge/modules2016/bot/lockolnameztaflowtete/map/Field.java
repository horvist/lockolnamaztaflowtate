package com.loxon.javachallenge.modules2016.bot.lockolnameztaflowtete.map;

import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.ObjectType;
import com.loxon.javachallenge.modules2015.ws.centralcontrol.gen.WsCoordinate;
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

	private final int x;
	private final int y;

	public Field(int x, int y) {
		this.team = FieldTeam.NO_MANS_LAND;
		this.x = x;
		this.y = y;
	}

	public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public WsCoordinate getWsCoord() {
        WsCoordinate coord = new WsCoordinate();
        coord.setX(x);
        coord.setY(y);
        return coord;
    }

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

	@Override
    public Field clone() {
		Field newField = new Field(x, y);
		newField.setTeam(this.team);
		newField.setObjectType(this.objectType);

		return newField;
	}

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((objectType == null) ? 0 : objectType.hashCode());
        result = prime * result + ((team == null) ? 0 : team.hashCode());
        result = prime * result + x;
        result = prime * result + y;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Field other = (Field) obj;
        if (objectType != other.objectType)
            return false;
        if (team != other.team)
            return false;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        return true;
    }

    @Override
    public String toString() {
//		String ret = "";
//		if (objectType == null) {
//			ret += "_";
//		} else {
//			ret += objectType.name().substring(0, 1);
//		}
//		if (team == null) {
//			ret += "_";
//		} else {
//			ret += team.name().substring(0, 1);
//		}
//
//		return ret;

		return x + ":" + y;
	}


}
