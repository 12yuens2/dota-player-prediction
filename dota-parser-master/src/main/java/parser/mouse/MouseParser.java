package parser.mouse;

import parser.Parser;
import parser.PlayerData;
import parser.mouse.atomic.MousePosition;
import parser.mouse.level1.MouseMoveSequence;
import parser.mouse.level3.Level3MouseAction;
import parser.mouse.level3.MouseMoveAttack;
import parser.mouse.level3.MouseMoveCast;
import parser.mouse.level3.MouseMoveMove;
import skadistats.clarity.model.Entity;
import skadistats.clarity.wire.common.proto.DotaCommonMessages;
import skadistats.clarity.wire.common.proto.DotaUserMessages;
import util.ClarityUtil;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class MouseParser extends Parser {

    // Threshold for consecutive mouse events in gameticks
    public static int CTT = 10;

    private int lastTick;

    private HashMap<Long, MousePosition> lastPositions;
    private HashMap<Long, MouseMoveSequence> currentSequences;
    private ArrayList<MouseMoveSequence> movements;

    private PrintWriter mouseMovementWriter, mouseActionWriter;

    public MouseParser(long filterSteamID) {
        super(filterSteamID);
        this.movements = new ArrayList<>();
        this.lastPositions = new HashMap<>();
        this.currentSequences = new HashMap<>();
    }

    public void writeMouseMovements(PlayerData pd, Entity player) {
        if (player != null && pd != null) {
            long steamid = pd.getSteamID();
            int mouseX = ClarityUtil.getEntityProperty(player, "m_iCursor.0000");
            int mouseY = ClarityUtil.getEntityProperty(player, "m_iCursor.0001");
            MousePosition newPosition = new MousePosition(mouseX, mouseY, gameTick);
            MousePosition lastPosition = lastPositions.get(steamid);

            if (lastPosition == null) {
                lastPosition = newPosition;
                lastPositions.put(steamid, newPosition);
                lastTick = gameTick;

                currentSequences.put(steamid, new MouseMoveSequence(newPosition));
            }

            if (!lastPosition.equals(newPosition)) {
                MouseMoveSequence currentSequence = currentSequences.get(steamid);
                if (gameTick - lastTick < CTT) {
                    currentSequence.add(newPosition);
                }
                else {
                    movements.add(currentSequence);
                    writeStats(steamid, currentSequence, mouseMovementWriter);
                    currentSequences.put(steamid, new MouseMoveSequence(newPosition));
                }
                lastTick = gameTick;
            }

            lastPositions.put(steamid, newPosition);
        }
    }

    private void writeStats(long steamid, MouseActivity activity, PrintWriter writer) {
        String stats = activity.outputStats();

        writer.write(steamid + "," +  stats + "\n");
        writer.flush();
    }


    public void parseUnitOrder(long steamid, DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders msg) {
        MouseMoveSequence currentSequence = currentSequences.get(steamid);
        MousePosition lastPosition = lastPositions.get(steamid);

        MousePosition actionPosition = new MousePosition(lastPosition.x, lastPosition.y, gameTick);
        Level3MouseAction level3Action = null;
        switch (msg.getOrderType()) {
            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_ATTACK_MOVE_VALUE:
            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_ATTACK_TARGET_VALUE:
                level3Action = new MouseMoveAttack(currentSequence, actionPosition);
                break;

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_MOVE_TO_POSITION_VALUE:
            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_MOVE_TO_TARGET_VALUE:
                level3Action = new MouseMoveMove(currentSequence, actionPosition);
                break;

            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_CAST_POSITION_VALUE:
            case DotaCommonMessages.dotaunitorder_t.DOTA_UNIT_ORDER_CAST_TARGET_VALUE:
                level3Action = new MouseMoveCast(currentSequence, actionPosition);
                break;
        }

        if (level3Action != null) {
            writeStats(steamid, level3Action, mouseActionWriter);
        }
    }


    public void initWriter(String mouseMovementFilename, String mouseActionFilename) throws FileNotFoundException {
        MousePosition tempPosition = new MousePosition(0, 0, 0);
        MouseMoveSequence tempSequence = new MouseMoveSequence(tempPosition);
        MouseMoveAttack tempAction = new MouseMoveAttack(tempSequence, tempPosition);

        mouseMovementWriter = new PrintWriter(mouseMovementFilename);
        mouseMovementWriter.write(headers(tempSequence));
        mouseMovementWriter.flush();

        mouseActionWriter = new PrintWriter(mouseActionFilename);
        mouseActionWriter.write(headers(tempAction));
        mouseActionWriter.flush();
    }

    private String headers(MouseActivity activity) {
        return String.format("%s,%s\n", "steamid", activity.headers());
    }

    public void closeWriter() {
        gameTick = 0;

        if (mouseMovementWriter != null) {
            mouseMovementWriter.flush();
            mouseMovementWriter.close();
        }

        if (mouseActionWriter != null) {
            mouseActionWriter.flush();
            mouseActionWriter.close();
        }
    }
}
