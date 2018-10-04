package parser.mouse;

import parser.Parser;
import parser.PlayerData;
import skadistats.clarity.model.Entity;
import skadistats.clarity.wire.common.proto.DotaCommonMessages;
import skadistats.clarity.wire.common.proto.DotaUserMessages;
import util.ClarityUtil;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class MouseParser extends Parser {

    private PrintWriter mouseMovementWriter;

    public MouseParser(long filterSteamID) {
        super(filterSteamID);
    }

    public void writeMouseMovements(PlayerData pd, Entity player) {
        if (player != null && pd != null) {
            long steamID = pd.getSteamID();
            long tick = gameTick;
            int mouseX = ClarityUtil.getEntityProperty(player, "m_iCursor.0000");
            int mouseY = ClarityUtil.getEntityProperty(player, "m_iCursor.0001");

            mouseMovementWriter.write(steamID + "," + tick + "," + mouseX + "," + mouseY + "\n");
            mouseMovementWriter.flush();
        }
    }





    public void parseUnitOrder(DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders msg) {
        System.out.println(getOrderDescription(msg));
    }

    private static String getOrderDescription(DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders msg) {
        int orderType = msg.getOrderType();
        switch (orderType) {
            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_MOVE_TO_POSITION_VALUE:
                return "Move to position " + msg.getPosition().getX() + "," + msg.getPosition().getY();

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_MOVE_TO_TARGET_VALUE:
                return "Move to target " + msg.getTargetIndex();

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_ATTACK_MOVE_VALUE:
                return "Attack move";

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_ATTACK_TARGET_VALUE:
                return "Attack target " + msg.getTargetIndex();

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_CAST_POSITION_VALUE:
                return "Cast at position " + msg.getPosition().getX() + "," + msg.getPosition().getY();

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_CAST_TARGET_VALUE:
                return "Cast target " + msg.getTargetIndex();

            default:
                return "Order type " + orderType;
        }
    }

    public void initWriter(String filename) throws FileNotFoundException {
        mouseMovementWriter = new PrintWriter(filename);
        mouseMovementWriter.write("steamID,tick,mouseX,mouseY\n");
        mouseMovementWriter.flush();
    }

    public void closeWriter() {
        if (mouseMovementWriter != null) {
            mouseMovementWriter.flush();
            mouseMovementWriter.close();
        }
    }
}
