package resolver;

/**
 * The ValueResolver class is used to resolve Entity Property names such as m_vecPlayerTeamData.0001
 * Adapted from https://github.com/skadistats/clarity-examples
 * @param <V>
 */
public interface ValueResolver<V> {
    V resolveValue(int index, int team, int pos);
}
