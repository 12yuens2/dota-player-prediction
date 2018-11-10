package parser.stats;

import parser.MainParser;
import skadistats.clarity.wire.common.proto.DotaCommonMessages;
import skadistats.clarity.wire.common.proto.DotaUserMessages;

public class PlayerStats {

    protected float apm;
    protected int movePos, moveTarget, attackPos, attackTarget, castPos, castTarget, castNoTarget, holdPos;

    private int numActions, numMinutes;

    public PlayerStats() {
        this.numActions = 0;
        this.numMinutes = 1;
    }

    public String getStats(float duration) {
        return String.format("%f,%d,%d, %d,%d, %d,%d,%d, %d\n",
                numActions/duration,
                movePos, moveTarget,
                attackPos, attackTarget,
                castPos, castTarget, castNoTarget,
                holdPos);
    }

    public void update(int gameTick, DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders msg) {
        numActions++;

        switch(msg.getOrderType()) {
            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_MOVE_TO_POSITION_VALUE:
                movePos++;
                break;

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_MOVE_TO_TARGET_VALUE:
                moveTarget++;
                break;

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_ATTACK_MOVE_VALUE:
                attackPos++;
                break;

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_ATTACK_TARGET_VALUE:
                attackTarget++;
                break;

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_CAST_POSITION_VALUE:
                castPos++;
                break;

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_CAST_TARGET_VALUE:
                castTarget++;
                break;

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_CAST_NO_TARGET_VALUE:
                castNoTarget++;
                break;

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_HOLD_POSITION_VALUE:
                holdPos++;
                break;
        }
    }

    public void updateAPM(int gameTick) {
       numActions++;
        if (gameTick % MainParser.TICK_RATE == 0) {
            numMinutes++;
        }

        apm = numActions / numMinutes;
    }

}
