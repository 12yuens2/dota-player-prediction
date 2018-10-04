package parser.mouse;

import parser.Parser;
import parser.PlayerData;
import parser.mouse.atomic.MousePosition;
import parser.mouse.level1.MouseMoveSequence;
import parser.mouse.level3.MouseMoveAttack;
import skadistats.clarity.model.Entity;
import skadistats.clarity.wire.common.proto.DotaCommonMessages;
import skadistats.clarity.wire.common.proto.DotaUserMessages;
import util.Cache;
import util.ClarityUtil;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class MouseParser extends Parser {

    // Threshold for consecutive mouse events in gameticks
    public static int CTT = 10;

    private int lastTick;
    private MousePosition lastPosition;

    private MouseMoveSequence currentSequence;
    private ArrayList<MouseMoveSequence> movements;

    private Cache<Integer, MousePosition> mousePositionCache;

    private PrintWriter mouseMovementWriter;

    public MouseParser(long filterSteamID) {
        super(filterSteamID);
        this.movements = new ArrayList<>();
        this.mousePositionCache = new Cache<>(5);
    }

    public void writeMouseMovements(PlayerData pd, Entity player) {
        if (player != null && pd != null) {
            long steamID = pd.getSteamID();
            int mouseX = ClarityUtil.getEntityProperty(player, "m_iCursor.0000");
            int mouseY = ClarityUtil.getEntityProperty(player, "m_iCursor.0001");
            MousePosition newPosition = new MousePosition(mouseX, mouseY, gameTick);

            if (lastPosition == null) {
                lastPosition = newPosition;
                lastTick = gameTick;

                currentSequence = new MouseMoveSequence(newPosition);
            }

            if (!lastPosition.equals(newPosition)) {
                if (gameTick - lastTick < CTT) {
                    currentSequence.add(newPosition);
                }
                else {
                    movements.add(currentSequence);
                    currentSequence = new MouseMoveSequence(newPosition);
                }
                lastTick = gameTick;
            }

            lastPosition = newPosition;
            mouseMovementWriter.write(steamID + "," + gameTick + "," + mouseX + "," + mouseY + "," + currentSequence + "\n");
            mouseMovementWriter.flush();
        }
    }


    public void parseUnitOrder(DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders msg) {
        if (msg.getOrderType() == 3 | msg.getOrderType() == 4) {
            MousePosition attackPosition = new MousePosition(lastPosition.x, lastPosition.y, gameTick);
            MouseMoveAttack attack = new MouseMoveAttack(currentSequence, attackPosition);
            System.out.println(attack);

        }
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
