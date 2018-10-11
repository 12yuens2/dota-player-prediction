package parser;

public class Placeholder {
	
}
/*
@UsesEntities
public class Main {

    private final Logger log = LoggerFactory.getLogger(Main.class.getPackage().getClass());

    private final String outputPath = "output/";
    private final int MIN_MOVEMENTS = 5;

    private FieldPath originX;
    private String filterAccountID;
    private int filterPlayerID = -1;
    private int tick = -1;
    
    private ArrayList<MouseMovement> movements = new ArrayList<MouseMovement>();

    BufferedWriter bw = null;
    FileWriter fw = null;
    BufferedWriter bwMovements = null;
    FileWriter fwMovements = null;

    private Map IDtoIndex = new HashMap();
    private Map IndexToID = new HashMap();

    private boolean isPlayer(Entity e) {
        return e.getDtClass().getDtName().startsWith("CDOTAPlayer");
    }

    private boolean isWearable(Entity e) {
        return e.getDtClass().getDtName().startsWith("CDOTAWearableItem");
    }

    private <T> T get(Entity e, String property) {
        try {
            FieldPath f = e.getDtClass().getFieldPathForName(property);
            return e.getPropertyForFieldPath(f);

        } catch (Exception x) {
            return null;
        }
    }

    private static CompressorInputStream getStreamForCompressedFile(String fileIn) throws FileNotFoundException, CompressorException {
        FileInputStream fin = new FileInputStream(fileIn);
        BufferedInputStream bis = new BufferedInputStream(fin);
        return new CompressorStreamFactory().createCompressorInputStream(bis);
    }

    @OnTickStart
    public void onTickStart(Context ctx, boolean synthetic) {
        tick = ctx.getTick();
    }

    @OnEntityEntered
    public void onEntityEntered(Context ctx, Entity e) {
        if (e == null) {
            return;
        }
        if (isPlayer(e)) {
            Integer playerIndex = e.getIndex();
            Integer playerID = get(e, "m_iPlayerID");
            IDtoIndex.put(playerID, playerIndex);
            IndexToID.put(playerIndex, playerID);
        }
        if (filterPlayerID != -1) {
            return;
        }
//        if (isWearable(e)) {
//            Integer aID = get(e, "m_iAccountID");
//            if (aID > 0 && (aID.toString().equals(filterAccountID))) {
//                Integer ownerEntity = get(e, "m_hOwnerEntity");
//                Entity owner = ctx.getProcessor(Entities.class).getByHandle(ownerEntity);
//                if (owner != null) {
//                    filterPlayerID = get(owner, "m_iPlayerID");
//                }
//            }
//        }
    }

    @OnEntityUpdated
    public void onUpdated(Entity e, FieldPath[] updatedPaths, int updateCount) {
        if (!isPlayer(e)) {
            return;
        }
        if (filterPlayerID == -1) {
            return;
        }
        if (originX == null) {
            originX = e.getDtClass().getFieldPathForName("m_iCursor.0000");
        }
        boolean update = false;
        for (int i = 0; i < updateCount; i++) {
            if (updatedPaths[i].equals(originX)) {
                update = true;
                break;
            }
        }
        if (update) {
            Integer pID = get(e, "m_iPlayerID");
            if (pID == filterPlayerID) {
                Integer mouseX = get(e, "m_iCursor.0000");
                Integer mouseY = get(e, "m_iCursor.0001");
                try {
                    bw.write(String.format("m,%s,%s,%s\n", mouseX, mouseY, tick));
                } catch (IOException x) {
                    System.out.println(x);
                }
                if (movements.isEmpty()) {
					movements.add(new MouseMovement(new MousePosition(mouseX, mouseY, tick)));
				}
                else if (!movements.get(movements.size() - 1).add(new MousePosition(mouseX, mouseY, tick))) {
                    MouseMovement last = movements.get(movements.size() - 1);
                    if (last.size() != MIN_MOVEMENTS) {
                        movements.remove(last);
                    }
					movements.add(new MouseMovement(new MousePosition(mouseX, mouseY, tick)));
				}
            }
        }
    }

    @OnMessage(DotaUserMessages.CDOTAUserMsg_SpectatorPlayerClick.class)
    public void onMessage(Context ctx, DotaUserMessages.CDOTAUserMsg_SpectatorPlayerClick message) {
        if (IDtoIndex.get(filterPlayerID).equals(message.getEntindex())) {
            try {
                bw.write(String.format("c,%s,%s,%s\n", message.getOrderType(), message.getTargetIndex(), tick));
            } catch (IOException x) {
                System.out.println(x);
            }
        }
    }

    public void run(String[] args) throws Exception {
//        if (args.length != 3) {
//            System.out.println("\tUsage:");
//            System.out.println("\t\tdemo_file.dem.bz2 replay_id steam32_id");
//            System.out.println("\tFormat:");
//            System.out.println("\t\t#m,mouseX,mouseY,tick");
//            System.out.println("\t\t#c,type,target,tick");
//            System.out.println("\tOutput:");
//            System.out.println("\t\t./output/demoid_steamid.csv");
//            return;
//        }
        //filterAccountID = args[2];

        try {
            File outputDir = new File(outputPath);
            outputDir.mkdir();

            String fileName = "textoutput.csv";
            
            File outputFile = new File(outputDir, fileName);
            fw = new FileWriter(outputFile);
            bw = new BufferedWriter(fw);
 
            try {
                bw.write("#m,mouseX,mouseY,tick\n");
                bw.write("#c,type,target,tick\n");
            } catch (IOException ix) {
                System.out.println(ix);
            }

            CompressorInputStream cis = getStreamForCompressedFile("replay.dem.bz2");
            new SimpleRunner(new InputStreamSource(cis)).runWith(this);
            
            // if (movements != null && movements.size() > 1) {
            //     File outputFileMovements = new File(outputDir, args[1] + "_" + args[2] + "_movements.json");
            //     fwMovements = new FileWriter(outputFileMovements);
            //     bwMovements = new BufferedWriter(fwMovements);
            //     bwMovements.append("[");
            //     int size = 2;
            //     for (MouseMovement mm: movements) {
            //         if (mm.size() == MIN_MOVEMENTS) {
            //             bwMovements.append(mm.toString());
            //             if (size < movements.size()) {
            //                 bwMovements.append(",\n"); 
            //             }
            //             size++;
            //        }
            //     }
            //     bwMovements.append("]");
            // }
            if (movements != null && movements.size() > 1) {
                File outputFileMovements = new File(outputDir, "testoutput_movement_angles.json");
                fwMovements = new FileWriter(outputFileMovements);
                bwMovements = new BufferedWriter(fwMovements);
                bwMovements.append("[");
                int size = 2;
                for (MouseMovement mm: movements) {
                    if (mm.size() == MIN_MOVEMENTS) {
                        bwMovements.append(mm.getAngles());
                        if (size < movements.size()) {
                            bwMovements.append(",\n"); 
                        }
                        size++;
                   }
                }
                bwMovements.append("]");
            }
			
        } catch (IOException ix) {
            System.out.println(ix);
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }
                if (fw != null) {
                    fw.close();
                }
                if (bwMovements != null) {
                    bwMovements.close();
                }
                if (fwMovements != null) {
                    fwMovements.close();
                }
            } catch (IOException ex) {
                System.out.println(ex);
            }
        }
    }


//    @OnCombatLogEntry
//    public void onCombatLogEntry(CombatLogEntry cle) {
//        if (getAttackerNameCompiled(cle).contains(" ")) {
//            String time = cle.getTimestamp() + ": " + gameTick;
//        switch (cle.getType()) {
//            case DOTA_COMBATLOG_DAMAGE:
//                log.info("{} {} hits {}{} for {} damage{}",
//                        time,
//                        getAttackerNameCompiled(cle),
//                        getTargetNameCompiled(cle),
//                        cle.getInflictorName() != null ? String.format(" with %s", cle.getInflictorName()) : "",
//                        cle.getValue(),
//                        cle.getHealth() != 0 ? String.format(" (%s->%s)", cle.getHealth() + cle.getValue(), cle.getHealth()) : ""
//                );
//                break;
//            case DOTA_COMBATLOG_HEAL:
//                log.info("{} {}'s {} heals {} for {} health ({}->{})",
//                        time,
//                        getAttackerNameCompiled(cle),
//                        cle.getInflictorName(),
//                        getTargetNameCompiled(cle),
//                        cle.getValue(),
//                        cle.getHealth() - cle.getValue(),
//                        cle.getHealth()
//                );
//                break;
//            case DOTA_COMBATLOG_MODIFIER_ADD:
//                log.info("{} {} receives {} buff/debuff from {}",
//                        time,
//                        getTargetNameCompiled(cle),
//                        cle.getInflictorName(),
//                        getAttackerNameCompiled(cle)
//                );
//                break;
//            case DOTA_COMBATLOG_MODIFIER_REMOVE:
//                log.info("{} {} loses {} buff/debuff",
//                        time,
//                        getTargetNameCompiled(cle),
//                        cle.getInflictorName()
//                );
//                break;
//            case DOTA_COMBATLOG_DEATH:
//                log.info("{} {} is killed by {}",
//                        time,
//                        getTargetNameCompiled(cle),
//                        getAttackerNameCompiled(cle)
//                );
//                break;
//            case DOTA_COMBATLOG_ABILITY:
//                log.info("{} {} {} ability {} (lvl {}){}{}",
//                        time,
//                        getAttackerNameCompiled(cle),
//                        cle.isAbilityToggleOn() || cle.isAbilityToggleOff() ? "toggles" : "casts",
//                        cle.getInflictorName(),
//                        cle.getAbilityLevel(),
//                        cle.isAbilityToggleOn() ? " on" : cle.isAbilityToggleOff() ? " off" : "",
//                        cle.getTargetName() != null ? " on " + getTargetNameCompiled(cle) : ""
//                );
//                break;
//            case DOTA_COMBATLOG_ITEM:
//                log.info("{} {} uses {}",
//                        time,
//                        getAttackerNameCompiled(cle),
//                        cle.getInflictorName()
//                );
//                break;
//            case DOTA_COMBATLOG_GOLD:
//                log.info("{} {} {} {} gold",
//                        time,
//                        getTargetNameCompiled(cle),
//                        cle.getValue() < 0 ? "looses" : "receives",
//                        Math.abs(cle.getValue())
//                );
//                break;
//            case DOTA_COMBATLOG_GAME_STATE:
//                log.info("{} game state is now {}",
//                        time,
//                        cle.getValue()
//                );
//                break;
//            case DOTA_COMBATLOG_XP:
//                log.info("{} {} gains {} XP",
//                        time,
//                        getTargetNameCompiled(cle),
//                        cle.getValue()
//                );
//                break;
//            case DOTA_COMBATLOG_PURCHASE:
//                log.info("{} {} buys item {}",
//                        time,
//                        getTargetNameCompiled(cle),
//                        cle.getValueName()
//                );
//                break;
//            case DOTA_COMBATLOG_BUYBACK:
//                log.info("{} player in slot {} has bought back",
//                        time,
//                        cle.getValue()
//                );
//                break;
//
//            default:
////                DotaUserMessages.DOTA_COMBATLOG_TYPES type = cle.getType();
////                log.info("\n{} ({})\n", type.name(), type.ordinal());
//                break;
//        }
//        }
//    }
}
*/
