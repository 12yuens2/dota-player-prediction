package util;

import skadistats.clarity.decoder.Util;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;
import skadistats.clarity.processor.entities.Entities;
import skadistats.clarity.processor.runner.Context;

import java.util.Iterator;

/**
 * Utility class that contains methods for resolving Entity properties and types
 */
public class ClarityUtil {

    public static final int RADIANT = 2;
    public static final int DIRE = 3;

    /**
     * If the entity is player (CDOTAPlayer) entity
     * @param e
     * @return
     */
    public static boolean isPlayer(Entity e) {
        return getEntityName(e).startsWith("CDOTAPlayer");
    }

    /**
     * If the entity is a player on the radiant or dire teams
     * @param e
     * @return
     */
    public static boolean isGamePlayer(Entity e) {
    	if (isPlayer(e)) {
    		int playerTeamNum = getEntityProperty(e, "m_iTeamNum");
    		return playerTeamNum == RADIANT || playerTeamNum == DIRE;
    	}
    	return false;
    }
    public static String getTeamName(int team) {
        switch(team) {
            case 2: return "Radiant";
            case 3: return "Dire";
            default: return "";
        }
    }

    /**
     * Resolve patterned names such as m_vecPlayerData.0001.m_iPlayerTeam
     * @param ctx
     * @param entityName
     * @param pattern - Pattern with property names, %i for indices, %t for team and %p for positions
     * @param index - index to replace the %i in the pattern
     * @param team - team to replace the %t in the pattern
     * @param pos - pos to replace the %p in the pattern
     * @param <T>
     * @return
     */
    public static <T> T resolveValue(Context ctx, String entityName, String pattern, int index, int team, int pos) {
        String fieldPathString = pattern
                .replaceAll("%i", Util.arrayIdxToString(index))
                .replaceAll("%t", Util.arrayIdxToString(team))
                .replaceAll("%p", Util.arrayIdxToString(pos));
        String compiledName = entityName.replaceAll("%n", getTeamName(team));
        Entity entity = getEntity(ctx, compiledName);
        FieldPath fieldPath = entity.getDtClass().getFieldPathForName(fieldPathString);

        return entity.getPropertyForFieldPath(fieldPath);
    }

    /**
     * Get a property of an entity. Returns null if the property is not found.
     * @param e
     * @param property
     * @param <T>
     * @return
     */
    public static <T> T getEntityProperty(Entity e, String property) {
    	try {
            FieldPath f = e.getDtClass().getFieldPathForName(property);
            return e.getPropertyForFieldPath(f);
        } catch (Exception x) {
            return null;
        }
    }

    public static String getEntityName(Entity e) {
        return e.getDtClass().getDtName();
    }

    /**
     * Retrieve an entity from the game context with the entity name
     * @param ctx
     * @param entityName
     * @return
     */
    public static Entity getEntity(Context ctx, String entityName) {
        if (ctx != null) {
            return ctx.getProcessor(Entities.class).getByDtName(entityName);
        }
        return null;
    }

    /**
     * Retrieve all entities with a given name
     * @param ctx
     * @param entityName
     * @return Iterator over list of entities found
     */
    public static Iterator<Entity> getEntities(Context ctx, String entityName) {
        if (ctx != null) {
            return ctx.getProcessor(Entities.class).getAllByDtName(entityName);
        }
        return null;
    }
}
