/*
 * Copyright (C) 2014 Sandra Bardot Jonas Gouraud
 * 
 * This file is part of FusionEngine.
 *
 * FusionEngine is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * FusionEngine is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FusionEngine. If not, see <http://www.gnu.org/licenses/>.
 */
package voiceinteraction;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyException;
import fr.irit.elipse.enseignement.isia.PaletteGraphique;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class offers treatment for all needed messages by FusionEngine. It uses
 * a state machine to know how to treat messages. The core is "number", an
 * integer which is updated when a line of recognized instructions. When timer
 * ends, an instruction is sent according to number. Every recognized message is
 * treated by a specific private function.
 *
 * @author Jonas Gouraud, Sandra Bardot
 */
public class Commands {

    //private static boolean isThereAHergerFrame = false;
    private static boolean isThereAPaletteFrame = false;
    private static boolean isThereAHergerFrame = false;

    private static final String address = "127.255.255:2010";
    private static Timer timer = new Timer();

    // Booleans needed to the statemachine
    private static boolean create = false;
    private static boolean moving = false;
    private static boolean place = false;
    private static boolean color = false;
    private static boolean ici = false;
    private static boolean designateColor = false;

    private static String formName = "";
    private static String placeName = "";
    private static String colorName = "";
    private static String designateColorName = "";
    private static String movement = "";
    private static ArrayList<String> designateFormNames = new ArrayList();
    private static ArrayList<String[]> names = new ArrayList();

    // Convention pour les messages envoyés
    // 0 => aucune instruction
    // 1 => creation d'une forme
    // 2 => creation d'une forme a un emplacement
    // 3 => creation d'une forme avec une couleur
    // 4 => creation d'une forme a un emplacement et avec une couleur
    // 5 => deplacer une forme a un emplacement
    // 6 => deplacer une forme selon un mouvement
    private static int number = 0;

    /**
     * Manage treatment of all ivy messages and dispatch to lower units
     *
     * @param strings
     * @param bus
     */
    public static void commandsTreatment(String[] strings, Ivy bus) {
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
        //information(strings, bus);
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

            // Initialize Herger
            if (!isThereAHergerFrame && findWord(strings, "herger")) {
                recogestureherger.HergerUI myHerger
                        = new recogestureherger.HergerUI(address, 0, 0, 300, 300);
                isThereAHergerFrame = true;
            }
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
            // Cleaning and timer
            executeCommands(bus);
            restartTimer(bus);

            // Recognition
            if (findWord(strings, "rectangle")) {
                formName = "Rectangle";
            } else if (findWord(strings, "ellipse")) {
                formName = "Ellipse";
            }

            // State machine
            number = 1;
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
            // Cleaning and timer
            executeCommands(bus);
            restartTimer(bus);

            if (findWord(strings, "rectangle")) {
                formName = "R";
            } else if (findWord(strings, "ellipse")) {
                formName = "E";
            }

            // State machine
            number = -1;
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
            // Timer
            restartTimer(bus);

            // Get numbers and equal signs
            String x, y;
            String copy = strings[0].replaceAll("[([a-z]|[A-Z]|:| )]", "");
            // Get numbers
            String[] numbers = copy.split("=");
            x = numbers[1];
            y = numbers[2];

            // If it's a creation, then add information to the command
            if (create && ici) {
                placeName = "x=" + x + " y=" + y;

                //State machine
                if (color) {
                    number = 4;
                } else {
                    number = 2;
                }
                place = true;

            } else if (moving) {

                // If we have the name of the form, then we remember it
                if (ici && !(names.isEmpty())) {
                    int deltaX, deltaY;
                    // We will move all remaining elements
                    names.stream().forEach((element) -> {
                        designateFormNames.add(element[0]);
                    });

                    // Same movement for all
                    deltaX = Integer.parseInt(x) - Integer.parseInt(names.get(0)[1]);
                    deltaY = Integer.parseInt(y) - Integer.parseInt(names.get(0)[2]);
                    movement = "x=" + deltaX + " y=" + deltaY;

                    // State machine
                    number = 5;
                    place = true;

                    // If we don't have yet the name of the form, then we ask
                } else if (!ici) {
                    try {
                        placeName = "x=" + x + " y=" + y;
                        bus.sendMsg("Palette:TesterPoint " + placeName);
                    } catch (IvyException ex) {
                        Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    // State machine
                    number = -1;
                }
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

            // Remember color
            if (create) {
                if (findWord(strings, "rouge")) {
                    colorName = "couleurFond=255:0:0 couleurContour=255:0:0";
                } else if (findWord(strings, "vert")) {
                    colorName = "couleurFond=0:255:0 couleurContour=0:255:0";
                } else if (findWord(strings, "bleu")) {
                    colorName = "couleurFond=0:0:255 couleurContour=0:0:255";
                }

                //State machine
                if (place) {
                    number = 4;
                } else {
                    number = 3;
                }
                color = true;

                // If it's a moving, then color is designation information :
                // we store it to detect later
            } else if (moving && !designateColor) {
                if (findWord(strings, "rouge")) {
                    designateColorName = "rouge";
                } else if (findWord(strings, "vert")) {
                    designateColorName = "vert";
                } else if (findWord(strings, "bleu")) {
                    designateColorName = "bleu";
                }

                // We remove all forms which are not of specified color
                // Forced to use a temp because we can't remove an element in
                // the loop
                ArrayList<String[]> temp = new ArrayList<>();
                names.stream().filter((element) -> (element[3].contains(designateColorName))).forEach((element) -> {
                    temp.add(element);
                });
                names = temp;
                
                // State machine
                number = -1;
                designateColor = true;
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
     * Lower unit managing result of test to know if a point is inside a form
     *
     * @param strings
     * @param bus
     */
    private static void resultTesterPoint(String[] strings, Ivy bus) {
        if (findWord(strings, "Palette:ResultatTesterPoint")) {
            // We don't care about the first part, the second is the name
            String[] nameFormShowed = strings[0].split("nom=");

            // There is no need to seek information if it's not the desired form
            if (nameFormShowed[1].contains(formName)) {
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
                // Name
                usefulInfos[0] = infoFormShowed[1].replaceAll("[[^RE0123456789]]", "");
                // x
                usefulInfos[1] = infoFormShowed[2].replaceAll("[[^0-9]]", "");
                // y
                usefulInfos[2] = infoFormShowed[3].replaceAll("[[^0-9]]", "");
                // Color
                if (findWord(strings, "r=255,g=0,b=0")) {
                    usefulInfos[3] = "rouge";
                } else if (findWord(strings, "r=0,g=255,b=0")) {
                    usefulInfos[3] = "vert";
                } else if (findWord(strings, "r=0,g=0,b=255")) {
                    usefulInfos[3] = "bleu";
                } else {
                    usefulInfos[3] = "none";
                }

                // We add all information inside names, to get what we want later
                names.add(usefulInfos);

                // State machine
                number = -1;
            }
            /*System.out.print("DEBUG > names contains : |");
            names.stream().map((element) -> {
                for (String string : element) {
                    System.out.print(string + "-");
                }
                return element;
            }).forEach((_item) -> {
                System.out.print("|");
            });
            System.out.println();*/
        }
    }

    /**
     * Lower unit managing cleaning of instructions
     *
     * @param strings
     */
    private static void clean(String[] strings) {
        if (findWord(strings, "nettoyer") && findWord(strings, "commandes")) {
            restartStateMachine();
        }
    }

    /**
     * Restart the timer
     *
     * @param bus
     */
    private static void restartTimer(Ivy bus) {
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
    private static void setTimerTask(Ivy bus) {
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
    // Convention pour les messages envoyés
    // 0 => aucune instruction
    // 1 => creation d'une forme
    // 2 => creation d'une forme a un emplacement
    // 3 => creation d'une forme avec une couleur
    // 4 => creation d'une forme a un emplacement et avec une couleur
    // 5 => deplacer une forme a un emplacement
    // 6 => deplacer une forme selon un mouvement
    private static void executeCommands(Ivy bus) {
        try {
            switch (number) {
                case -1:
                    bus.sendMsg("Wrong set of instructions");
                case 0:
                    //nothing to do
                    break;
                case 1:
                    bus.sendMsg("Palette:Creer" + formName);
                    break;
                case 2:
                    bus.sendMsg("Palette:Creer" + formName + " " + placeName);
                    break;
                case 3:
                    bus.sendMsg("Palette:Creer" + formName + " " + colorName);
                    break;
                case 4:
                    bus.sendMsg("Palette:Creer" + formName + " " + placeName + " " + colorName);
                    break;
                case 5:
                    for (String form : designateFormNames) {
                        bus.sendMsg("Palette:DeplacerObjet nom=" + form + " " + movement);
                    }
                    break;
                default:
                    break;
            }
        } catch (IvyException ex) {
            Logger.getLogger(Commands.class.getName()).log(Level.SEVERE, null, ex);
        }

        restartStateMachine();
    }

    /**
     * Restart the state machine by putting all booleans at false and number at
     * 0
     */
    private static void restartStateMachine() {
        // Strings
        formName = "";
        colorName = "";
        placeName = "";
        designateColorName = "";
        movement = "";

        // Number and arraylists
        number = 0;
        names.clear();
        designateFormNames.clear();

        // Booleans
        create = false;
        moving = false;
        place = false;
        color = false;
        ici = false;
        designateColor = false;
    }

    /**
     * Find a specified word inside a string array
     *
     * @param strings
     * @param word
     * @return true if the word is inside the string, false if not
     */
    private static boolean findWord(String[] strings, String word) {
        for (String string : strings) {
            if (string.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
