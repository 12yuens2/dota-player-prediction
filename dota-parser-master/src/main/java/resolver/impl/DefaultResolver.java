package resolver.impl;

import parser.Parser;
import resolver.ValueResolver;
import skadistats.clarity.decoder.Util;
import skadistats.clarity.model.Entity;
import skadistats.clarity.model.FieldPath;
import skadistats.clarity.processor.runner.Context;

/**
 * Default implementation of the ValueResolver. Adapted from https://github.com/skadistats/clarity-examples
 * @param <V>
 */
public class DefaultResolver<V> implements ValueResolver<V> {

    private Context ctx;
    private final String entityName;
    private final String pattern;

    public DefaultResolver(Context ctx, String entityName, String pattern) {
        this.ctx = ctx;

        this.entityName = entityName;
        this.pattern = pattern;
    }

    @Override
    public V resolveValue(int index, int team, int pos) {
        String fieldPathString = pattern
                .replaceAll("%i", Util.arrayIdxToString(index))
                .replaceAll("%t", Util.arrayIdxToString(team))
                .replaceAll("%p", Util.arrayIdxToString(pos));
        String compiledName = entityName.replaceAll("%n", getTeamName(team));
        Entity entity = Parser.getEntity(ctx, compiledName);
        FieldPath fieldPath = entity.getDtClass().getFieldPathForName(fieldPathString);
        return entity.getPropertyForFieldPath(fieldPath);
    }

    private String getTeamName(int team) {
        switch(team) {
            case 2: return "Radiant";
            case 3: return "Dire";
            default: return "";
        }
    }

}
