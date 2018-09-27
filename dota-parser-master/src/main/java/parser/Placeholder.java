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
}
*/
