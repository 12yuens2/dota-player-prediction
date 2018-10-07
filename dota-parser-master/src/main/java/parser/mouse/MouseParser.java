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
import util.VectorFeatures;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class MouseParser extends Parser {

    // Threshold for consecutive mouse events in gameticks
    public static int CTT = 10;

    private int lastTick;
    private MousePosition lastPosition;

    private MouseMoveSequence currentSequence;
    private ArrayList<MouseMoveSequence> movements;

    private PrintWriter mouseMovementWriter;

    public MouseParser(long filterSteamID) {
        super(filterSteamID);
        this.movements = new ArrayList<>();
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
                    writeStats(currentSequence);
                    currentSequence = new MouseMoveSequence(newPosition);
                }
                lastTick = gameTick;
            }

            lastPosition = newPosition;
//            mouseMovementWriter.write(steamID + "," + gameTick + "," + mouseX + "," + mouseY + "," + currentSequence + "\n");
//            mouseMovementWriter.flush();
        }
    }

    private void writeStats(MouseMoveSequence sequence) {
        ArrayList<VectorFeatures> stats = sequence.getStats();

        StringBuilder sb = new StringBuilder();
        for (VectorFeatures features : stats) {
            sb.append(features.getStats() + ",");
        }
        mouseMovementWriter.write(sb.toString() + "\n");
        mouseMovementWriter.flush();
    }


    public void parseUnitOrder(DotaUserMessages.CDOTAUserMsg_SpectatorPlayerUnitOrders msg) {
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
    }


    public void initWriter(String filename) throws FileNotFoundException {
        mouseMovementWriter = new PrintWriter(filename);
        StringBuilder sb = new StringBuilder();

        ArrayList<VectorFeatures> features = new MouseMoveSequence(new MousePosition(0, 0, 0)).getStats();

        for (VectorFeatures vf : features) {
            sb.append(vf.getHeaders() + ",");
        }
        sb.append("\n");
        mouseMovementWriter.write(sb.toString());
        mouseMovementWriter.flush();
    }

    public void closeWriter() {
        if (mouseMovementWriter != null) {
            mouseMovementWriter.flush();
            mouseMovementWriter.close();
        }
    }
}
