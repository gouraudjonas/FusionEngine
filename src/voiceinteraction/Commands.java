/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package voiceinteraction;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.irit.elipse.enseignement.isia.PaletteGraphique;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author sacapuce
 */
public class Commands {

    //private static boolean isThereAHergerFrame = false;
    private static boolean isThereAPaletteFrame = false;
    private static boolean isThereAIcarFrame = false;

    private static final String address = "127.255.255:2010";
    private static Timer timer = new Timer();

    private static boolean create = false;
    private static boolean moving = false;
    private static boolean information = false;
    private static boolean ici = false;

    private static ArrayList<String[]> names = new ArrayList();
    private static String formDesignate = "";
    private static String instruction = "";

    /**
     * Manage treatment of all ivy messages and dispatch to lower units
     * 
     * @param strings
     * @param bus 
     */
    public static void commandsGeneration(String[] strings, Ivy bus) {
        // Initialize a client : palette, icar, herger
        initialiser(strings);

        // Get a creation
        creation(strings, bus);

        // Get a moving
        moving(strings, bus);

        // Get a mouse click
        mouseReleased(strings, bus);

        // Get a color
        color(strings, bus);

        // Get a place
        emplacement(strings, bus);

        // Get a test result
        resultTesterPoint(strings, bus);

        // Get an asking for information result
        resultAskingInfo(strings);

        // Get an information
        information(strings, bus);

        // Get a clean
        clean(strings);
    }
    
    /**
     * Lower unit managing initialization
     * 
     * @param strings 
     */
    private static void initialiser(String[] strings) {
        if (findWord(strings, "initialiser")) {
            // Initiliaze Palette
            if (!isThereAPaletteFrame && findWord(strings, "palette")) {
                PaletteGraphique myPG = new PaletteGraphique(address, 0, 0, 300, 300);
                isThereAPaletteFrame = true;
            }

            // Initialize Icar
            if (!isThereAIcarFrame && findWord(strings, "icar")) {
                // Not the proper component to initialize, nothing happens
                //icar.IcarComponent myIcar = new icar.IcarComponent(address, 100, 100);
                isThereAIcarFrame = true;
            }

            // Initialize Herger
            /*if (!isThereAHergerFrame && findWord(strings, "herger")) {
             HergerUI myHerger = new HergerUI(address, 0, 0, 300, 300);
             isThereAHergerFrame = true;
             }*/
        }
    }

    /**
     * Lower unit managing creation
     * 
     * @param strings
     * @param bus 
     */
    private static void creation(String[] strings, Ivy bus) {
        if (findWord(strings, "Action:creation")) {
            // A creation is the first command
            executeCommands(bus);
            restartTimer(bus);

            if (findWord(strings, "rectangle")) {
                instruction = instruction.concat("Palette:CreerRectangle ");
            } else if (findWord(strings, "ellipse")) {
                instruction = instruction.concat("Palette:CreerEllipse ");
            }
            create = true;
        }
    }

    /**
     * Lower unit managing moving
     * 
     * @param strings
     * @param bus 
     */
    private static void moving(String[] strings, Ivy bus) {
        if (findWord(strings, "Action:deplacement")) {
            // A creation is the first command
            executeCommands(bus);
            restartTimer(bus);

            if (findWord(strings, "rectangle")) {
                instruction = instruction.concat("Palette:DeplacerObjet ");
                formDesignate = "R";
            } else if (findWord(strings, "ellipse")) {
                instruction = instruction.concat("Palette:DeplacerObjet ");
                formDesignate = "E";
            }
            moving = true;
        }
    }

    /**
     * Lower unit managing mouse releasing
     * 
     * @param strings
     * @param bus 
     */
    private static void mouseReleased(String[] strings, Ivy bus) {
        if (findWord(strings, "Palette:MouseReleased")) {
            restartTimer(bus);

            String x, y;

            // Get numbers and equal signs
            String copy = strings[0].replaceAll("[([a-z]|[A-Z]|:| )]", "");
            // Get numbers
            String[] numbers = copy.split("=");
            x = numbers[1];
            y = numbers[2];

            // If it's a creation, then add information to the command
            if (create) {
                instruction = instruction.concat("x=" + x + " y=" + y + " ");

                // If it's a moving, then ask name of showed form
            } else if (moving) {
                try {
                    bus.sendMsg("Palette:TesterPoint x=" + x + " y=" + y);
                } catch (IvyException ex) {
                    Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (ici && !(names.isEmpty())) {
                // Normally it remains only one element
                instruction = instruction.concat("nom=" + names.get(0)[0] + " ");

                int deltaX = Integer.parseInt(x) - Integer.parseInt(names.get(0)[1]);
                int deltaY = Integer.parseInt(y) - Integer.parseInt(names.get(0)[2]);
                instruction = instruction.concat("x=" + deltaX + " y=" + deltaY + " ");
            }
        }
    }

    /**
     * Lower unit managing colors
     * 
     * @param strings
     * @param bus 
     */
    private static void color(String[] strings, Ivy bus) {
        if (findWord(strings, "Couleur")) {
            restartTimer(bus);

            if (create) {
                if (findWord(strings, "rouge")) {
                    instruction = instruction.concat("couleurFond=255:0:0 couleurContour=255:0:0 ");
                }
                if (findWord(strings, "vert")) {
                    instruction = instruction.concat("couleurFond=0:255:0 couleurContour=0:255:0 ");
                }
                if (findWord(strings, "bleu")) {
                    instruction = instruction.concat("couleurFond=0:0:255 couleurContour=0:0:255 ");
                }

                // If it's a moving, then color is designation information
            } else if (moving) {
                String designateColor = "";
                if (findWord(strings, "rouge")) {
                    designateColor = "rouge";
                } else if (findWord(strings, "vert")) {
                    designateColor = "vert";
                } else if (findWord(strings, "bleu")) {
                    designateColor = "bleu";
                }

                for (String[] element : names) {
                    if (element[3].contains(designateColor)) {
                        instruction = instruction.concat("nom=" + element[0] + " ");
                    } else {
                        names.remove(element);
                    }
                }
                moving = false;
            }
        }
    }

    /**
     * Lower unit managing declaration of a future emplacement ("ici")
     * 
     * @param strings
     * @param bus 
     */
    private static void emplacement(String[] strings, Ivy bus) {
        if (findWord(strings, "Emplacement")) {
            restartTimer(bus);
            ici = true;
        }
    }

    /**
     * Lower unit managing result of an asking to test if a point is inside a form
     * 
     * @param strings
     * @param bus 
     */
    private static void resultTesterPoint(String[] strings, Ivy bus) {
        if (findWord(strings, "Palette:ResultatTesterPoint")) {
            // We don't care about the first part, the second is the name
            String[] nameFormShowed = strings[0].split("nom=");

            // There is no need to seek information if it's not the desired form
            if (nameFormShowed[1].contains(formDesignate)) {
                try {
                    bus.sendMsg("Palette:DemanderInfo nom=" + nameFormShowed[1]);
                } catch (IvyException ex) {
                    Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    /**
     * Lower unit managing result of an asking to have informations about a form
     * 
     * @param strings 
     */
    private static void resultAskingInfo(String[] strings) {
        if (findWord(strings, "Palette:Info")) {
            if (moving) {
                String[] infoFormShowed = strings[0].split("=");

                String[] usefulInfos = new String[4];
                usefulInfos[0] = infoFormShowed[1].replaceAll("[[^RE0123456789]]", "");
                usefulInfos[1] = infoFormShowed[2].replaceAll("[[^0-9]]", "");
                usefulInfos[2] = infoFormShowed[3].replaceAll("[[^0-9]]", "");
                if (findWord(strings, "r=255,g=0,b=0")) {
                    usefulInfos[3] = "rouge";
                } else if (findWord(strings, "r=0,g=255,b=0")) {
                    usefulInfos[3] = "vert";
                } else if (findWord(strings, "r=0,g=0,b=255")) {
                    usefulInfos[3] = "bleu";
                }
                names.add(usefulInfos);

                moving = false;
            }
        }
    }

    /**
     * Lower unit managing information asking
     * 
     * @param strings
     * @param bus 
     */
    private static void information(String[] strings, Ivy bus) {
        if (findWord(strings, "information")) {
            restartTimer(bus);
            information = true;
        }
    }

    /**
     * Lower unit managing cleaning of instructions
     * 
     * @param strings 
     */
    private static void clean(String[] strings) {
        if (findWord(strings, "nettoyer") && findWord(strings, "commandes")) {
            timer.cancel();
            timer.purge();
            timer = new Timer();
            instruction = "";
        }
    }

    /**
     * Restart the timer
     * 
     * @param bus 
     */
    public static void restartTimer(Ivy bus) {
        timer.cancel();
        timer.purge();
        timer = new Timer();
        setTimerTask(bus);
    }

    /**
     * Launch the timer for 2s ; at the end it executes executeCommands function
     * 
     * @param bus 
     */
    public static void setTimerTask(Ivy bus) {
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                executeCommands(bus);
            }
        }, 2000);
    }

    /**
     * Send instruction on the ivy bus and reset all variables
     * 
     * @param bus 
     */
    public static void executeCommands(Ivy bus) {
        try {
            if (instruction != "") {
                bus.sendMsg(instruction);
            }
        } catch (IvyException ex) {
            Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
        }

        instruction = "";
        formDesignate = "";
        names.clear();
        create = false;
        moving = false;
        information = false;
    }

    /**
     * Find a specified word inside a string
     * 
     * @param string
     * @param word
     * @return true if the word is inside the string, false if not
     */
    public static boolean findWord(String string, String word) {
        return string.contains(word);
    }

    /**
     * Find a specified word inside a string array 
     * 
     * @param strings
     * @param word
     * @return true if the word is inside the string, false if not
     */
    public static boolean findWord(String[] strings, String word) {
        for (String string : strings) {
            if (string.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
